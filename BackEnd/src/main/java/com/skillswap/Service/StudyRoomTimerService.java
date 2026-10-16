package com.skillSwap.Service;

import com.skillSwap.Dto.studyroom.StudyRoomDtos.TimerView;
import com.skillSwap.Entity.StudyRoom;
import com.skillSwap.Entity.StudyRoomEnums.*;
import com.skillSwap.Entity.StudyRoomTimer;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Exception.StudyRoomException;
import com.skillSwap.Repository.StudyRoomTimerRepository;
import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The shared timer. The database row holds the target end time; browsers only DISPLAY (endsAt - now).
 * Every change takes a row lock, so start/pause/resume/reset/expiry can never run on top of each other.
 */
@Service
public class StudyRoomTimerService {

	private static final Logger log = LoggerFactory.getLogger(StudyRoomTimerService.class);

	private final StudyRoomTimerRepository timers;
	private final StudyRoomService roomService;
	private final StudyRoomAttendanceService attendance;
	private final StudyRoomPresenceRegistry registry;

	public StudyRoomTimerService(
		StudyRoomTimerRepository timers,
		StudyRoomService roomService,
		StudyRoomAttendanceService attendance,
		StudyRoomPresenceRegistry registry
	) {
		this.timers = timers;
		this.roomService = roomService;
		this.attendance = attendance;
		this.registry = registry;
	}

	public static int defaultSeconds(TimerPhase p) {
		return switch (p) {
			case FOCUS -> 25 * 60;
			case SHORT_BREAK -> 5 * 60;
			case LONG_BREAK -> 15 * 60;
		};
	}

	public static TimerView view(StudyRoomTimer t, Instant now) {
		int remaining =
			switch (t.getStatus()) {
				case RUNNING -> (int) Math.max(
					0,
					(long) Math.ceil(Duration.between(now, t.getEndsAt()).toMillis() / 1000.0)
				);
				case PAUSED -> t.getRemainingSeconds();
				case FINISHED -> 0;
				case IDLE -> t.getDurationSeconds();
			};
		return new TimerView(
			t.getPhase(),
			t.getStatus(),
			t.getDurationSeconds(),
			t.getEndsAt(),
			remaining,
			now,
			t.getRevision()
		);
	}

	@Transactional(readOnly = true)
	public TimerView get(Integer userId, Integer roomId) {
		roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);
		return view(load(roomId), Instant.now());
	}

	/** Host only. */
	@Transactional
	public TimerView start(Integer userId, Integer roomId, TimerPhase phase, Integer customSeconds) {
		roomService.requireHost(roomId, userId);
		StudyRoomTimer t = lock(roomId);
		Instant now = Instant.now();
		int seconds = customSeconds != null ? customSeconds : defaultSeconds(phase);
		t.setPhase(phase);
		t.setDurationSeconds(seconds);
		t.setRemainingSeconds(seconds);
		t.setStatus(TimerStatus.RUNNING);
		t.setEndsAt(now.plusSeconds(seconds));
		return changed(roomId, t, now);
	}

	/** Host, or any member when the host enabled "members can pause". */
	@Transactional
	public TimerView pause(Integer userId, Integer roomId) {
		requirePauseRights(userId, roomId);
		StudyRoomTimer t = lock(roomId);
		Instant now = Instant.now();
		if (expireIfDue(t, roomId, now)) return view(t, now);
		if (t.getStatus() != TimerStatus.RUNNING) throw new StudyRoomException(
			HttpStatus.CONFLICT,
			"The timer is not running"
		);
		t.setRemainingSeconds(Math.max(1, view(t, now).remainingSeconds()));
		t.setStatus(TimerStatus.PAUSED);
		t.setEndsAt(null);
		return changed(roomId, t, now);
	}

	@Transactional
	public TimerView resume(Integer userId, Integer roomId) {
		requirePauseRights(userId, roomId);
		StudyRoomTimer t = lock(roomId);
		Instant now = Instant.now();
		if (t.getStatus() != TimerStatus.PAUSED) throw new StudyRoomException(
			HttpStatus.CONFLICT,
			"The timer is not paused"
		);
		t.setStatus(TimerStatus.RUNNING);
		t.setEndsAt(now.plusSeconds(t.getRemainingSeconds()));
		return changed(roomId, t, now);
	}

	/** Host only. */
	@Transactional
	public TimerView reset(Integer userId, Integer roomId) {
		roomService.requireHost(roomId, userId);
		StudyRoomTimer t = lock(roomId);
		Instant now = Instant.now();
		t.setStatus(TimerStatus.IDLE);
		t.setEndsAt(null);
		t.setRemainingSeconds(t.getDurationSeconds());
		return changed(roomId, t, now);
	}

	/** Called every 2s by StudyRoomScheduler. Safe to call many times: only the first call after the deadline does anything. */
	@Transactional
	public void expireIfDue(Integer roomId) {
		StudyRoomTimer t = timers.findByRoomIdForUpdate(roomId).orElse(null);
		if (t == null) return;
		Instant now = Instant.now();
		if (expireIfDue(t, roomId, now)) log.debug("Timer finished in room {}", roomId);
	}

	private boolean expireIfDue(StudyRoomTimer t, Integer roomId, Instant now) {
		if (t.getStatus() != TimerStatus.RUNNING || t.getEndsAt() == null || t.getEndsAt().isAfter(now)) return false;
		t.setStatus(TimerStatus.FINISHED);
		t.setEndsAt(null);
		t.setRemainingSeconds(0);
		changed(roomId, t, now);
		if (t.getPhase() == TimerPhase.FOCUS) {
			StudyRoom room = roomService.requireRoom(roomId);
			if (room.getStatus() == RoomStatus.LIVE) {
				// bookkeeping only (no credits here): counts a finished focus block for everyone present right now
				attendance.incrementFocusCycle(roomId, room.getSessionNo(), registry.userIds(roomId));
			}
		}
		return true;
	}

	private TimerView changed(Integer roomId, StudyRoomTimer t, Instant now) {
		t.setRevision(t.getRevision() + 1);
		t.setUpdatedAt(now);
		TimerView v = view(t, now);
		registry.broadcastAfterCommit(roomId, "timer", v);
		return v;
	}

	private void requirePauseRights(Integer userId, Integer roomId) {
		StudyRoom room = roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);
		boolean host = room.getHost().getId().equals(userId);
		if (!host && !room.isMembersCanPause()) throw new AccessDeniedException(
			"Only the host can control the timer in this room"
		);
	}

	private StudyRoomTimer lock(Integer roomId) {
		return timers
			.findByRoomIdForUpdate(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Timer", "roomId", roomId));
	}

	private StudyRoomTimer load(Integer roomId) {
		return timers.findByRoom_Id(roomId).orElseThrow(() -> new ResourceNotFoundException("Timer", "roomId", roomId));
	}

	public List<Integer> dueRoomIds() {
		return timers.findDueRoomIds(TimerStatus.RUNNING, Instant.now());
	}
}

package com.skillSwap.Service;

import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

/**
 * Two small background jobs for the WHOLE server (not one per user):
 *  1. every 2s: finish timers whose end time has passed (exactly once, see StudyRoomTimerService)
 *  2. every 15s: drop connections that stopped sending heartbeats (closed laptop, lost wifi) and auto-end timed sessions
 * (@EnableScheduling is already switched on in AsyncSchedulingConfig.)
 */
@Component
public class StudyRoomScheduler {

	private static final Logger log = LoggerFactory.getLogger(StudyRoomScheduler.class);
	private static final long STALE_AFTER_MS = 45_000; // clients send a heartbeat every 15s

	private final StudyRoomTimerService timers;
	private final StudyRoomService rooms;
	private final StudyRoomAttendanceService attendance;
	private final StudyRoomPresenceRegistry registry;

	public StudyRoomScheduler(
		StudyRoomTimerService timers,
		StudyRoomService rooms,
		StudyRoomAttendanceService attendance,
		StudyRoomPresenceRegistry registry
	) {
		this.timers = timers;
		this.rooms = rooms;
		this.attendance = attendance;
		this.registry = registry;
	}

	@Scheduled(fixedDelay = 2000, initialDelay = 5000)
	public void finishDueTimers() {
		for (Integer roomId : timers.dueRoomIds()) {
			try {
				timers.expireIfDue(roomId);
			} catch (RuntimeException e) {
				log.warn("Could not finish timer of room {}: {}", roomId, e.getMessage());
			}
		}
	}

	@Scheduled(fixedDelay = 15000, initialDelay = 15000)
	public void sweep() {
		for (var left : registry.removeStale(System.currentTimeMillis() - STALE_AFTER_MS)) {
			registry.close(left.session(), CloseStatus.GOING_AWAY);
			if (!left.userGone()) continue;
			try {
				attendance.noteLeave(left.roomId(), left.userId());
				rooms.broadcastPresence(left.roomId());
			} catch (RuntimeException e) {
				log.debug("Stale cleanup failed: {}", e.getMessage());
			}
		}
		try {
			rooms.autoEndExpiredSessions();
		} catch (RuntimeException e) {
			log.warn("Auto-ending sessions failed: {}", e.getMessage());
		}
	}
}

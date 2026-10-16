package com.skillSwap.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillSwap.Dto.studyroom.StudyRoomDtos.RoomRequest;
import com.skillSwap.Entity.*;
import com.skillSwap.Entity.StudyRoomEnums.*;
import com.skillSwap.Exception.StudyRoomException;
import com.skillSwap.Repository.*;
import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import com.skillSwap.websocket.StudyRoomPresenceRegistry.JoinResult;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.WebSocketSession;

/** Plain unit tests: no database, no Spring context, no paid services. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyRoomLogicTest {

	// ------------------------------------------------------------ helpers

	private static User user(int id, String name) {
		User u = new User();
		u.setId(id);
		u.setName(name);
		return u;
	}

	private static StudyRoom room(int id, User host, Visibility vis, int max) {
		StudyRoom r = new StudyRoom();
		r.setId(id);
		r.setHost(host);
		r.setName("Java Beginners");
		r.setVisibility(vis);
		r.setMaxParticipants(max);
		r.setStatus(RoomStatus.OPEN);
		r.setAccessCode(vis == Visibility.PRIVATE ? "ABCD2345" : null);
		return r;
	}

	private static WebSocketSession socket(String id) {
		WebSocketSession s = mock(WebSocketSession.class);
		when(s.getId()).thenReturn(id);
		when(s.isOpen()).thenReturn(true);
		return s;
	}

	private static StudyRoomMember member(StudyRoom r, User u, MemberStatus st) {
		StudyRoomMember m = new StudyRoomMember();
		m.setRoom(r);
		m.setUser(u);
		m.setStatus(st);
		return m;
	}

	// ------------------------------------------------------------ shared timer

	@Test
	void timerCountsDownFromServerEndTimeNotFromAnyBrowser() {
		StudyRoomTimer t = new StudyRoomTimer();
		Instant now = Instant.parse("2026-10-16T10:00:00Z");
		t.setStatus(TimerStatus.RUNNING);
		t.setDurationSeconds(1500);
		t.setEndsAt(now.plusSeconds(600));
		// two "different clients" asking at the same moment get the same answer
		assertThat(StudyRoomTimerService.view(t, now).remainingSeconds()).isEqualTo(600);
		assertThat(StudyRoomTimerService.view(t, now.plusSeconds(100)).remainingSeconds()).isEqualTo(500);
		assertThat(StudyRoomTimerService.view(t, now.plusSeconds(9999)).remainingSeconds()).isZero();
	}

	@Test
	void timerPausedFinishedAndIdleStates() {
		StudyRoomTimer t = new StudyRoomTimer();
		t.setDurationSeconds(1500);
		t.setStatus(TimerStatus.PAUSED);
		t.setRemainingSeconds(321);
		assertThat(StudyRoomTimerService.view(t, Instant.now()).remainingSeconds()).isEqualTo(321);
		t.setStatus(TimerStatus.FINISHED);
		assertThat(StudyRoomTimerService.view(t, Instant.now()).remainingSeconds()).isZero();
		t.setStatus(TimerStatus.IDLE);
		assertThat(StudyRoomTimerService.view(t, Instant.now()).remainingSeconds()).isEqualTo(1500);
	}

	@Test
	void defaultPhaseLengthsAre25_5_15Minutes() {
		assertThat(StudyRoomTimerService.defaultSeconds(TimerPhase.FOCUS)).isEqualTo(1500);
		assertThat(StudyRoomTimerService.defaultSeconds(TimerPhase.SHORT_BREAK)).isEqualTo(300);
		assertThat(StudyRoomTimerService.defaultSeconds(TimerPhase.LONG_BREAK)).isEqualTo(900);
	}

	// ------------------------------------------------------------ validation

	@Test
	void roomRequestRejectsBadCapacityAndBlankName() {
		Validator v = Validation.buildDefaultValidatorFactory().getValidator();
		RoomRequest ok = new RoomRequest("Java Beginners", "Java", null, Difficulty.BEGINNER, 10, Visibility.PUBLIC, null, null, false);
		assertThat(v.validate(ok)).isEmpty();
		assertThat(v.validate(new RoomRequest("Java Beginners", "Java", null, Difficulty.BEGINNER, 1, Visibility.PUBLIC, null, null, false))).isNotEmpty();
		assertThat(v.validate(new RoomRequest("Java Beginners", "Java", null, Difficulty.BEGINNER, 51, Visibility.PUBLIC, null, null, false))).isNotEmpty();
		assertThat(v.validate(new RoomRequest("  ", "Java", null, Difficulty.BEGINNER, 10, Visibility.PUBLIC, null, null, false))).isNotEmpty();
		assertThat(v.validate(new RoomRequest("Java Beginners", "Java", null, Difficulty.BEGINNER, 10, Visibility.PUBLIC, null, 2, false))).isNotEmpty();
	}

	// ------------------------------------------------------------ live presence

	@Test
	void presenceCountsAUserOnceAndEnforcesCapacity() {
		StudyRoomPresenceRegistry r = new StudyRoomPresenceRegistry(new ObjectMapper());
		assertThat(r.tryJoin(1, 10, socket("a"), 2)).isEqualTo(JoinResult.NEW_USER);
		assertThat(r.tryJoin(1, 10, socket("b"), 2)).isEqualTo(JoinResult.EXISTING_USER); // 2nd tab
		assertThat(r.count(1)).isEqualTo(1);
		assertThat(r.tryJoin(1, 11, socket("c"), 2)).isEqualTo(JoinResult.NEW_USER);
		assertThat(r.tryJoin(1, 12, socket("d"), 2)).isEqualTo(JoinResult.FULL);
		// closing one of two tabs keeps the user present; closing the last tab removes them
		assertThat(r.leave("a").orElseThrow().userGone()).isFalse();
		assertThat(r.leave("b").orElseThrow().userGone()).isTrue();
		assertThat(r.count(1)).isEqualTo(1);
		assertThat(r.leave("b")).isEmpty(); // leaving twice is harmless
	}

	@Test
	void silentConnectionsAreRemovedAsStale() {
		StudyRoomPresenceRegistry r = new StudyRoomPresenceRegistry(new ObjectMapper());
		r.tryJoin(1, 10, socket("a"), 5);
		assertThat(r.removeStale(System.currentTimeMillis() - 60_000)).isEmpty(); // fresh: kept
		assertThat(r.removeStale(System.currentTimeMillis() + 1_000)).hasSize(1); // silent: removed
		assertThat(r.count(1)).isZero();
	}

	// ------------------------------------------------------------ rooms: access rules

	private StudyRoomService roomService(StudyRoomRepository rooms, StudyRoomMemberRepository members, StudyRoomPresenceRegistry reg) {
		return new StudyRoomService(
			rooms, members, mock(StudyRoomTimerRepository.class), mock(StudyRoomNoteRepository.class),
			mock(UserRepository.class), reg, mock(StudyRoomAttendanceService.class), 5
		);
	}

	@Test
	void nonMemberAndRemovedMemberAreRejected() {
		StudyRoomRepository rooms = mock(StudyRoomRepository.class);
		StudyRoomMemberRepository members = mock(StudyRoomMemberRepository.class);
		User host = user(1, "Host");
		StudyRoom r = room(5, host, Visibility.PUBLIC, 10);
		when(rooms.findById(5)).thenReturn(Optional.of(r));
		StudyRoomService svc = roomService(rooms, members, mock(StudyRoomPresenceRegistry.class));

		when(members.findByRoom_IdAndUser_Id(5, 2)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> svc.requireActiveMember(5, 2)).isInstanceOf(AccessDeniedException.class);

		when(members.findByRoom_IdAndUser_Id(5, 3)).thenReturn(Optional.of(member(r, user(3, "Kicked"), MemberStatus.REMOVED)));
		assertThatThrownBy(() -> svc.requireActiveMember(5, 3)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> svc.join(3, 5, null)).isInstanceOf(AccessDeniedException.class);
		assertThat(svc.canConnect(5, 2)).isFalse();
	}

	@Test
	void onlyTheHostCanModifyARoom() {
		StudyRoomRepository rooms = mock(StudyRoomRepository.class);
		when(rooms.findById(5)).thenReturn(Optional.of(room(5, user(1, "Host"), Visibility.PUBLIC, 10)));
		StudyRoomService svc = roomService(rooms, mock(StudyRoomMemberRepository.class), mock(StudyRoomPresenceRegistry.class));
		assertThatThrownBy(() -> svc.requireHost(5, 2)).isInstanceOf(AccessDeniedException.class);
		assertThat(svc.requireHost(5, 1).getId()).isEqualTo(5);
	}

	@Test
	void privateRoomNeedsTheRightCodeAndGuessingIsLimited() {
		StudyRoomRepository rooms = mock(StudyRoomRepository.class);
		StudyRoomMemberRepository members = mock(StudyRoomMemberRepository.class);
		when(rooms.findById(5)).thenReturn(Optional.of(room(5, user(1, "Host"), Visibility.PRIVATE, 10)));
		when(members.findByRoom_IdAndUser_Id(5, 2)).thenReturn(Optional.empty());
		StudyRoomService svc = roomService(rooms, members, mock(StudyRoomPresenceRegistry.class));

		for (int i = 0; i < 5; i++) {
			assertThatThrownBy(() -> svc.join(2, 5, "WRONG123")).isInstanceOf(AccessDeniedException.class);
		}
		// 6th attempt is blocked even though the code would be wrong anyway: it is rate limited
		assertThatThrownBy(() -> svc.join(2, 5, "ABCD2345"))
			.isInstanceOf(StudyRoomException.class)
			.extracting(e -> ((StudyRoomException) e).getStatus())
			.isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void fullRoomRefusesNewMembers() {
		StudyRoomRepository rooms = mock(StudyRoomRepository.class);
		StudyRoomMemberRepository members = mock(StudyRoomMemberRepository.class);
		StudyRoomPresenceRegistry reg = mock(StudyRoomPresenceRegistry.class);
		when(rooms.findById(5)).thenReturn(Optional.of(room(5, user(1, "Host"), Visibility.PUBLIC, 2)));
		when(members.findByRoom_IdAndUser_Id(5, 2)).thenReturn(Optional.empty());
		when(reg.count(5)).thenReturn(2);
		when(reg.userIds(5)).thenReturn(java.util.Set.of(8, 9));
		StudyRoomService svc = roomService(rooms, members, reg);
		assertThatThrownBy(() -> svc.join(2, 5, null))
			.isInstanceOf(StudyRoomException.class)
			.extracting(e -> ((StudyRoomException) e).getStatus())
			.isEqualTo(HttpStatus.CONFLICT);
	}

	// ------------------------------------------------------------ chat

	@Test
	void unauthorisedUserCannotPostAndRetriesAreNotDuplicated() {
		StudyRoomMessageRepository messages = mock(StudyRoomMessageRepository.class);
		StudyRoomService roomService = mock(StudyRoomService.class);
		StudyRoomChatService chat = new StudyRoomChatService(messages, roomService, mock(UserRepository.class), mock(StudyRoomPresenceRegistry.class));

		when(roomService.requireActiveMember(5, 2)).thenThrow(new AccessDeniedException("no"));
		assertThatThrownBy(() -> chat.send(5, 2, "hello", "client-0001")).isInstanceOf(AccessDeniedException.class);
		verify(messages, never()).save(any());

		assertThatThrownBy(() -> chat.send(5, 1, "   ", null)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> chat.send(5, 1, "x".repeat(1001), null)).isInstanceOf(IllegalArgumentException.class);

		// a retry with the same clientId returns the stored message and saves nothing new
		StudyRoomMessage stored = new StudyRoomMessage();
		stored.setId(77L);
		stored.setSender(user(1, "Ann"));
		stored.setContent("hello");
		stored.setClientId("client-0002");
		when(messages.findByRoom_IdAndSender_IdAndClientId(5, 1, "client-0002")).thenReturn(Optional.of(stored));
		assertThat(chat.send(5, 1, "hello", "client-0002").id()).isEqualTo(77L);
		verify(messages, never()).save(any());
	}

	// ------------------------------------------------------------ notes

	@Test
	void conflictingNoteSaveIsRefused() {
		StudyRoomNoteRepository notes = mock(StudyRoomNoteRepository.class);
		StudyRoomNoteService svc = new StudyRoomNoteService(notes, mock(StudyRoomService.class), mock(UserRepository.class), mock(StudyRoomPresenceRegistry.class));
		when(notes.saveIfVersionMatches(eq(5), anyString(), any(), any(), eq(3L))).thenReturn(0);
		assertThatThrownBy(() -> svc.save(1, 5, "my text", 3L))
			.isInstanceOf(StudyRoomException.class)
			.extracting(e -> ((StudyRoomException) e).getStatus())
			.isEqualTo(HttpStatus.CONFLICT);
	}

	// ------------------------------------------------------------ rewards

	@Test
	void dailyCapLimitsThePayout() {
		UserRepository users = mock(UserRepository.class);
		CreditTransactionRepository tx = mock(CreditTransactionRepository.class);
		CreditWalletService wallet = mock(CreditWalletService.class);
		when(users.findByIdForUpdate(7)).thenReturn(Optional.of(user(7, "A")));
		when(tx.sumSince(eq(7), anyCollection(), any())).thenReturn(4L); // already earned 4 of 5 today
		StudyRewardService rewards = new StudyRewardService(users, tx, wallet, 5);

		assertThat(rewards.award(7, 3, CreditTxType.STUDY_CHALLENGE, "x")).isEqualTo(1);
		verify(wallet).credit(eq(7), eq(1), eq(CreditTxType.STUDY_CHALLENGE), isNull(), eq("x"));

		when(tx.sumSince(eq(7), anyCollection(), any())).thenReturn(5L); // cap reached
		assertThat(rewards.award(7, 3, CreditTxType.STUDY_CHALLENGE, "x")).isZero();
	}

	private StudyRoomAttendanceService attendanceService(StudyRoomAttendanceRepository repo, StudyRoomRepository rooms, StudyRewardService rewards) {
		return new StudyRoomAttendanceService(repo, rooms, mock(UserRepository.class), rewards, mock(StudyRoomPresenceRegistry.class), 15, 1, 2);
	}

	private StudyRoomAttendance row(StudyRoom r, int activeSeconds, Instant lastActive) {
		StudyRoomAttendance a = new StudyRoomAttendance();
		a.setId(40);
		a.setRoom(r);
		a.setUser(user(2, "Bob"));
		a.setSessionNo(1);
		a.setActiveSeconds(activeSeconds);
		a.setLastActiveAt(lastActive);
		return a;
	}

	@Test
	void attendanceRewardIsPaidOnlyWhenTheClaimSucceeds() {
		StudyRoomAttendanceRepository repo = mock(StudyRoomAttendanceRepository.class);
		StudyRoomRepository rooms = mock(StudyRoomRepository.class);
		StudyRewardService rewards = mock(StudyRewardService.class);
		StudyRoom r = room(5, user(1, "Host"), Visibility.PUBLIC, 10);
		r.setStatus(RoomStatus.LIVE);
		r.setSessionNo(1);
		when(rooms.findById(5)).thenReturn(Optional.of(r));
		when(repo.findByRoom_IdAndUser_IdAndSessionNo(5, 2, 1)).thenReturn(Optional.of(row(r, 890, Instant.now().minusSeconds(10))));
		when(repo.findById(40)).thenReturn(Optional.of(row(r, 900, Instant.now()))); // 15 minutes reached
		when(rewards.award(eq(2), eq(1), eq(CreditTxType.STUDY_ATTENDANCE), anyString())).thenReturn(1);
		StudyRoomAttendanceService svc = attendanceService(repo, rooms, rewards);

		when(repo.claimReward(40)).thenReturn(0); // somebody else already claimed it
		svc.onHeartbeat(5, 2, true, 2);
		verify(rewards, never()).award(anyInt(), anyInt(), any(), anyString());

		when(repo.claimReward(40)).thenReturn(1); // first claim wins
		svc.onHeartbeat(5, 2, true, 2);
		verify(rewards).award(eq(2), eq(1), eq(CreditTxType.STUDY_ATTENDANCE), anyString());
	}

	@Test
	void timeDoesNotCountWhenAloneOrTabHiddenOrNoLiveSession() {
		StudyRoomAttendanceRepository repo = mock(StudyRoomAttendanceRepository.class);
		StudyRoomRepository rooms = mock(StudyRoomRepository.class);
		StudyRoom r = room(5, user(1, "Host"), Visibility.PUBLIC, 10);
		r.setStatus(RoomStatus.LIVE);
		r.setSessionNo(1);
		when(rooms.findById(5)).thenReturn(Optional.of(r));
		when(repo.findByRoom_IdAndUser_IdAndSessionNo(5, 2, 1)).thenReturn(Optional.of(row(r, 0, Instant.now().minusSeconds(10))));
		StudyRoomAttendanceService svc = attendanceService(repo, rooms, mock(StudyRewardService.class));

		svc.onHeartbeat(5, 2, true, 1); // alone in the room
		svc.onHeartbeat(5, 2, false, 3); // tab not visible
		verify(repo, never()).addActive(anyInt(), anyInt(), any(), any());

		r.setStatus(RoomStatus.OPEN); // no live session
		svc.onHeartbeat(5, 2, true, 3);
		verify(repo, never()).addActive(anyInt(), anyInt(), any(), any());
	}

	// ------------------------------------------------------------ challenges

	private StudyRoomChallengeService challengeService(StudyRoomService roomService, StudyRoomChallengeRepository ch, StudyRoomSubmissionRepository sub, StudyRewardService rewards) {
		return new StudyRoomChallengeService(ch, sub, roomService, rewards, mock(UserRepository.class), mock(StudyRoomPresenceRegistry.class));
	}

	@Test
	void hostCannotSubmitToTheirOwnChallenge() {
		StudyRoomService roomService = mock(StudyRoomService.class);
		User host = user(1, "Host");
		when(roomService.requireRoom(5)).thenReturn(room(5, host, Visibility.PUBLIC, 10));
		StudyRoomChallengeService svc = challengeService(roomService, mock(StudyRoomChallengeRepository.class), mock(StudyRoomSubmissionRepository.class), mock(StudyRewardService.class));
		assertThatThrownBy(() -> svc.submit(1, 5, 9, "done")).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void approvalPaysExactlyOnce() {
		StudyRoomService roomService = mock(StudyRoomService.class);
		StudyRoomChallengeRepository ch = mock(StudyRoomChallengeRepository.class);
		StudyRoomSubmissionRepository sub = mock(StudyRoomSubmissionRepository.class);
		StudyRewardService rewards = mock(StudyRewardService.class);
		User host = user(1, "Host");
		StudyRoom r = room(5, host, Visibility.PUBLIC, 10);
		StudyRoomChallenge c = new StudyRoomChallenge();
		c.setId(9);
		c.setRoom(r);
		c.setTitle("5 SQL queries");
		c.setRewardCredits(2);
		StudyRoomSubmission s = new StudyRoomSubmission();
		s.setId(3);
		s.setChallenge(c);
		s.setUser(user(2, "Bob"));
		when(roomService.requireHost(5, 1)).thenReturn(r);
		when(ch.findByIdAndRoom_Id(9, 5)).thenReturn(Optional.of(c));
		when(sub.findByIdAndChallenge_Id(3, 9)).thenReturn(Optional.of(s));
		when(sub.findById(3)).thenReturn(Optional.of(s));
		when(rewards.award(eq(2), eq(2), eq(CreditTxType.STUDY_CHALLENGE), anyString())).thenReturn(2);
		StudyRoomChallengeService svc = challengeService(roomService, ch, sub, rewards);

		when(sub.review(eq(3), eq(SubmissionStatus.APPROVED), any(), any())).thenReturn(1);
		svc.review(1, 5, 9, 3, true);
		verify(rewards).award(eq(2), eq(2), eq(CreditTxType.STUDY_CHALLENGE), anyString());

		// a second approval (double click / two hosts' tabs) finds nothing PENDING: refused, no second payout
		when(sub.review(eq(3), eq(SubmissionStatus.APPROVED), any(), any())).thenReturn(0);
		assertThatThrownBy(() -> svc.review(1, 5, 9, 3, true)).isInstanceOf(StudyRoomException.class);
		verify(rewards, org.mockito.Mockito.times(1)).award(anyInt(), anyInt(), any(), anyString());
	}
}
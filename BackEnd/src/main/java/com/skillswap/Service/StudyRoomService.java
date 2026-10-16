package com.skillSwap.Service;

import com.skillSwap.Dto.common.PageResponse;
import com.skillSwap.Dto.studyroom.StudyRoomDtos.*;
import com.skillSwap.Entity.*;
import com.skillSwap.Entity.StudyRoomEnums.*;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Exception.StudyRoomException;
import com.skillSwap.Repository.*;
import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Rooms, membership, access rules and sessions. Every other study-room service asks THIS class "is this user allowed?". */
@Service
public class StudyRoomService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_CODE_TRIES = 5;
    private static final long CODE_WINDOW_MS = 10 * 60 * 1000L;

    private final StudyRoomRepository rooms;
    private final StudyRoomMemberRepository members;
    private final StudyRoomTimerRepository timers;
    private final StudyRoomNoteRepository notes;
    private final UserRepository users;
    private final StudyRoomPresenceRegistry registry;
    private final StudyRoomAttendanceService attendance;
    private final int maxRoomsPerHost;
    private final Map<String, Deque<Long>> failedCodes = new ConcurrentHashMap<>();

    public StudyRoomService(
            StudyRoomRepository rooms,
            StudyRoomMemberRepository members,
            StudyRoomTimerRepository timers,
            StudyRoomNoteRepository notes,
            UserRepository users,
            StudyRoomPresenceRegistry registry,
            StudyRoomAttendanceService attendance,
            @Value("${skillswap.study.max-rooms-per-host:5}") int maxRoomsPerHost
    ) {
        this.rooms = rooms;
        this.members = members;
        this.timers = timers;
        this.notes = notes;
        this.users = users;
        this.registry = registry;
        this.attendance = attendance;
        this.maxRoomsPerHost = maxRoomsPerHost;
    }

    public record SocketInfo(int maxParticipants, RoomStatus status) {}

    // =====================================================================
    // Access rules (used by every other study-room service)
    // =====================================================================

    public StudyRoom requireRoom(Integer roomId) {
        return rooms
                .findById(roomId)
                .filter(r -> r.getStatus() != RoomStatus.ARCHIVED)
                .orElseThrow(() -> new ResourceNotFoundException("Study room", "id", roomId));
    }

    public StudyRoomMember requireActiveMember(Integer roomId, Integer userId) {
        StudyRoomMember m = members
                .findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new AccessDeniedException("Join this room first"));
        if (m.getStatus() != MemberStatus.ACTIVE) throw new AccessDeniedException("You no longer have access to this room");
        return m;
    }

    public StudyRoom requireHost(Integer roomId, Integer userId) {
        StudyRoom r = requireRoom(roomId);
        if (!r.getHost().getId().equals(userId)) throw new AccessDeniedException("Only the room host can do this");
        return r;
    }

    /** Used by the websocket handshake: may this user open a live connection to this room? */
    @Transactional(readOnly = true)
    public boolean canConnect(Integer roomId, Integer userId) {
        return rooms
                .findById(roomId)
                .filter(r -> r.getStatus() != RoomStatus.ARCHIVED)
                .flatMap(r -> members.findByRoom_IdAndUser_Id(roomId, userId))
                .map(m -> m.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public SocketInfo socketInfo(Integer roomId) {
        StudyRoom r = requireRoom(roomId);
        return new SocketInfo(r.getMaxParticipants(), r.getStatus());
    }

    // =====================================================================
    // Create / edit / archive
    // =====================================================================

    @Transactional
    public RoomDetail create(Integer userId, RoomRequest req) {
        validate(req, true);
        if (rooms.countByHost_IdAndStatusNot(userId, RoomStatus.ARCHIVED) >= maxRoomsPerHost) {
            throw new StudyRoomException(HttpStatus.CONFLICT, "You can host at most " + maxRoomsPerHost + " active rooms. Archive one first.");
        }
        User host = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        StudyRoom r = new StudyRoom();
        r.setHost(host);
        apply(r, req);
        r.setAccessCode(req.visibility() == Visibility.PRIVATE ? generateCode() : null);
        r = rooms.save(r);

        StudyRoomMember m = new StudyRoomMember();
        m.setRoom(r);
        m.setUser(host);
        m.setRole(MemberRole.HOST);
        members.save(m);

        StudyRoomTimer t = new StudyRoomTimer();
        t.setRoom(r);
        t.setUpdatedAt(Instant.now());
        timers.save(t);

        StudyRoomNote n = new StudyRoomNote();
        n.setRoom(r);
        notes.save(n);

        return detail(userId, r.getId());
    }

    @Transactional
    public RoomDetail update(Integer userId, Integer roomId, RoomRequest req) {
        validate(req, false);
        StudyRoom r = requireHost(roomId, userId);
        int online = registry.count(roomId);
        if (req.maxParticipants() < online) {
            throw new IllegalArgumentException("Capacity cannot be lower than the " + online + " people in the room right now");
        }
        Visibility before = r.getVisibility();
        apply(r, req);
        if (req.visibility() == Visibility.PRIVATE && before == Visibility.PUBLIC) r.setAccessCode(generateCode());
        if (req.visibility() == Visibility.PUBLIC) r.setAccessCode(null);
        registry.broadcastAfterCommit(roomId, "room", roomEvent(r));
        return detail(userId, roomId);
    }

    /** "Delete" = archive: the room disappears for everyone but its history (messages, attendance) stays in the database. */
    @Transactional
    public void archive(Integer userId, Integer roomId) {
        StudyRoom r = requireRoom(roomId);
        boolean admin = users.findById(userId).map(u -> u.getRole() == Role.ADMIN).orElse(false);
        if (!admin && !r.getHost().getId().equals(userId)) throw new AccessDeniedException("Only the room host can do this");
        if (r.getStatus() == RoomStatus.LIVE) attendance.finalizeSession(roomId, r.getSessionNo());
        r.setStatus(RoomStatus.ARCHIVED);
        r.setSessionStartedAt(null);
        // a timer that is still running in an archived room must not keep firing
        timers.findByRoom_Id(roomId).ifPresent(t -> {
            t.setStatus(TimerStatus.IDLE);
            t.setEndsAt(null);
        });
        Integer id = r.getId();
        registry.broadcastAfterCommit(id, "room", roomEvent(r));
        afterCommit(() -> registry.closeRoom(id));
    }

    private void validate(RoomRequest req, boolean creating) {
        if (creating && req.scheduledStart() != null && req.scheduledStart().isBefore(Instant.now().minus(Duration.ofMinutes(5)))) {
            throw new IllegalArgumentException("Scheduled start time must not be in the past");
        }
    }

    private void apply(StudyRoom r, RoomRequest req) {
        r.setName(req.name().strip());
        r.setTopic(req.topic().strip());
        r.setDescription(req.description() == null || req.description().isBlank() ? null : req.description().strip());
        r.setDifficulty(req.difficulty());
        r.setMaxParticipants(req.maxParticipants());
        r.setVisibility(req.visibility());
        r.setScheduledStart(req.scheduledStart());
        r.setDurationMinutes(req.durationMinutes());
        r.setMembersCanPause(Boolean.TRUE.equals(req.membersCanPause()));
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        return sb.toString();
    }

    // =====================================================================
    // Discovery + details
    // =====================================================================

    @Transactional(readOnly = true)
    public PageResponse<RoomCard> list(
            Integer userId,
            String q,
            String topic,
            Difficulty difficulty,
            String filter,
            int page,
            int size
    ) {
        int safeSize = Math.max(1, Math.min(size, 50));
        int safePage = Math.max(0, page);
        List<StudyRoom> visible = rooms.findVisibleFor(userId, RoomStatus.ARCHIVED, Visibility.PUBLIC, MemberStatus.ACTIVE, PageRequest.of(0, 300));

        Map<Integer, StudyRoomTimer> timerByRoom = visible.isEmpty()
                ? Map.of()
                : timers
                .findByRoom_IdIn(visible.stream().map(StudyRoom::getId).toList())
                .stream()
                .collect(Collectors.toMap(t -> t.getRoom().getId(), Function.identity()));
        Set<Integer> myRooms = members.findRoomIdsByUser(userId, MemberStatus.ACTIVE);

        String needle = q == null ? "" : q.strip().toLowerCase();
        String topicNeedle = topic == null ? "" : topic.strip().toLowerCase();
        String mode = filter == null ? "ALL" : filter.strip().toUpperCase();
        Instant now = Instant.now();

        List<RoomCard> cards = new ArrayList<>();
        for (StudyRoom r : visible) {
            int online = registry.count(r.getId());
            if (!needle.isEmpty() && !(lower(r.getName()).contains(needle) || lower(r.getTopic()).contains(needle) || lower(r.getDescription()).contains(needle))) continue;
            if (!topicNeedle.isEmpty() && !lower(r.getTopic()).contains(topicNeedle)) continue;
            if (difficulty != null && r.getDifficulty() != difficulty) continue;
            switch (mode) {
                case "AVAILABLE" -> { if (online >= r.getMaxParticipants()) continue; }
                case "ACTIVE" -> { if (r.getStatus() != RoomStatus.LIVE && online == 0) continue; }
                case "SCHEDULED" -> { if (r.getScheduledStart() == null || !r.getScheduledStart().isAfter(now) || r.getStatus() != RoomStatus.OPEN) continue; }
                default -> {}
            }
            cards.add(card(r, online, timerByRoom.get(r.getId()), myRooms.contains(r.getId())));
        }

        int from = Math.min(safePage * safeSize, cards.size());
        int to = Math.min(from + safeSize, cards.size());
        return PageResponse.of(new PageImpl<>(cards.subList(from, to), PageRequest.of(safePage, safeSize), cards.size()));
    }

    @Transactional(readOnly = true)
    public RoomDetail detail(Integer userId, Integer roomId) {
        StudyRoom r = rooms.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Study room", "id", roomId));
        boolean host = r.getHost().getId().equals(userId);
        boolean member = members
                .findByRoom_IdAndUser_Id(roomId, userId)
                .map(m -> m.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
        // archived rooms and other people's private rooms look like they do not exist
        if (r.getStatus() == RoomStatus.ARCHIVED && !host) throw new ResourceNotFoundException("Study room", "id", roomId);
        if (r.getVisibility() == Visibility.PRIVATE && !member) throw new ResourceNotFoundException("Study room", "id", roomId);

        StudyRoomTimer t = timers.findByRoom_Id(roomId).orElse(null);
        User me = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return new RoomDetail(
                card(r, registry.count(roomId), t, member),
                r.isMembersCanPause(),
                r.getSessionNo(),
                r.getSessionStartedAt(),
                host ? r.getAccessCode() : null,
                new MeInfo(me.getId(), nameOf(me), host),
                t == null ? null : StudyRoomTimerService.view(t, Instant.now()),
                attendance.requiredSeconds() / 60
        );
    }

    // =====================================================================
    // Join / leave / moderation
    // =====================================================================

    @Transactional
    public RoomDetail join(Integer userId, Integer roomId, String code) {
        StudyRoom r = requireRoom(roomId);
        StudyRoomMember m = members.findByRoom_IdAndUser_Id(roomId, userId).orElse(null);
        if (m != null && m.getStatus() == MemberStatus.REMOVED) throw new AccessDeniedException("The host removed you from this room");
        if (m == null) {
            if (r.getVisibility() == Visibility.PRIVATE) checkCode(userId, roomId, r.getAccessCode(), code);
            if (!registry.userIds(roomId).contains(userId) && registry.count(roomId) >= r.getMaxParticipants()) {
                throw new StudyRoomException(HttpStatus.CONFLICT, "This room is full right now");
            }
            m = new StudyRoomMember();
            m.setRoom(r);
            m.setUser(users.getReferenceById(userId));
            members.save(m);
        }
        return detail(userId, roomId);
    }

    private void checkCode(Integer userId, Integer roomId, String real, String given) {
        String key = userId + ":" + roomId;
        Deque<Long> tries = failedCodes.computeIfAbsent(key, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        synchronized (tries) {
            while (!tries.isEmpty() && now - tries.peekFirst() > CODE_WINDOW_MS) tries.pollFirst();
            if (tries.size() >= MAX_CODE_TRIES) {
                throw new StudyRoomException(HttpStatus.TOO_MANY_REQUESTS, "Too many wrong codes. Try again in a few minutes.");
            }
            boolean ok = real != null && given != null && constantTimeEquals(real, given.strip().toUpperCase());
            if (!ok) {
                tries.addLast(now);
                throw new AccessDeniedException("Invalid access code");
            }
            tries.clear();
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(
                a.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                b.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    @Transactional
    public void leave(Integer userId, Integer roomId) {
        StudyRoom r = requireRoom(roomId);
        if (r.getHost().getId().equals(userId)) throw new IllegalArgumentException("The host cannot leave. Archive the room instead.");
        StudyRoomMember m = members.findByRoom_IdAndUser_Id(roomId, userId).orElse(null);
        if (m == null) return;
        if (m.getStatus() == MemberStatus.ACTIVE) members.delete(m); // a REMOVED row is kept so the block stays
        afterCommit(() -> registry.kickUser(roomId, userId));
    }

    @Transactional(readOnly = true)
    public List<MemberView> members(Integer userId, Integer roomId) {
        requireHost(roomId, userId);
        return members
                .findAllForRoom(roomId)
                .stream()
                .map(m -> new MemberView(m.getUser().getId(), nameOf(m.getUser()), m.getRole(), m.getStatus(), m.getJoinedAt()))
                .toList();
    }

    @Transactional
    public void removeMember(Integer hostId, Integer roomId, Integer targetId) {
        requireHost(roomId, hostId);
        if (hostId.equals(targetId)) throw new IllegalArgumentException("You cannot remove yourself");
        StudyRoomMember m = members
                .findByRoom_IdAndUser_Id(roomId, targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "userId", targetId));
        m.setStatus(MemberStatus.REMOVED);
        afterCommit(() -> registry.kickUser(roomId, targetId));
    }

    @Transactional
    public void restoreMember(Integer hostId, Integer roomId, Integer targetId) {
        requireHost(roomId, hostId);
        StudyRoomMember m = members
                .findByRoom_IdAndUser_Id(roomId, targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "userId", targetId));
        m.setStatus(MemberStatus.ACTIVE);
    }

    // =====================================================================
    // Sessions (what attendance is measured against)
    // =====================================================================

    @Transactional
    public RoomDetail startSession(Integer userId, Integer roomId) {
        StudyRoom r = requireHost(roomId, userId);
        if (r.getStatus() != RoomStatus.OPEN) throw new StudyRoomException(HttpStatus.CONFLICT, "A session is already running");
        r.setStatus(RoomStatus.LIVE);
        r.setSessionNo(r.getSessionNo() + 1);
        r.setSessionStartedAt(Instant.now());
        registry.broadcastAfterCommit(roomId, "room", roomEvent(r));
        return detail(userId, roomId);
    }

    @Transactional
    public RoomDetail endSession(Integer userId, Integer roomId) {
        StudyRoom r = requireHost(roomId, userId);
        endInternal(r);
        return detail(userId, roomId);
    }

    /** Called every 15s by StudyRoomScheduler: ends sessions that ran past the room's duration. */
    @Transactional
    public void autoEndExpiredSessions() {
        Instant now = Instant.now();
        for (StudyRoom r : rooms.findByStatus(RoomStatus.LIVE)) {
            if (r.getDurationMinutes() != null && r.getSessionStartedAt() != null &&
                    !r.getSessionStartedAt().plus(Duration.ofMinutes(r.getDurationMinutes())).isAfter(now)) {
                endInternal(r);
            }
        }
    }

    private void endInternal(StudyRoom r) {
        if (r.getStatus() != RoomStatus.LIVE) throw new StudyRoomException(HttpStatus.CONFLICT, "No session is running");
        attendance.finalizeSession(r.getId(), r.getSessionNo());
        r.setStatus(RoomStatus.OPEN);
        r.setSessionStartedAt(null);
        registry.broadcastAfterCommit(r.getId(), "room", roomEvent(r));
    }

    // =====================================================================
    // Live presence
    // =====================================================================

    @Transactional(readOnly = true)
    public List<PresenceEntry> presence(Integer roomId) {
        Set<Integer> ids = registry.userIds(roomId);
        if (ids.isEmpty()) return List.of();
        Integer hostId = rooms.findById(roomId).map(r -> r.getHost().getId()).orElse(null);
        return users
                .findAllById(ids)
                .stream()
                .map(u -> new PresenceEntry(u.getId(), nameOf(u), u.getId().equals(hostId)))
                .sorted(Comparator.comparing(PresenceEntry::host).reversed().thenComparing(p -> p.name().toLowerCase()))
                .toList();
    }

    public void broadcastPresence(Integer roomId) {
        registry.broadcast(roomId, "presence", presence(roomId));
    }

    // =====================================================================
    // helpers
    // =====================================================================

    private RoomCard card(StudyRoom r, int online, StudyRoomTimer t, boolean member) {
        String activity;
        if (r.getStatus() == RoomStatus.LIVE) activity = "Study session in progress";
        else if (t != null && t.getStatus() == TimerStatus.RUNNING) activity = t.getPhase() == TimerPhase.FOCUS ? "Focus timer running" : "On a break";
        else if (online > 0) activity = "People are studying here";
        else activity = "Waiting for learners";
        User h = r.getHost();
        return new RoomCard(
                r.getId(), r.getName(), r.getTopic(), r.getDescription(), r.getDifficulty(), r.getVisibility(),
                r.getStatus(), r.getMaxParticipants(), online, new HostRef(h.getId(), nameOf(h)),
                r.getScheduledStart(), r.getDurationMinutes(), activity, member
        );
    }

    private RoomEvent roomEvent(StudyRoom r) {
        return new RoomEvent(r.getStatus(), r.getSessionNo(), r.getSessionStartedAt());
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public static String nameOf(User u) {
        return u.getName() == null || u.getName().isBlank() ? "Learner" : u.getName();
    }

    private void afterCommit(Runnable task) {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        task.run();
                    }
                }
        );
    }
}
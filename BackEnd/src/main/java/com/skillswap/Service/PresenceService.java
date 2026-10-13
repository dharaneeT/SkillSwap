package com.skillSwap.Service;

import com.skillSwap.Dto.presence.PresenceDTO;
import com.skillSwap.Entity.UserPresence;
import com.skillSwap.Repository.UserPresenceRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {

	private static final Duration ONLINE_WINDOW = Duration.ofSeconds(75); // 2 missed heartbeats + slack
	private static final Duration PERSIST_EVERY = Duration.ofMinutes(1); // limit DB writes
	private static final int MAX_IDS = 50;

	// "online" is answered from memory (fast). The database only keeps "last seen" for after restarts.
	private final Map<Integer, Instant> lastSeen = new ConcurrentHashMap<>();
	private final Map<Integer, Instant> lastPersisted = new ConcurrentHashMap<>();

	private final UserPresenceRepository repository;
	private final CurrentUserService currentUserService;

	public PresenceService(UserPresenceRepository repository, CurrentUserService currentUserService) {
		this.repository = repository;
		this.currentUserService = currentUserService;
	}

	/** Called every ~30s by the logged-in user's browser. */
	public void heartbeat() {
		Integer id = currentUserService.getCurrentUser().getId();
		Instant now = Instant.now();
		lastSeen.put(id, now);

		Instant persisted = lastPersisted.get(id);
		if (persisted == null || Duration.between(persisted, now).compareTo(PERSIST_EVERY) >= 0) {
			repository.save(new UserPresence(id, now));
			lastPersisted.put(id, now);
		}
	}

	public List<PresenceDTO> getPresence(List<Integer> ids) {
		List<Integer> wanted = ids.stream().filter(Objects::nonNull).distinct().limit(MAX_IDS).toList();

		Map<Integer, Instant> stored = repository
			.findAllById(wanted)
			.stream()
			.filter(p -> p.getLastSeenAt() != null)
			.collect(Collectors.toMap(UserPresence::getUserId, UserPresence::getLastSeenAt));

		Instant now = Instant.now();
		return wanted
			.stream()
			.map(id -> {
				Instant mem = lastSeen.get(id);
				boolean online = mem != null && Duration.between(mem, now).compareTo(ONLINE_WINDOW) <= 0;
				return new PresenceDTO(id, online, mem != null ? mem : stored.get(id));
			})
			.toList();
	}
}

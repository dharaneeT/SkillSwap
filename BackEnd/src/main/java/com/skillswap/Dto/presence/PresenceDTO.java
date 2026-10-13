package com.skillSwap.Dto.presence;

import java.time.Instant;

public record PresenceDTO(Integer userId, boolean online, Instant lastSeenAt) {}

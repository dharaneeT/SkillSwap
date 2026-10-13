package com.skillSwap.Dto.certificate;

import java.time.LocalDateTime;

public record CertificateDTO(
	String code,
	String learnerName,
	String providerName,
	String skillName,
	int sessionsCompleted,
	LocalDateTime issuedAt
) {}

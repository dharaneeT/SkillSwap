package com.skillSwap.Controllers;

import com.skillSwap.Dto.certificate.CertificateDTO;
import com.skillSwap.Dto.progress.ProgressDTO;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Service.CertificateService;
import com.skillSwap.Service.ProgressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1")
@Tag(name = "Progress API", description = "Learner stats, streaks and certificates")
public class ProgressController {

	private final ProgressService progressService;
	private final CertificateService certificateService;

	public ProgressController(ProgressService progressService, CertificateService certificateService) {
		this.progressService = progressService;
		this.certificateService = certificateService;
	}

	@GetMapping("/progress/me")
	public ResponseEntity<ApiResponse<ProgressDTO>> myProgress() {
		return ResponseEntity.ok(new ApiResponse<>(true, "My progress", progressService.getMyProgress()));
	}

	@GetMapping("/certificates/my")
	public ResponseEntity<ApiResponse<List<CertificateDTO>>> myCertificates() {
		return ResponseEntity.ok(new ApiResponse<>(true, "My certificates", certificateService.myCertificates()));
	}

	// any logged-in user can verify a code; it only exists if the sessions really happened
	@GetMapping("/certificates/{code}")
	public ResponseEntity<ApiResponse<CertificateDTO>> certificate(@PathVariable String code) {
		return ResponseEntity.ok(new ApiResponse<>(true, "Certificate", certificateService.verify(code)));
	}
}

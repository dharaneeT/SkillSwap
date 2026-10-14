package com.skillSwap.Controllers;

import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.roadmap.RoadmapDtos.*;
import com.skillSwap.Security.CurrentUserService;
import com.skillSwap.Service.GeminiRoadmapService;
import com.skillSwap.Service.RoadmapService;
import com.skillSwap.Service.RoadmapTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/roadmaps")
@Tag(name = "Roadmap API", description = "Personalised learning roadmaps")
public class RoadmapController {

	private final RoadmapService roadmapService;
	private final GeminiRoadmapService geminiService;
	private final RoadmapTemplateService templateService;
	private final CurrentUserService currentUserService;

	public RoadmapController(
		RoadmapService roadmapService,
		GeminiRoadmapService geminiService,
		RoadmapTemplateService templateService,
		CurrentUserService currentUserService
	) {
		this.roadmapService = roadmapService;
		this.geminiService = geminiService;
		this.templateService = templateService;
		this.currentUserService = currentUserService;
	}

	// ---- generation (nothing is saved here; the user reviews first) ----

	@PostMapping("/generate")
	public ResponseEntity<ApiResponse<Draft>> generate(@Valid @RequestBody GenerateRequest req) {
		Integer userId = currentUserService.getCurrentUser().getId();
		return ok("Roadmap generated. Review it before saving.", geminiService.generate(userId, req));
	}

	@GetMapping("/templates")
	public ResponseEntity<ApiResponse<List<TemplateInfo>>> templates() {
		return ok("Templates", templateService.list());
	}

	@GetMapping("/templates/{templateId}/draft")
	public ResponseEntity<ApiResponse<Draft>> templateDraft(
		@PathVariable String templateId,
		@RequestParam(required = false) String skill
	) {
		return ok("Template draft", templateService.draft(templateId, skill));
	}

	// ---- dashboard / list / detail ----

	@GetMapping("/dashboard")
	public ResponseEntity<ApiResponse<DashboardView>> dashboard() {
		return ok("Roadmap dashboard", roadmapService.dashboard());
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<RoadmapView>>> list() {
		return ok("My roadmaps", roadmapService.list());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<RoadmapView>> get(@PathVariable Long id) {
		return ok("Roadmap", roadmapService.get(id));
	}

	// ---- save / edit / delete ----

	@PostMapping
	public ResponseEntity<ApiResponse<RoadmapView>> create(@Valid @RequestBody Draft draft) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(new ApiResponse<>(true, "Roadmap saved", roadmapService.create(draft)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<RoadmapView>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateRequest req
	) {
		return ok("Roadmap updated", roadmapService.update(id, req));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		roadmapService.delete(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Roadmap deleted", null));
	}

	@PutMapping("/{id}/milestones/{milestoneId}")
	public ResponseEntity<ApiResponse<RoadmapView>> updateMilestone(
		@PathVariable Long id,
		@PathVariable Long milestoneId,
		@Valid @RequestBody MilestoneUpdateRequest req
	) {
		return ok("Milestone updated", roadmapService.updateMilestone(id, milestoneId, req));
	}

	// ---- progress ----

	@PatchMapping("/{id}/milestones/{milestoneId}")
	public ResponseEntity<ApiResponse<RoadmapView>> setMilestone(
		@PathVariable Long id,
		@PathVariable Long milestoneId,
		@Valid @RequestBody CompletionRequest req
	) {
		return ok("Milestone updated", roadmapService.setMilestoneCompleted(id, milestoneId, req.getCompleted()));
	}

	@PatchMapping("/{id}/tasks/{taskId}")
	public ResponseEntity<ApiResponse<RoadmapView>> setTask(
		@PathVariable Long id,
		@PathVariable Long taskId,
		@Valid @RequestBody CompletionRequest req
	) {
		return ok("Task updated", roadmapService.setTaskCompleted(id, taskId, req.getCompleted()));
	}

	private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
		return ResponseEntity.ok(new ApiResponse<>(true, message, data));
	}
}

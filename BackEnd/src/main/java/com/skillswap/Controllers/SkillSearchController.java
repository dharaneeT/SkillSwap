package com.skillSwap.Controllers;

import com.skillSwap.Dto.common.PageResponse;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.skill.SkillSearchQuery;
import com.skillSwap.Dto.skill.SkillSearchResultDTO;
import com.skillSwap.Entity.SkillType;
import com.skillSwap.Service.SkillSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/skills")
@Tag(name = "Skill Search API", description = "Search, filter, sort and page skills")
public class SkillSearchController {

	private static final int MAX_SIZE = 50;
	private static final List<String> SORT_FIELDS = List.of("name", "id");

	private final SkillSearchService searchService;

	public SkillSearchController(SkillSearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<SkillSearchResultDTO>>> search(
		@RequestParam(required = false) String search,
		@RequestParam(required = false) SkillType type,
		@RequestParam(required = false) Double minRating,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "name,asc") String sort
	) {
		if (page < 0) throw new IllegalArgumentException("page must be 0 or greater");
		if (size < 1 || size > MAX_SIZE) throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
		if (minRating != null && (minRating < 0 || minRating > 5)) throw new IllegalArgumentException(
			"minRating must be between 0 and 5"
		);

		String term = search == null ? null : search.trim().toLowerCase(Locale.ROOT);
		if (term != null && term.isEmpty()) term = null;
		if (term != null && term.length() > 50) throw new IllegalArgumentException("search is too long (max 50)");

		String[] parts = sort.split(",");
		String field = parts[0].trim();
		if (!SORT_FIELDS.contains(field)) throw new IllegalArgumentException(
			"sort field must be one of " + SORT_FIELDS
		);
		boolean asc = true;
		if (parts.length > 1) {
			String dir = parts[1].trim().toLowerCase(Locale.ROOT);
			if (dir.equals("desc")) asc = false; else if (!dir.equals("asc")) throw new IllegalArgumentException(
				"sort direction must be asc or desc"
			);
		}

		SkillSearchQuery q = new SkillSearchQuery(term, type, minRating, page, size, field, asc);

		long t0 = System.nanoTime();
		PageResponse<SkillSearchResultDTO> result = searchService.search(q); // cache hit or DB
		double ms = (System.nanoTime() - t0) / 1_000_000.0;

		return ResponseEntity
			.ok()
			.header("X-Search-Time-Ms", String.format(Locale.ROOT, "%.3f", ms))
			.body(new ApiResponse<>(true, "Skills", result));
	}
}

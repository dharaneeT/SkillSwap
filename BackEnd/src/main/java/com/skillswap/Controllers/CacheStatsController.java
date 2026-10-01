package com.skillSwap.Controllers;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.skillSwap.Dto.response.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/admin/cache/skill-search")
public class CacheStatsController {

	private final CacheManager cacheManager;

	public CacheStatsController(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
		Map<String, Object> out = new LinkedHashMap<>();
		Cache cache = cacheManager.getCache("skillSearch");
		if (cache instanceof CaffeineCache cc) {
			CacheStats s = cc.getNativeCache().stats();
			out.put("enabled", true);
			out.put("entries", cc.getNativeCache().estimatedSize());
			out.put("hits", s.hitCount());
			out.put("misses", s.missCount());
			out.put("hitRate", s.requestCount() == 0 ? 0.0 : s.hitRate());
		} else {
			out.put("enabled", false);
		}
		return ResponseEntity.ok(new ApiResponse<>(true, "skillSearch cache", out));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<String>> clear() {
		Cache cache = cacheManager.getCache("skillSearch");
		if (cache != null) cache.clear();
		return ResponseEntity.ok(new ApiResponse<>(true, "Cleared", "OK"));
	}
}

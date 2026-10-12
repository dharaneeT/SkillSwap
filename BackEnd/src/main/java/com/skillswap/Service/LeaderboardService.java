package com.skillSwap.Service;

import com.skillSwap.Dto.leaderboard.LeaderboardEntryDTO;
import com.skillSwap.Entity.User;
import com.skillSwap.Repository.ReviewRepository;
import com.skillSwap.Repository.ReviewRepository.RatingStat;
import com.skillSwap.Repository.SessionRepository;
import com.skillSwap.Security.CurrentUserService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaderboardService {

	private static final int MAX_LIMIT = 100;

	private final SessionRepository sessionRepository;
	private final ReviewRepository reviewRepository;
	private final CurrentUserService currentUserService;

	public LeaderboardService(
		SessionRepository sessionRepository,
		ReviewRepository reviewRepository,
		CurrentUserService currentUserService
	) {
		this.sessionRepository = sessionRepository;
		this.reviewRepository = reviewRepository;
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public List<LeaderboardEntryDTO> getLeaderboard(int limit) {
		int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
		return buildRanked().stream().limit(safeLimit).toList();
	}

	@Transactional(readOnly = true)
	public LeaderboardEntryDTO getMyStanding() {
		User me = currentUserService.getCurrentUser();
		return buildRanked()
			.stream()
			.filter(e -> e.getUserId().equals(me.getId()))
			.findFirst()
			.orElseGet(() -> new LeaderboardEntryDTO(null, me.getId(), me.getName(), 0L, 0.0, 0L));
	}

	private List<LeaderboardEntryDTO> buildRanked() {
		Map<Integer, RatingStat> ratings = reviewRepository
			.ratingStatsByProvider()
			.stream()
			.collect(Collectors.toMap(RatingStat::getUserId, Function.identity()));

		Comparator<LeaderboardEntryDTO> order = Comparator
			.comparing(LeaderboardEntryDTO::getSessionsTaught)
			.reversed()
			.thenComparing(Comparator.comparing(LeaderboardEntryDTO::getAverageRating).reversed())
			.thenComparing(Comparator.comparing(LeaderboardEntryDTO::getReviewCount).reversed())
			.thenComparing((LeaderboardEntryDTO e) -> e.getName() == null ? "" : e.getName().toLowerCase());

		List<LeaderboardEntryDTO> rows = new ArrayList<>(
			sessionRepository
				.countCompletedByProvider()
				.stream()
				.map(t -> {
					RatingStat r = ratings.get(t.getUserId());
					double avg = (r == null || r.getAvgRating() == null)
						? 0.0
						: Math.round(r.getAvgRating() * 10) / 10.0;
					long reviews = (r == null || r.getReviewCount() == null) ? 0L : r.getReviewCount();
					return new LeaderboardEntryDTO(null, t.getUserId(), t.getName(), t.getTaught(), avg, reviews);
				})
				.sorted(order)
				.toList()
		);

		// ties share a rank (1, 1, 3 ...)
		for (int i = 0; i < rows.size(); i++) {
			LeaderboardEntryDTO cur = rows.get(i);
			if (i == 0) {
				cur.setRank(1);
				continue;
			}
			LeaderboardEntryDTO prev = rows.get(i - 1);
			boolean tie =
				prev.getSessionsTaught().equals(cur.getSessionsTaught()) &&
				prev.getAverageRating().equals(cur.getAverageRating()) &&
				prev.getReviewCount().equals(cur.getReviewCount());
			cur.setRank(tie ? prev.getRank() : i + 1);
		}
		return rows;
	}
}

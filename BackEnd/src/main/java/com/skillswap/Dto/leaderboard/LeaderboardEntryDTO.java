package com.skillSwap.Dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDTO {

    private Integer rank; // null = hasn't completed a session yet
    private Integer userId;
    private String name;
    private Long sessionsTaught; // COMPLETED sessions as provider
    private Double averageRating; // 0.0 when reviewCount == 0
    private Long reviewCount;
}
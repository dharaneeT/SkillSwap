package com.skillSwap.skillswap.Dto.Match;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDTO {
    private Integer userId;
    private String userName;
    private String matchedSkill;
}

package com.skillSwap.skillswap.Dto.errorResponse;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
    private boolean success;
    private String message;
    private int status;
    private LocalDateTime timestamp;
}

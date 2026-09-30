package com.skillSwap.Dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private String message;
	private T data;
}

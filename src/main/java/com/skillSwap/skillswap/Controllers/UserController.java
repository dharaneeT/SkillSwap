package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.user.UserProfileDTO;
import com.skillSwap.skillswap.Dto.user.UserRequestDTO;
import com.skillSwap.skillswap.Dto.user.UserResponseDTO;
import com.skillSwap.skillswap.Exception.UserNotFoundException;
import com.skillSwap.skillswap.Service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/users")
public class UserController {

	@Autowired
	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	//CREATE USER
	@PostMapping("/add")
	public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserRequestDTO dto) {
		UserResponseDTO data = userService.createUser(dto);

		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User created", data));
	}

	//GET ALL USERS
	@GetMapping("/get")
	public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
		return ResponseEntity.ok(new ApiResponse<>(true, "All users", userService.getAllUsers()));
	}

	// GET USER BY ID
	@GetMapping("/{id}")
	public ResponseEntity<?> getUser(@PathVariable Integer id) {
		try {
			UserResponseDTO user = userService.getUserById(id);

			return ResponseEntity.ok(new ApiResponse<>(true, "User fetched", user));
		} catch (UserNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, ex.getMessage(), null));
		} catch (Exception ex) {
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse<>(false, "Something went wrong", null));
		}
	}

	//PROFILE
	@GetMapping("/profile/{id}")
	public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile(@PathVariable Integer id) {
		return ResponseEntity.ok(new ApiResponse<>(true, "User profile", userService.getUserProfile(id)));
	}
}

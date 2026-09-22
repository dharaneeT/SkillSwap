package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.user.UserRequestDTO;
import com.skillSwap.skillswap.Dto.user.UserResponseDTO;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
	public ApiResponse<UserResponseDTO> createUser(@RequestBody UserRequestDTO dto) {
		return new ApiResponse<>(true, "User created", userService.createUser(dto));
	}

	//GET ALL USERS
	@GetMapping("/get")
	public ApiResponse<List<UserResponseDTO>> getAllUsers() {
		return new ApiResponse<>(true, "All users", userService.getAllUsers());
	}

	// GET USER BY ID
	@GetMapping("/{id}")
	public ApiResponse<UserResponseDTO> getUser(@PathVariable Integer id) {
		return new ApiResponse<>(true, "User found", userService.getUserById(id));
	}
}

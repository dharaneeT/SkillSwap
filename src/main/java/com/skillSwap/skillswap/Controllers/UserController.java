package com.skillSwap.skillswap.Controllers;

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

	@PostMapping("/add")
	public User createUser(@RequestBody User user) {
		return userService.createUser(user);
	}

	@GetMapping("/get")
	public List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	@GetMapping("get/{id}")
	public User getUserById(@PathVariable Integer id) {
		return userService.getUserById(id);
	}
}

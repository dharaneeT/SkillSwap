package com.skillswap.Controllers;

import com.skillswap.Dto.Auth.AuthRequestDTO;
import com.skillswap.Dto.Auth.AuthResponseDTO;
import com.skillswap.Dto.Auth.SignupRequestDTO;
import com.skillswap.Dto.response.ApiResponse;
import com.skillswap.Service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/skillswap/v1/auth")
@Tag(name = "Auth API", description = "Signup and login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> signup(@Valid @RequestBody SignupRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Signup successful", authService.signup(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", authService.login(req)));
    }
}
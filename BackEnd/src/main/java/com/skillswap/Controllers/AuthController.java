package com.skillSwap.Controllers;

import com.skillSwap.Dto.Auth.AuthRequestDTO;
import com.skillSwap.Dto.Auth.AuthResponseDTO;
import com.skillSwap.Dto.Auth.SignupRequestDTO;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Service.AuthService;
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

//User sends signup request
//        ↓
//AuthController.signup()
//        ↓
//AuthService.signup()
//        ↓
//User saved in DB
//        ↓
//Wallet credit added
//        ↓
//UserRegisteredEvent published
//        ↓
//NotificationListener catches event
//        ↓
//EmailService.send()
//        ↓
//JavaMailSender → SMTP server
//        ↓
//Email delivered
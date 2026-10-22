package com.example.demo.controller;


import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource
    .authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of(
                "message", "Đăng ký thành công"
            ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
            authService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        JwtAuthenticationToken jwtAuth =
            (JwtAuthenticationToken) authentication;

        return ResponseEntity.ok(Map.of(
            "userId", jwtAuth.getToken().getSubject(),
            "username", jwtAuth.getToken().getClaimAsString("username"),
            "role", jwtAuth.getToken().getClaimAsString("role")
        ));
    }
}

// import com.example.demo.dto.LoginRequest;
// import com.example.demo.dto.LoginResponse;
// import com.example.demo.service.AuthService;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
// public class AuthController {

//     private final AuthService authService;

//     public AuthController(AuthService authService) {
//         this.authService = authService;
//     }

//     @PostMapping("/login")
//     public ResponseEntity<LoginResponse> login(
//             @RequestBody LoginRequest request) {

//         LoginResponse response = authService.login(request);

//         if (!response.isSuccess()) {
//             HttpStatus status =
//                 response.getMessage().contains("không được để trống")
//                     ? HttpStatus.BAD_REQUEST
//                     : HttpStatus.UNAUTHORIZED;

//             return ResponseEntity.status(status).body(response);
//         }

//         return ResponseEntity.ok(response);
//     }
// }
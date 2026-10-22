 package com.example.demo.service;


import java.util.Locale;

import org.springframework.security.authentication
    .BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.repository.AuthRepository;

@Service
public class AuthService {

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AuthRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email()
            .trim()
            .toLowerCase(Locale.ROOT);

        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                "Username đã tồn tại"
            );
        }

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "Email đã được sử dụng"
            );
        }

        String passwordHash = passwordEncoder.encode(
            request.password()
        );

        repository.create(
            username,
            email,
            passwordHash,
            request.fullName().trim()
        );
    }

    public AuthResponse login(LoginRequest request) {
        AuthRepository.Account account = repository
            .findByUsernameOrEmail(
                request.getUsername().trim()
            )
            .orElseThrow(() ->
                new BadCredentialsException(
                    "Thông tin đăng nhập không chính xác"
                )
            );

        if (!"ACTIVE".equalsIgnoreCase(account.status())) {
            throw new BadCredentialsException(
                "Tài khoản không hoạt động"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.passwordHash())) {

            throw new BadCredentialsException(
                "Thông tin đăng nhập không chính xác"
            );
        }

        String token = jwtService.generateToken(account);

        return new AuthResponse(
            token,
            "Bearer",
            jwtService.getExpirationSeconds(),
            new AuthResponse.UserInfo(
                account.userId(),
                account.username(),
                account.email(),
                account.role()
            )
        );
    }
}

// import com.example.demo.dto.LoginRequest;
// import com.example.demo.dto.LoginResponse;
// import com.example.demo.model.UserModel;
// import com.example.demo.repository.UserRepository;
// import org.springframework.stereotype.Service;

// @Service
// public class AuthService {

//     private final UserRepository userRepository;


//     public AuthService(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }

//     public LoginResponse login(LoginRequest request) {
//         if (request == null
//                 || request.getUsername() == null
//                 || request.getUsername().isBlank()
//                 || request.getPassword() == null
//                 || request.getPassword().isBlank()) {
//             return new LoginResponse(
//                 false, "Username và password không được để trống", null
//             );
//         }

//         UserModel user = userRepository
//             .findByUsername(request.getUsername().trim())
//             .orElse(null);

//         if (user == null
//                 || user.getPassword() == null
//                 || ! request.getPassword().equals( user.getPassword())) {
//             return new LoginResponse(
//                 false, "Username hoặc password không đúng", null
//             );
//         }

//         return new LoginResponse(
//             true,
//             "Đăng nhập thành công",
//             user.getUsername()
//         );
//     }
// }
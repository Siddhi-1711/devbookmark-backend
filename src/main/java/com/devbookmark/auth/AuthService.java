package com.devbookmark.auth;

import com.devbookmark.auth.dto.AuthResponse;
import com.devbookmark.auth.dto.LoginRequest;
import com.devbookmark.auth.dto.RegisterRequest;
import com.devbookmark.security.JwtService;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = User.builder()
                .name(req.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token,
                new AuthResponse.UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        if (user.isBanned()) {
            String reason = user.getBanReason() != null
                    ? "Your account has been suspended: " + user.getBanReason()
                    : "Your account has been suspended.";
            throw new IllegalArgumentException(reason);
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token,
                new AuthResponse.UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
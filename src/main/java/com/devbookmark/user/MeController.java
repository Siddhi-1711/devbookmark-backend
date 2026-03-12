package com.devbookmark.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/me")
    public MeResponse me(Authentication authentication) {
        if (authentication == null) throw new IllegalArgumentException("Not authenticated.");

        UUID userId = UUID.fromString(authentication.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return new MeResponse(user.getId(), user.getName(), user.getEmail());
    }

    public record MeResponse(UUID id, String name, String email) {}
}
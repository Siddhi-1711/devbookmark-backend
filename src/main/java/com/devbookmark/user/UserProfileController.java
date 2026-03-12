package com.devbookmark.user;

import com.devbookmark.security.AuthUser;
import com.devbookmark.user.dto.UserPublicProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileController(UserProfileService userProfileService,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userProfileService = userProfileService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{userId}")
    public UserPublicProfileResponse publicProfile(@PathVariable UUID userId) {
        return userProfileService.publicProfile(userId);
    }

    /** Returns the full current user object — used by frontend on app load to stay in sync */
    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        User u = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toMeResponse(u);
    }

    /** Update profile fields: name, username, bio, avatarUrl */
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(Authentication auth,
                                           @RequestBody UpdateProfileRequest body) {
        UUID me = AuthUser.requireUserId(auth);
        User user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (body.name() != null) {
            String name = body.name().trim();
            if (name.isBlank()) return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name cannot be empty"));
            user.setName(name);
        }

        if (body.username() != null) {
            String username = body.username().trim().toLowerCase()
                    .replaceAll("[^a-z0-9_]", "");
            if (!username.isBlank()) {
                // Check uniqueness (skip if same user already has this username)
                boolean taken = userRepository.existsByUsernameAndIdNot(username, me);
                if (taken) return ResponseEntity.badRequest()
                        .body(Map.of("message", "Username already taken"));
                user.setUsername(username);
            }
        }

        if (body.bio() != null) {
            String bio = body.bio().trim();
            user.setBio(bio.isBlank() ? null : bio);
        }

        if (body.avatarUrl() != null) {
            String url = body.avatarUrl().trim();
            user.setAvatarUrl(url.isBlank() ? null : url);
        }

        userRepository.save(user);
        return ResponseEntity.ok(toMeResponse(user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(Authentication auth,
                                            @RequestBody Map<String, String> body) {
        UUID me = AuthUser.requireUserId(auth);
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must be at least 6 characters"));

        User user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Current password is incorrect"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        userRepository.deleteById(me);
        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }

    private MeResponse toMeResponse(User u) {
        return new MeResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getUsername(),
                u.getBio(),
                u.getAvatarUrl()
        );
    }

    public record MeResponse(
            UUID id,
            String name,
            String email,
            String username,
            String bio,
            String avatarUrl
    ) {}

    public record UpdateProfileRequest(
            String name,
            String username,
            String bio,
            String avatarUrl
    ) {}
}
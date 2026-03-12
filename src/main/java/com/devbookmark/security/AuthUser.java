package com.devbookmark.security;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public class AuthUser {
    private AuthUser() {}

    public static UUID requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Not authenticated.");
        }
        return UUID.fromString(authentication.getName());
    }
    public static UUID maybeUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;

        String name = authentication.getName();
        if (name.equals("anonymousUser")) return null;

        try { return UUID.fromString(name); }
        catch (Exception e) { return null; }
    }
}
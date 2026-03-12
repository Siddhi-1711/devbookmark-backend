package com.devbookmark.security;

import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                UUID userId = jwtService.getUserId(token);

                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {

                    // Reject banned users immediately — they cannot make any request
                    if (user.isBanned()) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType("application/json");
                        response.getWriter().write(
                                "{\"error\":\"Your account has been suspended.\"}"
                        );
                        return;
                    }

                    request.setAttribute("userId", userId);

                    // Use the real role from DB so @PreAuthorize("hasRole('ADMIN')") works
                    String authority = "ROLE_" + user.getRole().name(); // ROLE_USER or ROLE_ADMIN

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            user.getId().toString(),
                            null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException ignored) {
                // invalid token → stays unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
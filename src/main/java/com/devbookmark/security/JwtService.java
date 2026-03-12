package com.devbookmark.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        if (props.secret() == null || props.secret().length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long.");
        }
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.expirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("email", email))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parse(token).getBody().getSubject());
    }

    public String getEmail(String token) {
        return parse(token).getBody().get("email", String.class);
    }
}
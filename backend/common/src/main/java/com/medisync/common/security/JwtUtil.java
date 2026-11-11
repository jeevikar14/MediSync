package com.medisync.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Creates (patient-service /login) and validates (api-gateway) HS256 JWTs. Secret must be >= 32 chars. */
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtUtil(String secret, long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(String username, String role, String fullName) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("name", fullName)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMinutes * 60_000L))
                .signWith(key)
                .compact();
    }

    /** @throws io.jsonwebtoken.JwtException when the token is invalid or expired */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

package com.tricore.crmtoyota.security;

import com.tricore.crmtoyota.config.CrmProperties;
import com.tricore.crmtoyota.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationHours;

    public JwtService(CrmProperties properties) {
        byte[] keyBytes = properties.security().jwtSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationHours = properties.security().jwtExpirationHours();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationHours, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("fullName", user.getFullName())
                .claim("stationLabel", user.getStationLabel())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public Instant calculateExpiresAt() {
        return Instant.now().plus(expirationHours, ChronoUnit.HOURS);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.senla.pas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT секрет неправильный", e);
        }
    }

    public String generateToken(Authentication authentication, Long userId) {
        String username = authentication.getName();

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("authorities", authorities)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        String subject = parseClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    public List<String> getAuthoritiesFromToken(String token) {
        Object rawAuthorities = parseClaims(token).get("authorities");

        if (rawAuthorities == null) {
            return List.of();
        }

        if (rawAuthorities instanceof Collection<?> collection) {
            List<String> authorities = new ArrayList<>();
            for (Object authority : collection) {
                if (authority != null) {
                    String authorityName = authority.toString().trim();
                    if (!authorityName.isEmpty()) {
                        authorities.add(authorityName);
                    }
                }
            }
            return authorities;
        }

        if (rawAuthorities instanceof String authoritiesAsString) {
            String trimmed = authoritiesAsString.trim();
            if (trimmed.isEmpty()) {
                return List.of();
            }
            return List.of(trimmed.split("\\s*,\\s*"));
        }

        return List.of();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("JWT токен истёк: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Некорректный JWT: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("Невалидная подпись JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims пусты: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

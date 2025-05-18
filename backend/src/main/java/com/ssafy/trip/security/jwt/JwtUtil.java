package com.ssafy.trip.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final String base64Key = Base64.getEncoder()
            .encodeToString("BeomHwangSSAFY13giBeomHwangSSAFY13giBeomHwangSSAFY13giBeomHwangSSAFY13gi".getBytes());
    private final SecretKey secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Key));

    public String generateToken(String email, String role) {
        long expirationMs = 1000 * 60 * 60; // 1시간
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(secretKey)
                    .build();

            parser.parse(token); // 예외 없으면 유효
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    public Claims getClaims(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        Jwt<?, ?> jwt = parser.parse(token);

        return (Claims) jwt.getPayload();
    }
}

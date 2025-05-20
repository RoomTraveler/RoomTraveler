package com.ssafy.trip.security.jwt;

import com.ssafy.trip.user.User;
import io.jsonwebtoken.*;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    public JwtUtil() {
        key = Jwts.SIG.HS256.key().build();
    }
    @Value("${ssafy.jwt.access-expmin}")
    private int accessExpire;
    @Value("${ssafy.jwt.refresh-expmin}")
    private int refreshExpire;

    public String generateAccessToken(User user) {
        return create("accessToken", accessExpire, Map.of("email", user.getEmail(), "id", user.getUserId(), "name", user.getUsername(),"role",user.getRole()));
    }

    public String generateRefreshToken(String email, String role) {
        return create("refreshToken", refreshExpire, Map.of("email", email,"role", role));
    }

    private String create(String subject, long expiration, Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + 1000 * 60 * expiration);
        String jwt = Jwts.builder().subject(subject).claims(claims).expiration(expirationDate).signWith(key).compact();
        log.debug("token 생성: {}", jwt);
        return jwt;
    }

    public boolean isTokenValid(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(key)
                    .build();

            parser.parse(token); // 예외 없으면 유효
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    public Claims getClaims(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();

        Jws<Claims> jws = parser.parseSignedClaims(token);

        return jws.getPayload();
    }
}

package vn.edu.uit.is208.salon.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.entity.Staff;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    public String generateAccessToken(Staff staff) {
        return generateToken(staff, jwtConfig.getAccessTokenExpiration());
    }

    private String generateToken(Staff staff, Duration tokenExpiration) {
        return Jwts.builder()
                .subject(staff.getId().toString())
                .claim("role", staff.getRole().name())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(jwtConfig.getAccessTokenExpiration())))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}

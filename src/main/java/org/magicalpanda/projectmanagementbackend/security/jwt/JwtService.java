package org.magicalpanda.projectmanagementbackend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import org.magicalpanda.projectmanagementbackend.model.RefreshToken;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.repository.RefreshTokenRepository;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key signingKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${spring.security.jwt.access-token-expiration}") long accessTokenExpirationSeconds,
            @Value("${spring.security.jwt.refresh-token-expiration}") long refreshTokenExpirationSeconds,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // generate access token
    public String generateAccessToken(SecurityUser user) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("token_type", TokenType.ACCESS_TOKEN.name())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(now.plusSeconds(accessTokenExpirationSeconds))
                )
                .signWith(signingKey)
                .compact();
    }

    // generate refresh token
    public String generateRefreshToken(User user) {

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(refreshTokenExpirationSeconds);
        String jti = UUID.randomUUID().toString();

        // 1. Persist refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        // 2. Issue refresh token JWT
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("jti", jti)
                .claim("token_type", TokenType.REFRESH_TOKEN.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    // validate access token
    public Claims validateAccessToken(String token) {
        Claims claims = extractAllClaims(token);
        String tokenType = claims.get("token_type").toString();

        if (!TokenType.ACCESS_TOKEN.name().equals(tokenType)) {
            throw new JwtException("Invalid token type: " + tokenType);
        }

        return claims;
    }

    // validate refresh token
    public Claims validateRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String tokenType = claims.get("token_type").toString();

        if (!TokenType.REFRESH_TOKEN.name().equals(tokenType)) {
            throw new JwtException("Invalid token type: " + tokenType);
        }

        return claims;
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

//    private <T> @Nullable T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
}

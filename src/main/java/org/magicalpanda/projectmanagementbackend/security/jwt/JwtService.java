package org.magicalpanda.projectmanagementbackend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key signingKey;
    private final long accessTokenExpirationSeconds;

    public JwtService(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${spring.security.jwt.access-token-expiration}") long accessTokenExpirationSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
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

    // validate access token
    public void validateAccessToken(String token) {
        String tokenType = extractClaim(token, (claims) -> claims.get("token_type").toString());

        if (!TokenType.ACCESS_TOKEN.name().equals(tokenType)) {
            throw new JwtException("Invalid token type: " + tokenType);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> @Nullable T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}

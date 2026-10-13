
package com.project.mukera.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "mukera-super-secret-key-for-jwt-authentication-2026";

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private static final long ACCESS_TOKEN_EXPIRATION =
            1000L * 60 * 15; // 15 minutes

    private static final long REFRESH_TOKEN_EXPIRATION =
            1000L * 60 * 60 * 24 * 7; // 7 days

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Generate an access token
    public String generateToken(UserDetails userDetails) {
        return generateToken(
                userDetails,
                ACCESS_TOKEN_EXPIRATION,
                ACCESS_TOKEN_TYPE
        );
    }

    // Generate a refresh token
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(
                userDetails,
                REFRESH_TOKEN_EXPIRATION,
                REFRESH_TOKEN_TYPE
        );
    }

    private String generateToken(
            UserDetails userDetails,
            long expirationTime,
            String tokenType) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract the expiration date from a token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Accept access tokens only
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            String tokenType = claims.get(
                    TOKEN_TYPE_CLAIM,
                    String.class
            );

            Date expiration = claims.getExpiration();

            return username != null
                    && username.equals(userDetails.getUsername())
                    && ACCESS_TOKEN_TYPE.equals(tokenType)
                    && expiration != null
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    // Accept refresh tokens only
    public boolean isRefreshTokenValid(
            String token,
            UserDetails userDetails) {

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            String tokenType = claims.get(
                    TOKEN_TYPE_CLAIM,
                    String.class
            );

            Date expiration = claims.getExpiration();

            return username != null
                    && username.equals(userDetails.getUsername())
                    && REFRESH_TOKEN_TYPE.equals(tokenType)
                    && expiration != null
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}

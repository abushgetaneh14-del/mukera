
package com.project.mukera.service;

import com.project.mukera.entity.RefreshToken;
import com.project.mukera.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public void storeRefreshToken(String token, String username) {
        if (token == null || token.isBlank()
                || username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token and username are required");
        }

        try {
            Claims claims = jwtService.extractClaim(
                    token, claimsValue -> claimsValue);

            String tokenUsername = claims.getSubject();
            String tokenType = claims.get("token_type", String.class);
            Date expiration = claims.getExpiration();

            if (!username.equals(tokenUsername)) {
                throw new IllegalArgumentException(
                        "Refresh token does not belong to this user");
            }

            if (!"refresh".equals(tokenType)) {
                throw new IllegalArgumentException(
                        "A refresh token is required");
            }

            if (expiration == null
                    || !expiration.toInstant().isAfter(Instant.now())) {
                throw new IllegalArgumentException(
                        "Refresh token has expired");
            }

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setTokenHash(hashToken(token));
            refreshToken.setUsername(username);
            refreshToken.setExpiresAt(expiration.toInstant());
            refreshToken.setRevoked(false);

            refreshTokenRepository.save(refreshToken);

        } catch (JwtException ex) {
            throw new IllegalArgumentException(
                    "Invalid refresh token", ex);
        }
    }

    @Transactional
    public String consumeRefreshToken(
            String token, String username) {

        if (token == null || token.isBlank()
                || username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token and username are required");
        }

        String tokenHash = hashToken(token);

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new IllegalArgumentException(
                    "Refresh token has already been used or revoked");
        }

        if (!username.equals(storedToken.getUsername())) {
            throw new IllegalArgumentException(
                    "Refresh token does not belong to this user");
        }

        if (storedToken.getExpiresAt() == null
                || !storedToken.getExpiresAt()
                .isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "Refresh token has expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return storedToken.getUsername();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable", ex);
        }
    }
}


package com.project.mukera.service;

import com.project.mukera.entity.RefreshToken;
import com.project.mukera.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
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

    // Hash the token before storing it in the database
    private String hashToken(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    // Store a new refresh token
    @Transactional
    public void storeRefreshToken(
            String token,
            String username) {

        try {
            Instant expiration = jwtService
                    .extractExpiration(token)
                    .toInstant();

            if (!expiration.isAfter(Instant.now())) {
                throw new BadCredentialsException(
                        "Refresh token has expired"
                );
            }

            RefreshToken refreshToken = new RefreshToken();

            refreshToken.setTokenHash(hashToken(token));
            refreshToken.setUsername(username);
            refreshToken.setExpiresAt(expiration);
            refreshToken.setRevoked(false);

            refreshTokenRepository.save(refreshToken);

        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException(
                    "Invalid refresh token",
                    exception
            );
        }
    }

    // Validate and revoke a refresh token exactly once
    @Transactional
    public void consumeRefreshToken(
            String token,
            String username) {

        String tokenHash = hashToken(token);

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid or expired refresh token"
                ));

        if (storedToken.isRevoked()
                || !storedToken.getUsername().equals(username)
                || !storedToken.getExpiresAt().isAfter(Instant.now())) {

            throw new BadCredentialsException(
                    "Invalid or expired refresh token"
            );
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
    }
}

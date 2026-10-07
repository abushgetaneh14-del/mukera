package com.project.mukera.security;

import com.project.mukera.service.CustomUserDetailsService;
import com.project.mukera.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        // No Authorization header
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        final String jwtToken =
                authHeader.substring(7);

        try {

            // Extract email from JWT
            final String email =
                    jwtService.extractUsername(jwtToken);

            // Only authenticate if there is no existing authentication
            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                // Load user from database
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                // Validate JWT
                if (jwtService.isTokenValid(
                        jwtToken,
                        userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception exception) {

            // Invalid or expired JWT.
            // Leave the request unauthenticated.
        }

        filterChain.doFilter(request, response);
    }
}
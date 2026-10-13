
package com.project.mukera.service;

import com.project.mukera.dto.AdminCreateUserRequest;
import com.project.mukera.dto.LoginRequest;
import com.project.mukera.dto.LoginResponse;
import com.project.mukera.dto.RegisterRequest;
import com.project.mukera.dto.UserDTO;
import com.project.mukera.entity.Role;
import com.project.mukera.entity.User;
import com.project.mukera.exception.UserNotFoundException;
import com.project.mukera.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // Convert User entity to UserDTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    // Register a new user
    public UserDTO registerUser(RegisterRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

    // Create a user through the admin creation flow
    public UserDTO createAdminUser(AdminCreateUserRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Preserve the existing behavior: new users receive USER role.
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

    // Login: generate and store access and refresh tokens
    @Transactional
    public LoginResponse loginUser(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        request.getEmail()
                );

        String accessToken =
                jwtService.generateToken(userDetails);

        String refreshToken =
                jwtService.generateRefreshToken(userDetails);

        // Store only the refresh token's hash in PostgreSQL.
        refreshTokenService.storeRefreshToken(
                refreshToken,
                userDetails.getUsername()
        );

        return new LoginResponse(
                "Login successful",
                accessToken,
                refreshToken
        );
    }

    // Refresh: validate, revoke the old token, and issue replacements
    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken) {

        try {
            String email =
                    jwtService.extractUsername(refreshToken);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            // Validate the JWT signature, type, username, and expiry.
            if (!jwtService.isRefreshTokenValid(
                    refreshToken, userDetails)) {

                throw new BadCredentialsException(
                        "Invalid or expired refresh token"
                );
            }

            // Consume the old token. It cannot be reused.
            refreshTokenService.consumeRefreshToken(
                    refreshToken,
                    userDetails.getUsername()
            );

            // Generate replacement tokens.
            String newAccessToken =
                    jwtService.generateToken(userDetails);

            String newRefreshToken =
                    jwtService.generateRefreshToken(userDetails);

            // Register the replacement refresh token.
            refreshTokenService.storeRefreshToken(
                    newRefreshToken,
                    userDetails.getUsername()
            );

            return new LoginResponse(
                    "Token refreshed successfully",
                    newAccessToken,
                    newRefreshToken
            );

        } catch (BadCredentialsException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadCredentialsException(
                    "Invalid or expired refresh token",
                    exception
            );
        }
    }

    // Get all users
    public List<UserDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Get a user by ID
    public UserDTO getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        )
                );

        return convertToDTO(user);
    }

    // Update a user
    public UserDTO updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        )
                );

        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(existingUser);

        return convertToDTO(updatedUser);
    }

    // Delete a user
    public void deleteUser(Long id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        )
                );

        userRepository.delete(existingUser);
    }

    // Get users with pagination and sorting
    public Page<UserDTO> getUsers(Pageable pageable) {

        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    // Search users by name
    public List<UserDTO> searchByName(String name) {

        return userRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Search users by email
    public List<UserDTO> searchByEmail(String email) {

        return userRepository
                .findByEmailContainingIgnoreCase(email)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
}

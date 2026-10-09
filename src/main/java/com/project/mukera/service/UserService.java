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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

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

    public UserDTO createAdminUser(AdminCreateUserRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Never accept a role directly from the request.
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

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

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(
                "Login successful",
                token
        );
    }

    public List<UserDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public UserDTO getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        )
                );

        return convertToDTO(user);
    }

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

    public void deleteUser(Long id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        )
                );

        userRepository.delete(existingUser);
    }

    public Page<UserDTO> getUsers(Pageable pageable) {

        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public List<UserDTO> searchByName(String name) {

        return userRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<UserDTO> searchByEmail(String email) {

        return userRepository
                .findByEmailContainingIgnoreCase(email)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
}
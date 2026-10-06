package com.project.mukera.service;

import com.project.mukera.dto.UserDTO;
import com.project.mukera.entity.User;
import com.project.mukera.exception.UserNotFoundException;
import com.project.mukera.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Convert User entity to UserDTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    // Convert UserDTO to User entity
    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        return user;
    }

    // Create user
    public UserDTO saveUser(UserDTO userDTO) {
        User user = convertToEntity(userDTO);

        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

    // Get all users
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Get user by ID
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found"));

        return convertToDTO(user);
    }

    // Update user
    public UserDTO updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found"));

        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(existingUser);

        return convertToDTO(updatedUser);
    }

    // Delete user
    public void deleteUser(Long id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found"));

        userRepository.delete(existingUser);
    }

    // Pagination and sorting
    public Page<UserDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    // Search by name
    public List<UserDTO> searchByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Search by email
    public List<UserDTO> searchByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
}
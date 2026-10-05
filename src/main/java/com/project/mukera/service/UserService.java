package com.project.mukera.service;

import com.project.mukera.entity.User;
import com.project.mukera.exception.UserNotFoundException;
import com.project.mukera.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE USER
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // GET ALL USERS WITH PAGINATION AND SORTING
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // SEARCH USERS BY NAME OR EMAIL
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                );
    }

    // GET USER BY ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"));
    }

    // UPDATE USER
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());

        return userRepository.save(existingUser);
    }

    // DELETE USER
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"));

        userRepository.delete(user);
    }
}
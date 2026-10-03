package com.project.mukera.service;

import com.project.mukera.entity.User;
import com.project.mukera.exception.UserNotFoundException;
import com.project.mukera.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found"));
    }

    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found"));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());

        return userRepository.save(existingUser);
    }

   public void deleteUser(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() ->
                    new UserNotFoundException("User with id " + id + " not found"));

    userRepository.delete(user);
}
}
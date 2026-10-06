package com.project.mukera.controller;

import com.project.mukera.dto.UserDTO;
import com.project.mukera.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create user
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody UserDTO userDTO) {
        return userService.saveUser(userDTO);
    }

    // Get all users
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Update user
    @PutMapping("/{id}")
    public UserDTO updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {

        return userService.updateUser(id, userDTO);
    }

    // Delete user
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // Pagination and sorting
    @GetMapping("/page")
    public Page<UserDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userService.getUsers(pageable);
    }

    // Search users by name
    @GetMapping("/search/name")
    public List<UserDTO> searchByName(@RequestParam String name) {
        return userService.searchByName(name);
    }

    // Search users by email
    @GetMapping("/search/email")
    public List<UserDTO> searchByEmail(@RequestParam String email) {
        return userService.searchByEmail(email);
    }
}
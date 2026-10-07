package com.project.mukera.controller;

import com.project.mukera.dto.LoginRequest;
import com.project.mukera.dto.LoginResponse;
import com.project.mukera.dto.RegisterRequest;
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

    // Register user with password
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO registerUser(
            @Valid @RequestBody RegisterRequest request) {

        return userService.registerUser(request);
    }

    // Login user
    @PostMapping("/login")
    public LoginResponse loginUser(
            @Valid @RequestBody LoginRequest request) {

        return userService.loginUser(request);
    }

    // Get all users
    // Used when no pagination parameters are supplied
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get users with pagination and sorting
    // Example:
    // GET /users?page=0&size=2&sortBy=name&direction=asc
    @GetMapping(
            params = {"page", "size", "sortBy", "direction"}
    )
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

    // Get users with pagination using /users/page
    // Example:
    // GET /users/page?page=0&size=5&sortBy=name&direction=asc
    @GetMapping("/page")
    public Page<UserDTO> getUsersPage(
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
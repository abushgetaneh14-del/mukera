package com.project.mukera.controller;

import com.project.mukera.dto.LoginRequest;
import com.project.mukera.dto.LoginResponse;
import com.project.mukera.dto.RegisterRequest;
import com.project.mukera.dto.UserDTO;
import com.project.mukera.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================
    // CREATE USER
    // =========================

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody UserDTO userDTO) {

        UserDTO savedUser = userService.saveUser(userDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    // =========================
    // REGISTER
    // =========================

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(
            @Valid @RequestBody RegisterRequest request) {

        UserDTO user = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.loginUser(request);

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET ALL USERS
    // =========================

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {

        // Normal GET /users
        if (page == null && size == null) {

            return ResponseEntity.ok(
                    userService.getAllUsers()
            );
        }

        // Default values
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 5;

        String sortField =
                sortBy != null && !sortBy.isBlank()
                        ? sortBy
                        : "id";

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(sortDirection, sortField)
        );

        return ResponseEntity.ok(
                userService.getUsers(pageable)
        );
    }

    // =========================
    // GET USER BY ID
    // =========================

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    // =========================
    // UPDATE USER
    // =========================

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {

        return ResponseEntity.ok(
                userService.updateUser(id, userDTO)
        );
    }

    // =========================
    // DELETE USER
    // =========================

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // SEARCH BY NAME
    // =========================

    @GetMapping("/search/name")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserDTO>> searchByName(
            @RequestParam String name) {

        return ResponseEntity.ok(
                userService.searchByName(name)
        );
    }

    // =========================
    // SEARCH BY EMAIL
    // =========================

    @GetMapping("/search/email")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserDTO>> searchByEmail(
            @RequestParam String email) {

        return ResponseEntity.ok(
                userService.searchByEmail(email)
        );
    }
}
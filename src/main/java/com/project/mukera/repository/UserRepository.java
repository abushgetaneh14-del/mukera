package com.project.mukera.repository;

import com.project.mukera.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByNameContainingIgnoreCase(String name);

    List<User> findByEmailContainingIgnoreCase(String email);

    Optional<User> findByEmail(String email);
}
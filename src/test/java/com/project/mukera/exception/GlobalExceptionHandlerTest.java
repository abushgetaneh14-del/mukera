package com.project.mukera.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void handleUserNotFoundShouldReturn404() {
        var response = handler.handleUserNotFound(
                new UserNotFoundException("User not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    void handleBadCredentialsShouldReturn401() {
        var response = handler.handleBadCredentials(
                new BadCredentialsException("Invalid credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(
                "Invalid or expired authentication token.",
                response.getBody().get("message"));
    }

    @Test
    void handleTypeMismatchShouldReturn400() {
        var exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");

        var response = handler.handleTypeMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Invalid value provided for parameter: id",
                response.getBody().get("message"));
    }

    @Test
    void handleDataIntegrityViolationShouldReturn409() {
        var response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("Duplicate email"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("conflicts"));
    }

    @Test
    void handleUnexpectedExceptionShouldReturn500() {
        var response = handler.handleUnexpectedException(
                new RuntimeException("Internal detail"));

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
        assertEquals(
                "An unexpected server error occurred.",
                response.getBody().get("message"));
    }
}

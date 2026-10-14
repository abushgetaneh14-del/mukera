
package com.project.mukera.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    private static final String TEST_PASSWORD = "TestPassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }

    private String registerUser(String email) throws Exception {
        String registrationJson = objectMapper.writeValueAsString(
                new RegistrationRequest(
                        "Integration Test User",
                        email,
                        TEST_PASSWORD
                )
        );

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson))
                .andExpect(status().isCreated());

        return email;
    }

    private JsonNode loginUser(String email) throws Exception {
        String loginJson = objectMapper.writeValueAsString(
                new LoginRequest(email, TEST_PASSWORD)
        );

        String response = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        if (json.path("token").asText().isBlank()) {
            throw new AssertionError("Login did not return an access token");
        }

        if (json.path("refreshToken").asText().isBlank()) {
            throw new AssertionError("Login did not return a refresh token");
        }

        return json;
    }

    @Test
    void protectedEndpointRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registeredUserCanLoginAndReadUsers() throws Exception {
        String email = uniqueEmail("integration-user");
        registerUser(email);

        JsonNode login = loginUser(email);
        String accessToken = login.path("token").asText();

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void regularUserCannotCreateUsers() throws Exception {
        String email = uniqueEmail("integration-user");
        registerUser(email);

        JsonNode login = loginUser(email);
        String accessToken = login.path("token").asText();

        String createUserJson = objectMapper.writeValueAsString(
                new AdminCreateUserRequest(
                        "Attempted User",
                        uniqueEmail("attempted-user"),
                        TEST_PASSWORD
                )
        );

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshTokenRotationRejectsReuseOfOldToken() throws Exception {
        String email = uniqueEmail("refresh-user");
        registerUser(email);

        JsonNode login = loginUser(email);
        String oldRefreshToken = login.path("refreshToken").asText();

        String refreshJson = objectMapper.writeValueAsString(
                new RefreshRequest(oldRefreshToken)
        );

        String refreshResponse = mockMvc.perform(post("/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode refreshed = objectMapper.readTree(refreshResponse);
        String newRefreshToken = refreshed.path("refreshToken").asText();

        if (newRefreshToken.isBlank()) {
            throw new AssertionError(
                    "Refresh response did not contain a new refresh token"
            );
        }

        if (newRefreshToken.equals(oldRefreshToken)) {
            throw new AssertionError(
                    "Refresh must issue a different refresh token"
            );
        }

        // The old refresh token must no longer be reusable.
        mockMvc.perform(post("/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isUnauthorized());
    }

    private record RegistrationRequest(
            String name,
            String email,
            String password) {
    }

    private record LoginRequest(
            String email,
            String password) {
    }

    private record AdminCreateUserRequest(
            String name,
            String email,
            String password) {
    }

    private record RefreshRequest(
            String refreshToken) {
    }
}

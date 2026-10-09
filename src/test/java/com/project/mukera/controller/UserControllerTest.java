package com.project.mukera.controller;

import com.project.mukera.dto.AdminCreateUserRequest;
import com.project.mukera.dto.UserDTO;
import com.project.mukera.exception.UserNotFoundException;
import com.project.mukera.service.CustomUserDetailsService;
import com.project.mukera.service.JwtService;
import com.project.mukera.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getUserById_ShouldReturnUser() throws Exception {

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Abush");
        user.setEmail("abush@example.com");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Abush"))
                .andExpect(jsonPath("$.email").value("abush@example.com"));
    }

    @Test
    void getAllUsers_ShouldReturnUsers() throws Exception {

        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setName("Abush");
        user1.setEmail("abush@example.com");

        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setName("John");
        user2.setEmail("john@example.com");

        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Abush"))
                .andExpect(jsonPath("$[1].name").value("John"));
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound()
            throws Exception {

        when(userService.getUserById(99L))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Abush");
        user.setEmail("abush@example.com");

        when(userService.createAdminUser(
                any(AdminCreateUserRequest.class)
        )).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Abush",
                                    "email": "abush@example.com",
                                    "password": "SecurePass123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Abush"))
                .andExpect(jsonPath("$.email").value("abush@example.com"));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "",
                                    "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Abush",
                                    "email": "invalid-email",
                                    "password": "SecurePass123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Abush Updated");
        user.setEmail("abush.updated@example.com");

        when(userService.updateUser(
                eq(1L),
                any(UserDTO.class)
        )).thenReturn(user);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Abush Updated",
                                    "email": "abush.updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Abush Updated"))
                .andExpect(jsonPath("$.email")
                        .value("abush.updated@example.com"));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound()
            throws Exception {

        when(userService.updateUser(
                eq(99L),
                any(UserDTO.class)
        )).thenThrow(
                new UserNotFoundException("User not found")
        );

        mockMvc.perform(put("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test",
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {

        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound()
            throws Exception {

        doThrow(
                new UserNotFoundException("User not found")
        ).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByName_ShouldReturnUsers() throws Exception {

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Abush");
        user.setEmail("abush@example.com");

        when(userService.searchByName("Abush"))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/users/search/name")
                        .param("name", "Abush"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Abush"));
    }

    @Test
    void searchByEmail_ShouldReturnUsers() throws Exception {

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Abush");
        user.setEmail("abush@example.com");

        when(userService.searchByEmail("abush@example.com"))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/users/search/email")
                        .param("email", "abush@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email")
                        .value("abush@example.com"));
    }

    @Test
    void getUsers_WithPaginationAndSorting_ShouldReturnPage()
            throws Exception {

        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setName("Abush");
        user1.setEmail("abush@example.com");

        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setName("John");
        user2.setEmail("john@example.com");

        List<UserDTO> users = List.of(user1, user2);

        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by("name").ascending()
        );

        Page<UserDTO> page = new PageImpl<>(
                users,
                pageable,
                users.size()
        );

        when(userService.getUsers(pageable))
                .thenReturn(page);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Abush"))
                .andExpect(jsonPath("$.content[1].name").value("John"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
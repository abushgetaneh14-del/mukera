package com.project.mukera.controller;

import com.project.mukera.dto.UserDTO;
import com.project.mukera.exception.UserNotFoundException;
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
import static org.mockito.BDDMockito.given;
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

    @Test
    void getUserById_ShouldReturnUser() throws Exception {

        UserDTO userDTO = new UserDTO(
                1L,
                "Abush DTO",
                "abush.dto@example.com"
        );

        given(userService.getUserById(1L))
                .willReturn(userDTO);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Abush DTO"))
                .andExpect(jsonPath("$.email")
                        .value("abush.dto@example.com"));
    }

    @Test
    void getAllUsers_ShouldReturnUsers() throws Exception {

        List<UserDTO> users = List.of(
                new UserDTO(
                        1L,
                        "Abush",
                        "abush@example.com"
                ),
                new UserDTO(
                        2L,
                        "John",
                        "john@example.com"
                )
        );

        given(userService.getAllUsers())
                .willReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Abush"))
                .andExpect(jsonPath("$[0].email")
                        .value("abush@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("John"))
                .andExpect(jsonPath("$[1].email")
                        .value("john@example.com"));
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound()
            throws Exception {

        given(userService.getUserById(999L))
                .willThrow(
                        new UserNotFoundException(
                                "User with id 999 not found"
                        )
                );

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id 999 not found"));
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {

        UserDTO responseUser = new UserDTO(
                1L,
                "Alice",
                "alice@example.com"
        );

        given(userService.saveUser(any(UserDTO.class)))
                .willReturn(responseUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Alice",
                            "email": "alice@example.com"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email")
                        .value("alice@example.com"));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "",
                            "email": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name")
                        .value("Name is required"))
                .andExpect(jsonPath("$.email")
                        .value("Email is required"));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Abush",
                            "email": "not-an-email"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email")
                        .value("Email must be valid"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {

        UserDTO updatedUser = new UserDTO(
                1L,
                "Abush Updated",
                "abush.updated@example.com"
        );

        given(userService.updateUser(
                org.mockito.ArgumentMatchers.eq(1L),
                any(UserDTO.class)
        )).willReturn(updatedUser);

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
                .andExpect(jsonPath("$.name")
                        .value("Abush Updated"))
                .andExpect(jsonPath("$.email")
                        .value("abush.updated@example.com"));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound()
            throws Exception {

        given(userService.updateUser(
                org.mockito.ArgumentMatchers.eq(999L),
                any(UserDTO.class)
        )).willThrow(
                new UserNotFoundException(
                        "User with id 999 not found"
                )
        );

        mockMvc.perform(put("/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Test",
                            "email": "test@example.com"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id 999 not found"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound()
            throws Exception {

        org.mockito.BDDMockito.willThrow(
                new UserNotFoundException(
                        "User with id 999 not found"
                )
        ).given(userService).deleteUser(999L);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id 999 not found"));
    }

    @Test
    void searchByName_ShouldReturnUsers() throws Exception {

        List<UserDTO> users = List.of(
                new UserDTO(
                        1L,
                        "Abush",
                        "abush@example.com"
                ),
                new UserDTO(
                        2L,
                        "Abush Test",
                        "abush.test@example.com"
                )
        );

        given(userService.searchByName("Abush"))
                .willReturn(users);

        mockMvc.perform(get("/users/search/name")
                .param("name", "Abush"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Abush"))
                .andExpect(jsonPath("$[0].email")
                        .value("abush@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Abush Test"))
                .andExpect(jsonPath("$[1].email")
                        .value("abush.test@example.com"));
    }

    @Test
    void searchByEmail_ShouldReturnUsers() throws Exception {

        List<UserDTO> users = List.of(
                new UserDTO(
                        1L,
                        "Abush",
                        "abush@example.com"
                )
        );

        given(userService.searchByEmail("abush@example.com"))
                .willReturn(users);

        mockMvc.perform(get("/users/search/email")
                .param("email", "abush@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Abush"))
                .andExpect(jsonPath("$[0].email")
                        .value("abush@example.com"));
    }

    @Test
    void getUsers_WithPaginationAndSorting_ShouldReturnPage()
            throws Exception {

        List<UserDTO> users = List.of(
                new UserDTO(
                        1L,
                        "Abush",
                        "abush@example.com"
                ),
                new UserDTO(
                        2L,
                        "John",
                        "john@example.com"
                )
        );

        Page<UserDTO> page = new PageImpl<>(
                users,
                PageRequest.of(
                        0,
                        2,
                        Sort.by("name").ascending()
                ),
                4
        );

        given(userService.getUsers(any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/users/page")
                .param("page", "0")
                .param("size", "2")
                .param("sortBy", "name")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Abush"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("abush@example.com"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("John"))
                .andExpect(jsonPath("$.content[1].email")
                        .value("john@example.com"))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }
}
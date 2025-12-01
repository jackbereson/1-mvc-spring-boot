package com.mvcCore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvcCore.dto.request.UpdateUserRequest;
import com.mvcCore.model.Role;
import com.mvcCore.model.User;
import com.mvcCore.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Integration Test for UserController - Tests FULL API FLOW
 * API → Controller → Service → Repository → H2 Database (in-memory)
 * This is a REAL integration test that:
 * - Uses H2 in-memory database (not mocked)
 * - Tests complete end-to-end flow
 * - Validates actual HTTP requests/responses
 * - Tests with real Spring Security context
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("UserController Integration Tests - Full API Flow")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();
        
        // Create real test user in database
        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Test User")
                .isActive(true)
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);

        updateUserRequest = UpdateUserRequest.builder()
                .fullName("Updated User")
                .isActive(false)
                .build();
    }

    // ========== Test GET ALL USERS ==========

    @Test
    @Order(1)
    @DisplayName("Should retrieve all users successfully from database")
    void testGetAllUsers_Success() throws Exception {
        // Arrange - User already created in setUp()
        
        // Act & Assert - Call REAL API endpoint
        mockMvc.perform(get("/api/v1/users")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Users retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].email", is("test@example.com")))
                .andExpect(jsonPath("$.data[0].fullName", is("Test User")));
    }

    @Test
    @Order(2)
    @DisplayName("Should return empty list when no users exist in database")
    void testGetAllUsers_Empty() throws Exception {
        // Arrange - Delete all users from database
        userRepository.deleteAll();

        // Act & Assert - Call REAL API
        mockMvc.perform(get("/api/v1/users")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // ========== Test GET USER BY ID ==========

    @Test
    @Order(3)
    @DisplayName("Should retrieve user by ID from database")
    void testGetUserById_Success() throws Exception {
        // Arrange - User already in database from setUp()
        Long userId = testUser.getId();

        // Act & Assert - Call REAL API
        mockMvc.perform(get("/api/v1/users/{id}", userId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User retrieved successfully")))
                .andExpect(jsonPath("$.data.email", is("test@example.com")))
                .andExpect(jsonPath("$.data.fullName", is("Test User")))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @Order(4)
    @DisplayName("Should return 404 when user not found in database")
    void testGetUserById_NotFound() throws Exception {
        // Arrange - Use non-existent ID
        Long nonExistentId = 99999L;

        // Act & Assert - Call REAL API
        mockMvc.perform(get("/api/v1/users/{id}", nonExistentId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    // ========== Test UPDATE USER ==========

    @Test
    @Order(5)
    @DisplayName("Should update user in database successfully")
    void testUpdateUser_Success() throws Exception {
        // Arrange - User already in database
        Long userId = testUser.getId();

        // Act & Assert - Call REAL API to update
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User updated successfully")))
                .andExpect(jsonPath("$.data.fullName", is("Updated User")))
                .andExpect(jsonPath("$.data.isActive", is(false)));
        
        // Verify database was actually updated
        User updatedUserInDb = userRepository.findById(userId).orElseThrow();
        Assertions.assertEquals("Updated User", updatedUserInDb.getFullName());
        Assertions.assertEquals(false, updatedUserInDb.getIsActive());
    }

    @Test
    @Order(6)
    @DisplayName("Should return 404 when updating non-existent user in database")
    void testUpdateUser_NotFound() throws Exception {
        // Arrange - Use non-existent ID
        Long nonExistentId = 99999L;

        // Act & Assert - Call REAL API
        mockMvc.perform(put("/api/v1/users/{id}", nonExistentId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    @Order(7)
    @DisplayName("Should return 400 for invalid update request (validation)")
    void testUpdateUser_ValidationError() throws Exception {
        // Arrange - Create invalid request with blank fullName
        UpdateUserRequest invalidRequest = UpdateUserRequest.builder()
                .fullName("") // Invalid: blank
                .isActive(true)
                .build();
        Long userId = testUser.getId();

        // Act & Assert - Call REAL API
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        // Verify database was NOT updated
        User userInDb = userRepository.findById(userId).orElseThrow();
        Assertions.assertEquals("Test User", userInDb.getFullName()); // Still original
    }

    // ========== Test DELETE USER ==========

    @Test
    @Order(8)
    @DisplayName("Should delete user from database successfully")
    void testDeleteUser_Success() throws Exception {
        // Arrange - User already in database
        Long userId = testUser.getId();

        // Act & Assert - Call REAL API to delete
        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User deleted successfully")))
                .andExpect(jsonPath("$.data", nullValue()));
        
        // Verify user was actually deleted from database
        Assertions.assertFalse(userRepository.findById(userId).isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("Should return 404 when deleting non-existent user from database")
    void testDeleteUser_NotFound() throws Exception {
        // Arrange - Use non-existent ID
        Long nonExistentId = 99999L;

        // Act & Assert - Call REAL API
        mockMvc.perform(delete("/api/v1/users/{id}", nonExistentId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }
}

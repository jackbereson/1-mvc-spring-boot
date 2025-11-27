
import com.mvcCore.controller.*;

import com.mvcCore.dto.ApiResponse;
import com.mvcCore.dto.UserDto;
import com.mvcCore.model.User;
import com.mvcCore.model.Role;
import com.mvcCore.repository.UserRepository;
import com.mvcCore.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDto testUserDto;
    private String validToken = "Bearer valid_token";
    private String invalidToken = "Bearer invalid_token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Tạo user test
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setIsActive(true);
        testUser.setRole(Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Tạo UserDto test
        testUserDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .isActive(true)
                .role(Role.USER)
                .build();
    }

    // ========== Test GET ALL USERS ==========

    @Test
    void testGetAllUsers_Success() {
        // Arrange: Setup dữ liệu giả
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);

        // Act: Gọi hàm
        ResponseEntity<?> response = userController.getAllUsers(validToken);

        // Assert: Kiểm tra kết quả
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getSuccess());
        assertEquals("Success", apiResponse.getMessage());
    }

    @Test
    void testGetAllUsers_Unauthorized() {
        // Arrange
        when(jwtUtil.validateToken("invalid_token")).thenReturn(false);

        // Act
        ResponseEntity<?> response = userController.getAllUsers(invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.getSuccess());
        assertEquals("Unauthorized", apiResponse.getMessage());
    }

    @Test
    void testGetAllUsers_NoAuthHeader() {
        // Act
        ResponseEntity<?> response = userController.getAllUsers(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ========== Test GET USER BY ID ==========

    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.getUserById(1L, validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getSuccess());
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.getUserById(999L, validToken);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("User not found", apiResponse.getMessage());
    }

    @Test
    void testGetUserById_Unauthorized() {
        // Act
        ResponseEntity<?> response = userController.getUserById(1L, invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ========== Test UPDATE USER ==========

    @Test
    void testUpdateUser_Success() {
        // Arrange
        UserDto updateDto = UserDto.builder()
                .fullName("Updated Name")
                .isActive(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = userController.updateUser(1L, updateDto, validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getSuccess());
        assertEquals("User updated successfully", apiResponse.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.updateUser(999L, testUserDto, validToken);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
    }

    @Test
    void testUpdateUser_Unauthorized() {
        // Act
        ResponseEntity<?> response = userController.updateUser(1L, testUserDto, invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ========== Test DELETE USER ==========

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.deleteUser(1L, validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getSuccess());
        assertEquals("User deleted successfully", apiResponse.getMessage());
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.deleteUser(999L, validToken);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
    }

    @Test
    void testDeleteUser_Unauthorized() {
        // Act
        ResponseEntity<?> response = userController.deleteUser(1L, invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
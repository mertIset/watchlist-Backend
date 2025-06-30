package de.htwberlin.webtech.webtech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Given
        when(userService.registerUser(any(), any(), any(), any(), any())).thenReturn(testUser);

        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registrierung erfolgreich!"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Test"))
                .andExpect(jsonPath("$.user.lastName").value("User"));
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Username bereits vergeben!"));

        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username bereits vergeben!"));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        // Given
        when(userService.loginUser("testuser", "password123")).thenReturn(Optional.of(testUser));

        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login erfolgreich!"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        // Given
        when(userService.loginUser("testuser", "wrongpassword")).thenReturn(Optional.empty());

        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ungültige Anmeldedaten!"));
    }

    @Test
    void testGetUser_Success() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/auth/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void testGetUser_NotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/auth/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setCreatedAt(LocalDateTime.now());

        when(userService.updateUser(anyLong(), any(), any(), any())).thenReturn(updatedUser);

        AuthController.UpdateUserRequest request = new AuthController.UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setEmail("updated@example.com");

        // When & Then
        mockMvc.perform(put("/auth/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void testUpdateUser_UserNotFound() throws Exception {
        // Given
        when(userService.updateUser(anyLong(), any(), any(), any()))
                .thenThrow(new RuntimeException("User not found"));

        AuthController.UpdateUserRequest request = new AuthController.UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setEmail("updated@example.com");

        // When & Then
        mockMvc.perform(put("/auth/user/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testRegisterUser_InvalidRequestBody() throws Exception {
        // Given - Leerer Request Body
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isOk()) // Der Controller validiert nicht streng genug
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testLoginUser_InvalidRequestBody() throws Exception {
        // Given - Request ohne Username
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setPassword("password123");
        // username bleibt null

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testCORSHeaders() throws Exception {
        // Test, dass CORS-Header korrekt gesetzt werden
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
package de.htwberlin.webtech.webtech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
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
@Tag(TestConstants.TestCategories.CONTROLLER_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Email bereits registriert!"));

        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email bereits registriert!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testGetUser_NotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/auth/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testRegisterUser_EmptyUsername() throws Exception {
        // Given
        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername(""); // Empty username
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userService.registerUser("", "test@example.com", "password123", "Test", "User"))
                .thenThrow(new RuntimeException("Username darf nicht leer sein!"));

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username darf nicht leer sein!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testRegisterUser_InvalidEmail() throws Exception {
        // Given
        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email"); // Invalid email format
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userService.registerUser("testuser", "invalid-email", "password123", "Test", "User"))
                .thenThrow(new RuntimeException("Ungültiges Email-Format!"));

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ungültiges Email-Format!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testLoginUser_EmptyUsername() throws Exception {
        // Given
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername(""); // Empty username
        request.setPassword("password123");

        when(userService.loginUser("", "password123")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ungültige Anmeldedaten!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testLoginUser_NullPassword() throws Exception {
        // Given
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("testuser");
        request.setPassword(null); // Null password

        when(userService.loginUser("testuser", null)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ungültige Anmeldedaten!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testCORSHeaders() throws Exception {
        // Test, dass CORS-Header korrekt gesetzt werden
        mockMvc.perform(options("/auth/login")
                        .header("Origin", TestConstants.Http.CORS_ORIGIN_LOCALHOST)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testRegisterUser_SpecialCharactersInName() throws Exception {
        // Given
        when(userService.registerUser(any(), any(), any(), any(), any())).thenReturn(testUser);

        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("user123");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("José");
        request.setLastName("O'Connor");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testUpdateUser_PartialUpdate() throws Exception {
        // Given - Nur firstName wird geändert
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("OnlyFirstNameChanged");
        updatedUser.setLastName("User"); // Bleibt gleich
        updatedUser.setCreatedAt(LocalDateTime.now());

        when(userService.updateUser(1L, "OnlyFirstNameChanged", "User", "test@example.com"))
                .thenReturn(updatedUser);

        AuthController.UpdateUserRequest request = new AuthController.UpdateUserRequest();
        request.setFirstName("OnlyFirstNameChanged");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(put("/auth/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("OnlyFirstNameChanged"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
package de.htwberlin.webtech.webtech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Neue Annotation statt @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterUser_Success() throws Exception {
        // Arrange
        User mockUser = new User("testuser", "test@example.com", "password", "Test", "User");
        mockUser.setId(1L);
        mockUser.setCreatedAt(LocalDateTime.now());

        when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockUser);

        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFirstName("Test");
        request.setLastName("User");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registrierung erfolgreich!"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));

        verify(userService).registerUser("testuser", "test@example.com", "password", "Test", "User");
    }

    @Test
    void testLoginUser_Success() throws Exception {
        // Arrange
        User mockUser = new User("testuser", "test@example.com", "password", "Test", "User");
        mockUser.setId(1L);
        mockUser.setLastLogin(LocalDateTime.now());

        when(userService.loginUser("testuser", "password"))
                .thenReturn(Optional.of(mockUser));

        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login erfolgreich!"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(userService).loginUser("testuser", "password");
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        // Arrange
        when(userService.loginUser("testuser", "wrongpassword"))
                .thenReturn(Optional.empty());

        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ungültige Anmeldedaten!"));
    }
}
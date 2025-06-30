package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
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
    void registerUser_ShouldCreateNewUser_WhenValidData() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerUser("testuser", "test@example.com",
                "password123", "Test", "User");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser("testuser", "test@example.com",
                    "password123", "Test", "User");
        });

        assertEquals("Username bereits vergeben!", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser("testuser", "test@example.com",
                    "password123", "Test", "User");
        });

        assertEquals("Email bereits registriert!", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_ShouldReturnUser_WhenCredentialsValid() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        Optional<User> result = userService.loginUser("testuser", "password123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(any(User.class)); // For updating last login
    }

    @Test
    void loginUser_ShouldReturnEmpty_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.loginUser("nonexistent", "password123");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_ShouldReturnEmpty_WhenPasswordIncorrect() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.loginUser("testuser", "wrongpassword");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenUserExists() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(1L, "Updated", "Name", "updated@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("updated@example.com", result.getEmail());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(999L, "Updated", "Name", "updated@example.com");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }
}
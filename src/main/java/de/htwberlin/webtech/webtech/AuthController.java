package de.htwberlin.webtech.webtech;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://watchlist-frontend-bzxi.onrender.com"})
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            User newUser = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );

            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Registrierung erfolgreich!",
                    new UserDTO(newUser)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        Optional<User> userOptional = userService.loginUser(request.getUsername(), request.getPassword());

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Login erfolgreich!",
                    new UserDTO(userOptional.get())
            ));
        } else {
            return ResponseEntity.badRequest().body(new AuthResponse(
                    false,
                    "Ungültige Anmeldedaten!",
                    null
            ));
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(new UserDTO(userOptional.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        try {
            User updatedUser = userService.updateUser(id, request.getFirstName(), request.getLastName(), request.getEmail());
            return ResponseEntity.ok(new UserDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    // Request DTOs
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;

        // Getters und Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;
        private String email;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // Response DTOs
    public static class AuthResponse {
        private boolean success;
        private String message;
        private UserDTO user;

        public AuthResponse(boolean success, String message, UserDTO user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserDTO getUser() { return user; }
    }

    public static class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String createdAt;
        private String lastLogin;

        public UserDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
            this.lastLogin = user.getLastLogin() != null ? user.getLastLogin().toString() : null;
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getCreatedAt() { return createdAt; }
        public String getLastLogin() { return lastLogin; }
    }
}
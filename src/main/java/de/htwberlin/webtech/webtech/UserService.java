package de.htwberlin.webtech.webtech;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(String username, String email, String password, String firstName, String lastName) {
        // Prüfe ob Username oder Email bereits existiert
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username bereits vergeben!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email bereits registriert!");
        }

        // Erstelle neuen User
        User newUser = new User(username, email, password, firstName, lastName);
        return userRepository.save(newUser);
    }

    public Optional<User> loginUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Einfache Passwort-Prüfung (in der Realität sollte das gehashed werden)
            if (user.getPassword().equals(password)) {
                // Update last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(Long userId, String firstName, String lastName, String email) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }
}
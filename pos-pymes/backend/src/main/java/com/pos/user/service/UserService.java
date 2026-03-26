package com.pos.user.service;

import com.pos.user.model.User;
import com.pos.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            if (!user.getPasswordHash().startsWith("$2a$")) {
                user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public void softDelete(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(false);
        user.setLockedUntil(null);
        user.setLoginAttempts(0);
        userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && !user.get().getId().equals(id);
    }

    @Transactional
    public void incrementFailedAttempts(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetFailedAttempts(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        });
    }

    @Transactional
    public void lockUser(Long userId, LocalDateTime lockedUntil) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLockedUntil(lockedUntil);
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateLastLogin(Long userId, LocalDateTime lastLogin) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(lastLogin);
            userRepository.save(user);
        });
    }

    public List<User> findByNameContainingIgnoreCase(String name) {
        return userRepository.findAll().stream()
                .filter(u -> u.getFullName() != null &&
                           u.getFullName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public long countByActiveTrue() {
        return userRepository.findAll().stream()
                .filter(User::getActive)
                .count();
    }

    public List<User> findAllByActiveTrue() {
        return userRepository.findAll().stream()
                .filter(User::getActive)
                .toList();
    }

    public List<User> findAllByRole(String roleName) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null &&
                           u.getRoles().stream().anyMatch(r -> r.getName().equals(roleName)))
                .toList();
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public long count() {
        return userRepository.count();
    }
}

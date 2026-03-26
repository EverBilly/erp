package com.pos.user.service;

import com.pos.user.exception.UserNotFoundException;
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

    public User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
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
        encodePasswordIfNeeded(user);
        return userRepository.save(user);
    }

    @Transactional
    public void softDelete(Long id) {
        User user = findByIdOrThrow(id);
        user.setActive(false);
        user.setLockedUntil(null);
        user.setLoginAttempts(0);
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    public List<User> findByActiveTrue() {
        return userRepository.findByActiveTrue();
    }

    public long countByActiveTrue() {
        return userRepository.countByActiveTrue();
    }

    public List<User> findByNameContaining(String name) {
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }

    public List<User> findAllByRole(String roleName) {
        return userRepository.findAllByRole(roleName);
    }

    @Transactional
    public void incrementFailedAttempts(Long userId) {
        userRepository.incrementFailedAttempts(userId);
    }

    @Transactional
    public void resetFailedAttempts(Long userId) {
        userRepository.resetFailedAttempts(userId);
    }

    @Transactional
    public void lockUser(Long userId, LocalDateTime lockedUntil) {
        userRepository.lockUser(userId, lockedUntil);
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public long count() {
        return userRepository.count();
    }

    private void encodePasswordIfNeeded(User user) {
        String password = user.getPasswordHash();
        if (password != null && !password.isEmpty() && !password.startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
    }
}

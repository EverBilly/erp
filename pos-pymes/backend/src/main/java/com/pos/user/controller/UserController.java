package com.pos.user.controller;

import com.pos.user.model.User;
import com.pos.user.exception.DuplicateUserException;
import com.pos.user.exception.UserNotFoundException;
import com.pos.role.model.Role;
import com.pos.user.service.UserService;
import com.pos.user.dto.UserResponse;
import com.pos.user.dto.CreateUserRequest;
import com.pos.user.dto.UpdateUserRequest;
import com.pos.role.repository.RoleRepository;
import com.pos.shared.auth.dto.RoleDisplayDto;
import com.pos.shared.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Helper methods ---

    private UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }
        throw new IllegalStateException("User not authenticated");
    }

    private UserResponse convertToDto(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setTimezone(user.getTimezone());
        dto.setLocale(user.getLocale());

        List<RoleDisplayDto> rolesDisplay = user.getRoles().stream()
            .map(this::convertRoleToDisplayDto)
            .collect(Collectors.toList());
        dto.setRoles(rolesDisplay);

        return dto;
    }

    private RoleDisplayDto convertRoleToDisplayDto(Role role) {
        String displayName = role.getDescription() != null && !role.getDescription().trim().isEmpty()
            ? role.getDescription()
            : role.getName();

        String color = getRoleColor(role);

        return new RoleDisplayDto(
            role.getId(),
            role.getName(),
            displayName,
            role.getDescription() != null ? role.getDescription() : "Sin descripción",
            color,
            role.getPriorityLevel()
        );
    }

    private String getRoleColor(Role role) {
        Integer priority = role.getPriorityLevel();
        if (priority == null) return "default";
        if (priority >= 900) return "error";
        if (priority >= 500) return "warning";
        if (priority > 0) return "info";
        return "default";
    }

    private Set<Role> resolveRoles(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return Set.of();
        return roleIds.stream()
                .map(id -> roleRepository.findById(id.longValue()).orElse(null))
                .filter(r -> r != null)
                .collect(Collectors.toSet());
    }

    private boolean isSuperAdmin(User user) {
        return user.hasRole("SUPER_ADMIN");
    }

    // --- Endpoints ---

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
            userService.findAll().stream().map(this::convertToDto).toList()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        return ResponseEntity.ok(
            userService.findByActiveTrue().stream().map(this::convertToDto).toList()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam(required = false) String name) {
        List<User> users = (name != null && !name.trim().isEmpty())
            ? userService.findByNameContaining(name)
            : userService.findAll();
        return ResponseEntity.ok(users.stream().map(this::convertToDto).toList());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        long total = userService.count();
        long active = userService.countByActiveTrue();

        return ResponseEntity.ok(Map.of(
            "total", total,
            "activos", active,
            "inactivos", total - active
        ));
    }

    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(
            userService.findAllByRole(roleName).stream().map(this::convertToDto).toList()
        );
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("existe", userService.existsByUsername(username)));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok(Map.of("existe", userService.existsByEmail(email)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.findByIdOrThrow(id);
        return ResponseEntity.ok(convertToDto(user));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("username", request.getUsername());
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email", request.getEmail());
        }

        UserPrincipal currentUser = getCurrentUser();

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setPhone(request.getPhone());
        newUser.setActive(request.isActive());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setTenantId(currentUser.getTenantId());
        newUser.setAvatarUrl(request.getAvatarUrl());
        newUser.setTimezone(request.getTimezone());
        newUser.setLocale(request.getLocale());
        newUser.setRoles(resolveRoles(request.getRoleIds()));

        User saved = userService.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.findByIdOrThrow(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userService.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new DuplicateUserException("email", request.getEmail());
            }
        }

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone());
        if (request.getLocale() != null) user.setLocale(request.getLocale());
        if (request.getMetadata() != null) user.setMetadata(request.getMetadata());
        if (request.getActive() != null) user.setActive(request.getActive());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleIds() != null) {
            user.setRoles(resolveRoles(request.getRoleIds()));
        }

        User updated = userService.save(user);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        UserPrincipal currentUser = getCurrentUser();

        if (id.equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No puedes desactivar tu propia cuenta"));
        }

        User target = userService.findByIdOrThrow(id);
        if (isSuperAdmin(target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No se puede eliminar a un Super Administrador"));
        }

        userService.softDelete(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado correctamente"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        User user = userService.findByIdOrThrow(id);
        user.setActive(true);
        user.setLockedUntil(null);
        user.setLoginAttempts(0);

        User updated = userService.save(user);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        UserPrincipal currentUser = getCurrentUser();

        if (id.equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No puedes desactivar tu propia cuenta"));
        }

        User target = userService.findByIdOrThrow(id);
        if (isSuperAdmin(target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No se puede desactivar a un Super Administrador"));
        }

        target.setActive(false);
        User updated = userService.save(target);
        return ResponseEntity.ok(convertToDto(updated));
    }
}

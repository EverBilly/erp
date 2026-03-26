package com.pos.user.controller;

import com.pos.user.model.User;
import com.pos.role.model.Role;
import com.pos.user.service.UserService;
import com.pos.user.dto.UserResponse;
import com.pos.user.dto.CreateUserRequest;
import com.pos.user.dto.UpdateUserRequest;
import com.pos.role.repository.RoleRepository;
import com.pos.shared.auth.dto.RoleDisplayDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;

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

    private RoleDisplayDto convertRoleToDisplayDto(Role role) {
        String displayName = role.getDescription() != null && !role.getDescription().trim().isEmpty()
            ? role.getDescription()
            : role.getName();

        String color = getRoleColor(role.getName());

        return new RoleDisplayDto(
            role.getName(),
            displayName,
            role.getDescription() != null ? role.getDescription() : "Sin descripción",
            color,
            role.getPriorityLevel()
        );
    }

    private String getRoleColor(String roleName) {
        if ("SUPER_ADMIN".equals(roleName)) return "error";
        if ("ADMIN".equals(roleName)) return "warning";
        if ("USER".equals(roleName)) return "info";

        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isPresent()) {
            Integer priority = roleOpt.get().getPriorityLevel();
            if (priority != null) {
                if (priority >= 900) return "error";
                if (priority >= 500) return "warning";
                if (priority > 0) return "info";
            }
        }
        return "default";
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

        List<RoleDisplayDto> rolesDisplay = user.getRoles().stream()
            .map(this::convertRoleToDisplayDto)
            .collect(Collectors.toList());
        dto.setRoles(rolesDisplay);

        return dto;
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof com.pos.shared.security.UserPrincipal) {
            return ((com.pos.shared.security.UserPrincipal) auth.getPrincipal()).getId();
        }

        throw new IllegalStateException("User not authenticated");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserResponse> dtos = users.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        List<User> activeUsers = userService.findAllByActiveTrue();
        List<UserResponse> dtos = activeUsers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam(required = false) String name) {
        List<User> users;
        if (name != null && !name.trim().isEmpty()) {
            users = userService.findByNameContainingIgnoreCase(name);
        } else {
            users = userService.findAll();
        }
        List<UserResponse> dtos = users.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        long totalUsers = userService.findAll().size();
        long activeUsers = userService.countByActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalUsers);
        stats.put("activos", activeUsers);
        stats.put("inactivos", inactiveUsers);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String roleName) {
        List<User> users = userService.findAllByRole(roleName);
        List<UserResponse> dtos = users.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        UserResponse dto = convertToDto(userOpt.get());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El nombre de usuario ya existe"));
        }

        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setPhone(request.getPhone());
        newUser.setActive(request.isActive());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setTenant(1L);
        newUser.setAvatarUrl(request.getAvatarUrl());
        newUser.setTimezone(request.getTimezone());
        newUser.setLocale(request.getLocale());

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = request.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId.longValue()).orElse(null))
                    .filter(r -> r != null)
                    .collect(Collectors.toSet());
            newUser.setRoles(roles);
        }

        User saved = userService.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        User user = userOpt.get();

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userService.existsByEmailAndIdNot(request.getEmail(), id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El email ya está en uso"));
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
            Set<Role> newRoles = request.getRoleIds().stream()
                    .map(rid -> roleRepository.findById(rid.longValue()).orElse(null))
                    .filter(r -> r != null)
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }

        try {
            User updated = userService.save(user);
            return ResponseEntity.ok(convertToDto(updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Long loggedInUserId = getCurrentUserId();
        if (id.equals(loggedInUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No puedes desactivar tu propia cuenta"));
        }

        if (id.equals(1L)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No se puede eliminar al Super Administrador"));
        }

        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        try {
            userService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al desactivar usuario: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        User user = userOpt.get();
        user.setActive(true);
        user.setLockedUntil(null);
        user.setLoginAttempts(0);

        User updated = userService.save(user);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        Long loggedInUserId = getCurrentUserId();
        if (id.equals(loggedInUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No puedes desactivar tu propia cuenta"));
        }

        if (id.equals(1L)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No se puede desactivar al Super Administrador"));
        }

        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        User user = userOpt.get();
        user.setActive(false);

        User updated = userService.save(user);
        return ResponseEntity.ok(convertToDto(updated));
    }
}

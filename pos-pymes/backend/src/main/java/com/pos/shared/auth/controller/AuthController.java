package com.pos.shared.auth.controller;

import com.pos.shared.auth.dto.LoginRequest;
import com.pos.shared.auth.dto.LoginResponse;
import com.pos.shared.auth.service.AuthService;
import com.pos.rol.model.Rol;
import com.pos.usuario.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error en autenticación", "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // La lógica de logout se maneja en el cliente eliminando el token
        return ResponseEntity.ok("Logout exitoso");
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        // La validación se hace en el filtro JWT
        return ResponseEntity.ok("Token válido");
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "OK", "service", "auth"));
    }
}
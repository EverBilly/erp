package com.pos.shared.auth.controller;

import com.pos.shared.auth.service.SecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {
    
    private final SecurityService securityService;
    
    public DashboardController(SecurityService securityService) {
        this.securityService = securityService;
    }
    
    @GetMapping("/menu")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMenu() {
        Long usuarioId = securityService.getCurrentUserId();
        if (usuarioId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        // Retornar menú básico o implementar lógica específica
        return ResponseEntity.ok("Menú del usuario " + usuarioId);
    }
    
    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPermissions() {
        // Usar getCurrentUserRoles en lugar de getCurrentUserPermissions
        var roles = securityService.getCurrentUserRoles();
        return ResponseEntity.ok(roles);
    }
    
    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRoles() {
        var roles = securityService.getCurrentUserRoles();
        return ResponseEntity.ok(roles);
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        // Implementar estadísticas básicas
        return ResponseEntity.ok("Estadísticas del dashboard");
    }
    
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        var usuario = securityService.getCurrentUser();
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        
        // Retornar información básica del perfil
        var profileInfo = new ProfileInfo(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombreCompleto(),
            usuario.getRoles()
        );
        
        return ResponseEntity.ok(profileInfo);
    }
    
    // Clase interna para la respuesta del perfil
    public static class ProfileInfo {
        private Long id;
        private String username;
        private String email;
        private String nombreCompleto;
        private Object roles;
        
        public ProfileInfo(Long id, String username, String email, String nombreCompleto, Object roles) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.nombreCompleto = nombreCompleto;
            this.roles = roles;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getNombreCompleto() { return nombreCompleto; }
        public Object getRoles() { return roles; }
    }
}
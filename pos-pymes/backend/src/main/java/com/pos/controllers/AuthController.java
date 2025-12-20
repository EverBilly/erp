package com.pos.controllers;

import com.pos.dto.LoginRequest;
import com.pos.dto.LoginResponse;
import com.pos.models.Usuario;
import com.pos.repositories.UsuarioRepository;
import com.pos.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    // === ENDPOINTS PÚBLICOS PARA DIAGNÓSTICO ===
    
    @GetMapping("/public-test")
    public ResponseEntity<?> publicTest() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "timestamp", System.currentTimeMillis(),
            "message", "Endpoint público funcionando"
        ));
    }
    
    @GetMapping("/diagnostic")
    public ResponseEntity<?> diagnostic() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Verificar AuthenticationManager
            boolean authManagerOk = authenticationManager != null;
            response.put("authManagerExists", authManagerOk);
            
            // 2. Verificar PasswordEncoder
            boolean passwordEncoderOk = passwordEncoder != null;
            response.put("passwordEncoderExists", passwordEncoderOk);
            
            // 3. Verificar repositorios
            boolean userRepoOk = usuarioRepository != null;
            response.put("userRepositoryExists", userRepoOk);
            
            // 4. Verificar usuario admin
            Usuario admin = usuarioRepository.findByUsername("admin").orElse(null);
            response.put("adminExists", admin != null);
            if (admin != null) {
                response.put("adminPasswordLength", admin.getPassword().length());
                response.put("adminPasswordIsBCrypt", 
                    admin.getPassword().startsWith("$2a$") || 
                    admin.getPassword().startsWith("$2b$") || 
                    admin.getPassword().startsWith("$2y$"));
            }
            
            // 5. Contar usuarios
            response.put("totalUsers", usuarioRepository.count());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/test-db")
    public ResponseEntity<?> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Contar usuarios
            long userCount = usuarioRepository.count();
            response.put("totalUsuarios", userCount);
            
            // Obtener usuario admin
            Usuario admin = usuarioRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                response.put("adminExiste", true);
                response.put("adminUsername", admin.getUsername());
                response.put("adminEmail", admin.getEmail());
                response.put("adminActivo", admin.isActivo());
                
                // Verificar contraseña
                String testPassword = "admin123";
                boolean passwordMatches = passwordEncoder.matches(testPassword, admin.getPassword());
                response.put("passwordMatch", passwordMatches);
                response.put("passwordLength", admin.getPassword().length());
                response.put("passwordStartsWith", admin.getPassword().substring(0, Math.min(10, admin.getPassword().length())));
            } else {
                response.put("adminExiste", false);
            }
            
            // Listar todos los usuarios
            response.put("usuarios", usuarioRepository.findAll().stream()
                .map(u -> Map.of(
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "email", u.getEmail(),
                    "activo", u.isActivo()
                ))
                .collect(java.util.stream.Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/test-auth-manual")
    public ResponseEntity<?> testAuthManual(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            System.out.println("=== TEST AUTH MANUAL ===");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            
            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authResult = authenticationManager.authenticate(authRequest);
            
            System.out.println("Authentication successful!");
            System.out.println("Principal: " + authResult.getName());
            System.out.println("Authorities: " + authResult.getAuthorities());
            
            response.put("success", true);
            response.put("authenticated", authResult.isAuthenticated());
            response.put("principal", authResult.getName());
            response.put("authorities", authResult.getAuthorities().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            System.out.println("Authentication FAILED!");
            System.out.println("Error: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", e.getClass().getSimpleName());
            response.put("message", e.getMessage());
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            System.out.println("General ERROR!");
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", "General Error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/generate-hash")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        String hash = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hash);
        
        response.put("password", password);
        response.put("hash", hash);
        response.put("hashLength", hash.length());
        response.put("selfCheck", matches);
        response.put("hashStartsWith", hash.substring(0, Math.min(20, hash.length())));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-all-users")
    public ResponseEntity<?> checkAllUsers() {
        List<Map<String, Object>> usersInfo = new ArrayList<>();
        
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        for (Usuario usuario : usuarios) {
            String password = usuario.getPassword();
            
            Map<String, Object> info = new HashMap<>();
            info.put("id", usuario.getId());
            info.put("username", usuario.getUsername());
            info.put("email", usuario.getEmail());
            info.put("activo", usuario.isActivo());
            info.put("passwordLength", password.length());
            info.put("passwordStartsWith", password.substring(0, Math.min(10, password.length())));
            info.put("isBCrypt", password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
            
            usersInfo.add(info);
        }
        
        return ResponseEntity.ok(Map.of(
            "totalUsers", usuarios.size(),
            "users", usersInfo
        ));
    }
    
    // === ENDPOINTS DE RESET ===
    
    @PostMapping("/reset-admin-password")
    public ResponseEntity<?> resetAdminPassword() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Usuario admin = usuarioRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Usuario admin no encontrado"));
            
            // Contraseña: admin123
            String newHashedPassword = passwordEncoder.encode("admin123");
            admin.setPassword(newHashedPassword);
            usuarioRepository.save(admin);
            
            response.put("success", true);
            response.put("message", "Contraseña resetada exitosamente");
            response.put("newPasswordHash", newHashedPassword);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/hash-all-passwords")
    public ResponseEntity<?> hashAllPasswords() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> updatedUsers = new ArrayList<>();
        
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        for (Usuario usuario : usuarios) {
            String currentPassword = usuario.getPassword();
            
            // Solo hashear si NO es BCrypt
            if (!currentPassword.startsWith("$2a$") && 
                !currentPassword.startsWith("$2b$") && 
                !currentPassword.startsWith("$2y$")) {
                
                // Si la contraseña parece ser "admin123" (basado en username)
                String newPassword;
                if (usuario.getUsername().equals("admin")) {
                    newPassword = "admin123";
                } else {
                    newPassword = "password123"; // Contraseña por defecto
                }
                
                String hashedPassword = passwordEncoder.encode(newPassword);
                usuario.setPassword(hashedPassword);
                usuarioRepository.save(usuario);
                
                updatedUsers.add(Map.of(
                    "id", usuario.getId(),
                    "username", usuario.getUsername(),
                    "oldPasswordLength", currentPassword.length(),
                    "newPasswordHashed", true,
                    "defaultPassword", newPassword
                ));
            }
        }
        
        response.put("success", true);
        response.put("message", "Contraseñas actualizadas");
        response.put("updatedCount", updatedUsers.size());
        response.put("updatedUsers", updatedUsers);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/create-correct-user")
    public ResponseEntity<?> createCorrectUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.getOrDefault("email", username + "@pos.com");
            String nombre = request.getOrDefault("nombre", "Usuario");
            String apellido = request.getOrDefault("apellido", "Test");
            
            if (usuarioRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Usuario ya existe"
                ));
            }
            
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(password)); // ¡IMPORTANTE: HASH!
            usuario.setEmail(email);
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setActivo(true);
            
            usuarioRepository.save(usuario);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario creado correctamente",
                "credentials", username + " / " + password,
                "hashedPassword", usuario.getPassword().substring(0, 20) + "..."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/fix-admin-complete")
    public ResponseEntity<?> fixAdminComplete() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Obtener o crear usuario admin
            Usuario admin = usuarioRepository.findByUsername("admin").orElse(new Usuario());
            
            // 2. Resetear todos los campos
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Nueva contraseña
            admin.setEmail("admin@pos.com");
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setActivo(true);
            
            // 3. Guardar
            usuarioRepository.save(admin);
            
            // 4. Verificar
            boolean verify = passwordEncoder.matches("admin123", admin.getPassword());
            
            response.put("success", true);
            response.put("message", "Usuario admin reseteado completamente");
            response.put("credentials", "admin / admin123");
            response.put("verification", verify ? "OK" : "FAILED");
            response.put("newHash", admin.getPassword());
            response.put("newHashStartsWith", admin.getPassword().substring(0, 20));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/check-admin-roles")
    public ResponseEntity<?> checkAdminRoles() {
        try {
            Usuario admin = usuarioRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));
            
            List<String> roles = admin.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(java.util.stream.Collectors.toList());
            
            List<String> permisos = admin.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .map(permiso -> permiso.getNombre())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "username", admin.getUsername(),
                "roles", roles,
                "permisos", permisos,
                "rolesCount", roles.size(),
                "permisosCount", permisos.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    // === ENDPOINT DE LOGIN PRINCIPAL ===
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of(
                "error", "Autenticación fallida",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok().body("Token válido");
    }
}
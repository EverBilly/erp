package com.pos.usuario.controller;

import com.pos.usuario.model.Usuario;
import com.pos.rol.model.Rol;
import com.pos.usuario.service.UsuarioService;
import com.pos.usuario.dto.UsuarioResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Método auxiliar para convertir Usuario a UsuarioResponse
    private UsuarioResponse convertToDto(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setTelefono(usuario.getTelefono());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setUltimoLogin(usuario.getUltimoLogin());
        
        // Extraer solo los nombres de los roles
        List<String> nombresRoles = usuario.getRoles().stream()
            .map(Rol::getNombre)
            .collect(Collectors.toList());
        dto.setRoles(nombresRoles);
        
        return dto;
    }
    
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        List<UsuarioResponse> dtos = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        UsuarioResponse dto = convertToDto(usuarioOpt.get());
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<?> createUsuario(@Valid @RequestBody Usuario usuario) {
        // Validar que el username no exista
        if (usuarioService.existsByUsername(usuario.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El nombre de usuario ya existe"));
        }
        
        // Validar que el email no exista
        if (usuarioService.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El email ya está registrado"));
        }
        
        Usuario nuevoUsuario = usuarioService.save(usuario);
        UsuarioResponse dto = convertToDto(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id, @Valid @RequestBody Usuario usuarioDetails) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Validar que el email no esté en uso por otro usuario
        if (usuarioDetails.getEmail() != null && 
            !usuarioDetails.getEmail().equals(usuario.getEmail()) &&
            usuarioService.existsByEmailAndIdNot(usuarioDetails.getEmail(), id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El email ya está registrado por otro usuario"));
        }
        
        // Actualizar campos
        if (usuarioDetails.getNombreCompleto() != null) {
            usuario.setNombreCompleto(usuarioDetails.getNombreCompleto());
        }
        
        if (usuarioDetails.getEmail() != null) {
            usuario.setEmail(usuarioDetails.getEmail());
        }
        
        if (usuarioDetails.getTelefono() != null) {
            usuario.setTelefono(usuarioDetails.getTelefono());
        }
        
        if (usuarioDetails.getAvatarUrl() != null) {
            usuario.setAvatarUrl(usuarioDetails.getAvatarUrl());
        }
        
        if (usuarioDetails.getTimezone() != null) {
            usuario.setTimezone(usuarioDetails.getTimezone());
        }
        
        if (usuarioDetails.getIdioma() != null) {
            usuario.setIdioma(usuarioDetails.getIdioma());
        }
        
        if (usuarioDetails.getMetadata() != null) {
            usuario.setMetadata(usuarioDetails.getMetadata());
        }
        
        if (usuarioDetails.getActivo() != null) {
            usuario.setActivo(usuarioDetails.getActivo());
        }
        
        // Solo actualizar password si se proporciona
        if (usuarioDetails.getPasswordHash() != null && !usuarioDetails.getPasswordHash().isEmpty()) {
            usuario.setPasswordHash(usuarioDetails.getPasswordHash());
        }
        
        Usuario usuarioActualizado = usuarioService.save(usuario);
        UsuarioResponse dto = convertToDto(usuarioActualizado);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        
        usuarioService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
    }
    
    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(true);
        usuario.setBloqueadoHasta(null);
        usuario.setIntentosLogin(0);
        
        Usuario usuarioActualizado = usuarioService.save(usuario);
        UsuarioResponse dto = convertToDto(usuarioActualizado);
        return ResponseEntity.ok(dto);
    }
    
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(false);
        
        Usuario usuarioActualizado = usuarioService.save(usuario);
        UsuarioResponse dto = convertToDto(usuarioActualizado);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponse>> buscarUsuarios(@RequestParam(required = false) String nombre) {
        List<Usuario> usuarios;
        if (nombre != null && !nombre.trim().isEmpty()) {
            usuarios = usuarioService.findByNombreContainingIgnoreCase(nombre);
        } else {
            usuarios = usuarioService.findAll();
        }
        List<UsuarioResponse> dtos = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        long totalUsuarios = usuarioService.findAll().size();
        long usuariosActivos = usuarioService.countByActivoTrue();
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalUsuarios", totalUsuarios);
        estadisticas.put("usuariosActivos", usuariosActivos);
        estadisticas.put("usuariosInactivos", totalUsuarios - usuariosActivos);
        
        return ResponseEntity.ok(estadisticas);
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioResponse>> getUsuariosActivos() {
        List<Usuario> usuariosActivos = usuarioService.findAllByActivoTrue();
        List<UsuarioResponse> dtos = usuariosActivos.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/rol/{rolNombre}")
    public ResponseEntity<List<UsuarioResponse>> getUsuariosPorRol(@PathVariable String rolNombre) {
        List<Usuario> usuarios = usuarioService.findAllByRole(rolNombre);
        List<UsuarioResponse> dtos = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable String username) {
        boolean existe = usuarioService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@PathVariable String email) {
        boolean existe = usuarioService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }
}
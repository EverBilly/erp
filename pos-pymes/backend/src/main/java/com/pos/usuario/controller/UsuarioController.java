package com.pos.usuario.controller;

import com.pos.usuario.model.Usuario;
import com.pos.rol.model.Rol;
import com.pos.tenant.model.Tenant;
import com.pos.usuario.service.UsuarioService;
import com.pos.usuario.dto.UsuarioResponse;
import com.pos.usuario.dto.CrearUsuarioRequest;
import com.pos.usuario.dto.ActualizarUsuarioRequest;
import com.pos.rol.repository.RolRepository;
import com.pos.usuario.dto.RolDisplayDto;
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
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService,
                            RolRepository rolRepository,
                            PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método auxiliar para convertir Rol a RolDisplayDto (dinámico)
    private RolDisplayDto convertRolToDisplayDto(Rol rol) {
        String displayName = rol.getDescription() != null && !rol.getDescription().trim().isEmpty()
            ? rol.getDescription()
            : rol.getName();

        String color = getRoleColor(rol.getName()); // Asignar color dinámicamente

        return new RolDisplayDto(
            rol.getName(),
            displayName,
            rol.getDescription() != null ? rol.getDescription() : "Sin descripción",
            color,
            rol.getPriorityLevel()
        );
    }

    // Método para asignar colores dinámicamente basado en nivel de prioridad
    private String getRoleColor(String nombreRol) {
        // Podríamos también basarlo en nivel de prioridad
        if ("SUPER_ADMIN".equals(nombreRol)) return "error";
        if ("ADMIN".equals(nombreRol)) return "warning";
        if ("USER".equals(nombreRol)) return "info";

        // Para roles personalizados, basar en nivel de prioridad
        Optional<Rol> rolOpt = rolRepository.findByNombre(nombreRol);
        if (rolOpt.isPresent()) {
            Integer prioridad = rolOpt.get().getPriorityLevel();
            if (prioridad != null) {
                if (prioridad >= 900) return "error";      // Rojo para altos privilegios
                if (prioridad >= 500) return "warning";    // Amarillo para medio
                if (prioridad > 0) return "info";          // Azul para bajos
            }
        }
        return "default"; // Gris para roles sin prioridad
    }

    // Método auxiliar para convertir Usuario a UsuarioResponse
    private UsuarioResponse convertToDto(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setFullName(usuario.getFullName());
        dto.setPhone(usuario.getPhone());
        dto.setActive(usuario.getActive());
        dto.setCreatedAt(usuario.getCreatedAt());
        dto.setLastLogin(usuario.getLastLogin());

        // Convertir roles a formato amigable con metadata
        List<RolDisplayDto> rolesDisplay = usuario.getRoles().stream()
            .map(this::convertRolToDisplayDto)
            .collect(Collectors.toList());
        dto.setRoles(rolesDisplay);

        return dto;
    }

    private Long obtenerUserIdDelContexto() {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof com.pos.shared.security.UserPrincipal) {
            return ((com.pos.shared.security.UserPrincipal) auth.getPrincipal()).getId();
        }

        throw new IllegalStateException("Usuario no autenticado");
    }

    // ENDPOINTS FIJOS PRIMERO (sin @PathVariable)

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        List<UsuarioResponse> dtos = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioResponse>> getUsuariosActivos() {
        List<Usuario> usuariosActivos = usuarioService.findAllByActivoTrue();
        List<UsuarioResponse> dtos = usuariosActivos.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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

    @GetMapping("/contar")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        long totalUsuarios = usuarioService.findAll().size();
        long usuariosActivos = usuarioService.countByActivoTrue();
        long usuariosInactivos = totalUsuarios - usuariosActivos;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("total", totalUsuarios);
        estadisticas.put("activos", usuariosActivos);
        estadisticas.put("inactivos", usuariosInactivos);

        return ResponseEntity.ok(estadisticas);
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

    // ENDPOINTS CON @PATHVARIABLE ÚLTIMO (después de todos los fijos)

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
    public ResponseEntity<?> createUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        if (usuarioService.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El nombre de usuario ya existe"));
        }

        if (usuarioService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(request.getUsername());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setFullName(request.getFullName());
        nuevoUsuario.setPhone(request.getPhone());
        nuevoUsuario.setActive(request.isActive());
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // <--- ASIGNACIÓN TENANT Y METADATA ---
        nuevoUsuario.setTenant(1L);
        // Comentado temporalmente para evitar el error de tipo String->Map
        // if (request.getMetadata() != null) { nuevoUsuario.setMetadata(request.getMetadata()); }
        nuevoUsuario.setAvatarUrl(request.getAvatarUrl());
        nuevoUsuario.setTimezone(request.getTimezone());
        nuevoUsuario.setLocale(request.getLocale());

        // Asignar Roles
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Rol> roles = request.getRoleIds().stream()
                    .map(id -> rolRepository.findById(id.longValue()).orElse(null))
                    .filter(r -> r != null)
                    .collect(Collectors.toSet());
            nuevoUsuario.setRoles(roles);
        }

        Usuario guardado = usuarioService.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioService.existsByEmailAndIdNot(request.getEmail(), id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El email ya está en uso"));
            }
        }

        if (request.getFullName() != null) {
            usuario.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            usuario.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            usuario.setPhone(request.getPhone());
        }

        if (request.getAvatarUrl() != null) {
            usuario.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getTimezone() != null) {
            usuario.setTimezone(request.getTimezone());
        }
        if (request.getLocale() != null) {
            usuario.setLocale(request.getLocale());
        }
        if (request.getMetadata() != null) {
            usuario.setMetadata(request.getMetadata());
        }
        if (request.getActive() != null) {
            usuario.setActive(request.getActive());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleIds() != null) {
            Set<Rol> nuevosRoles = request.getRoleIds().stream()
                    .map(rid -> rolRepository.findById(rid.longValue()).orElse(null))
                    .filter(r -> r != null)
                    .collect(Collectors.toSet());
            usuario.setRoles(nuevosRoles);
        }

        try {
            Usuario actualizado = usuarioService.save(usuario);
            return ResponseEntity.ok(convertToDto(actualizado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        // Protección: No borrar al superadmin (ID 1) ni a uno mismo
        Long userIdLogueado = obtenerUserIdDelContexto();
        if (id.equals(userIdLogueado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No puedes desactivar tu propia cuenta"));
        }

        if (id.equals(1L)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No se puede eliminar al Super Administrador"));
        }

        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        try {
            usuarioService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al desactivar usuario: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setActive(true);
        usuario.setLockedUntil(null);
        usuario.setLoginAttempts(0);

        Usuario usuarioActualizado = usuarioService.save(usuario);
        UsuarioResponse dto = convertToDto(usuarioActualizado);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id) {
        Long userIdLogueado = obtenerUserIdDelContexto();
        if (id.equals(userIdLogueado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No puedes desactivar tu propia cuenta"));
        }

        if (id.equals(1L)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No se puede desactivar al Super Administrador"));
        }

        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setActive(false);

        Usuario usuarioActualizado = usuarioService.save(usuario);
        return ResponseEntity.ok(convertToDto(usuarioActualizado));
    }
}

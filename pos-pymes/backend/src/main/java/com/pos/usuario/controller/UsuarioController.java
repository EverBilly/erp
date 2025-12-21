package com.pos.usuario.controller;

import com.pos.usuario.dto.ActualizarUsuarioRequest;
import com.pos.usuario.dto.CrearUsuarioRequest;
import com.pos.usuario.dto.UsuarioResponse;
import com.pos.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de usuarios.
 *
 * Endpoints:
 * - GET    /api/usuarios          -> Listar todos
 * - GET    /api/usuarios/{id}     -> Obtener por ID
 * - POST   /api/usuarios          -> Crear usuario
 * - PUT    /api/usuarios/{id}     -> Actualizar usuario
 * - PATCH  /api/usuarios/{id}/desactivar -> Desactivar (soft delete)
 *
 * Las excepciones son manejadas por GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista todos los usuarios.
     * GET /api/usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene un usuario por ID.
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Crea un nuevo usuario.
     * POST /api/usuarios
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    /**
     * Actualiza un usuario existente.
     * PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Desactiva un usuario (soft delete).
     * PATCH /api/usuarios/{id}/desactivar
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponse> desactivarUsuario(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(usuario);
    }
}

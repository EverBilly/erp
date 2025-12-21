package com.pos.usuario.service;

import com.pos.usuario.dto.ActualizarUsuarioRequest;
import com.pos.usuario.dto.CrearUsuarioRequest;
import com.pos.usuario.dto.UsuarioResponse;
import com.pos.usuario.exception.UsuarioDuplicadoException;
import com.pos.usuario.exception.UsuarioNotFoundException;
import com.pos.usuario.model.Usuario;
import com.pos.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Lista todos los usuarios como DTOs (sin exponer password).
     * Este es el método que debe usar el Controller.
     */
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por ID y lo retorna como DTO.
     * Lanza excepción si no existe.
     */
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        return convertirAResponse(usuario);
    }

    /**
     * Crea un nuevo usuario desde un DTO de request.
     * Valida unicidad de username y email.
     * Hashea el password antes de guardar.
     */
    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request) {
        // Validar unicidad de username
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new UsuarioDuplicadoException("username", request.getUsername());
        }

        // Validar unicidad de email
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioDuplicadoException("email", request.getEmail());
        }

        // Crear entidad Usuario desde el request
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setActivo(true);

        // Guardar y retornar como DTO
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertirAResponse(usuarioGuardado);
    }

    /**
     * Actualiza un usuario existente.
     * Solo modifica los campos que vienen en el request (no nulos).
     */
    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, ActualizarUsuarioRequest request) {
        // Buscar usuario existente
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        // Validar unicidad de email si se proporciona uno nuevo
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new UsuarioDuplicadoException("email", request.getEmail());
            }
            usuario.setEmail(request.getEmail());
        }

        // Actualizar solo campos no nulos
        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getApellido() != null) {
            usuario.setApellido(request.getApellido());
        }
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Guardar y retornar
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertirAResponse(usuarioActualizado);
    }

    /**
     * Desactiva un usuario (soft delete).
     * El usuario no se elimina, solo se marca como inactivo.
     */
    @Transactional
    public UsuarioResponse desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        usuario.setActivo(false);
        Usuario usuarioDesactivado = usuarioRepository.save(usuario);
        return convertirAResponse(usuarioDesactivado);
    }

    /**
     * Convierte una entidad Usuario a UsuarioResponse.
     * Método privado para no exponer detalles de implementación.
     */
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        List<String> nombresRoles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toList());

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.isActivo(),
                usuario.getFechaCreacion(),
                usuario.getFechaUltimoLogin(),
                nombresRoles
        );
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null && usuario.getPassword() != null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario update(Long id, Usuario usuarioDetails) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        usuario.setNombre(usuarioDetails.getNombre());
        usuario.setApellido(usuarioDetails.getApellido());
        usuario.setEmail(usuarioDetails.getEmail());
        usuario.setActivo(usuarioDetails.isActivo());

        // Si se proporciona una nueva contraseña, encriptarla
        if (usuarioDetails.getPassword() != null && !usuarioDetails.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDetails.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public List<Usuario> searchByNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public long countActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    public List<Usuario> findAllActivos() {
        return usuarioRepository.findAllByActivoTrue();
    }
}

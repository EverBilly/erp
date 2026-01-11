package com.pos.usuario.service;

import com.pos.usuario.model.Usuario;
import com.pos.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    @Transactional
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }
        
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isEmpty()) {
            usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        }
        
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void softDelete(Long id) {
        Usuario usuario = findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        // Simplemente desactivamos el usuario
        usuario.setActivo(false);
        
        // Opcional: Limpiamos el bloqueo para facilitar la reactivación
        usuario.setBloqueadoHasta(null);
        usuario.setIntentosLogin(0);
        
        // Guardamos. Las relaciones (Roles, Tenant) se mantienen intactas.
        usuarioRepository.save(usuario);
    }
    
    @Transactional
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id); 
    }
    
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByEmailAndIdNot(String email, Long id) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        return usuario.isPresent() && !usuario.get().getId().equals(id);
    }
    
    @Transactional
    public void incrementarIntentosFallidos(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setIntentosLogin(usuario.getIntentosLogin() + 1);
            usuarioRepository.save(usuario);
        });
    }
    
    @Transactional
    public void resetIntentosFallidos(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setIntentosLogin(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);
        });
    }
    
    @Transactional
    public void bloquearUsuario(Long usuarioId, LocalDateTime bloqueadoHasta) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setBloqueadoHasta(bloqueadoHasta);
            usuarioRepository.save(usuario);
        });
    }
    
    @Transactional
    public void actualizarUltimoLogin(Long usuarioId, LocalDateTime ultimoLogin) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setUltimoLogin(ultimoLogin);
            usuarioRepository.save(usuario);
        });
    }
    
    @Transactional(readOnly = true)
    public List<Usuario> findByNombreContainingIgnoreCase(String nombre) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getNombreCompleto() != null && 
                           u.getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public long countByActivoTrue() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::getActivo)
                .count();
    }
    
    @Transactional(readOnly = true)
    public List<Usuario> findAllByActivoTrue() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::getActivo)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Usuario> findAllByRole(String rolNombre) {
        // Implementación simplificada (filtro en memoria) para evitar queries JPQL complejos ahora
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && 
                           u.getRoles().stream().anyMatch(r -> r.getNombre().equals(rolNombre)))
                .toList();
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional(readOnly = true)
    public long count() {
        return usuarioRepository.count();
    }
}
package com.pos.shared.auth.service;

import com.pos.shared.security.JwtTokenProvider;
import com.pos.rol.model.Rol;
import com.pos.usuario.model.Usuario;
import com.pos.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityService {
    
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider tokenProvider;
    
    public SecurityService(UsuarioRepository usuarioRepository, JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.tokenProvider = tokenProvider;
    }
    
    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = null;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }
        
        if (username == null) {
            return null;
        }
        
        return usuarioRepository.findByUsername(username).orElse(null);
    }
    
    public Long getCurrentUserId() {
        Usuario usuario = getCurrentUser();
        return usuario != null ? usuario.getId() : null;
    }
    
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }
    
    @Transactional(readOnly = true)
    public List<String> getCurrentUserPermissions() {
        // Implementación básica - puedes mejorarla según tu estructura de permisos
        Usuario usuario = getCurrentUser();
        if (usuario == null) {
            return Collections.emptyList();
        }
        
        // Por ahora, devolver los nombres de los roles como "permisos"
        return usuario.getRoles().stream()
                .map(rol -> "ROL_" + rol.getNombre().toUpperCase())
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<String> getCurrentUserRoles() {
        Usuario usuario = getCurrentUser();
        if (usuario == null) {
            return Collections.emptyList();
        }
        
        return usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toList());
    }
    
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }
}
package com.pos.shared.auth.service;

import com.pos.shared.auth.dto.LoginRequest;
import com.pos.shared.auth.dto.LoginResponse;
import com.pos.shared.security.JwtTokenProvider;
import com.pos.shared.security.UserPrincipal;
import com.pos.usuario.model.Usuario;
import com.pos.usuario.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AuthService(AuthenticationManager authenticationManager,
                      JwtTokenProvider tokenProvider,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        // 1. Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // 2. Verificar que esté activo
        if (!usuario.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // 3. Verificar contraseña (solución simple sin AuthenticationManager)
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        
        // 4. Crear autenticación simple
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            usuario.getUsername(),
            null  // credentials null porque ya verificamos la contraseña
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 5. Generar token JWT
        String jwt = tokenProvider.generateTokenFromUsername(usuario.getUsername());
        
        // 6. Actualizar último login
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        // 7. Crear UserPrincipal para la respuesta
        UserPrincipal userPrincipal = UserPrincipal.create(usuario);

        // 8. Crear y devolver respuesta
        return new LoginResponse(
            jwt,
            "Bearer",
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombreCompleto(),
            userPrincipal.getAuthorities()
        );
    }
    
    // public boolean validatePassword(String rawPassword, String encodedPassword) {
    //     return passwordEncoder.matches(rawPassword, encodedPassword);
    // }
    
    // public String encodePassword(String rawPassword) {
    //     return passwordEncoder.encode(rawPassword);
    // }
}
package com.pos.services;

import com.pos.dto.LoginRequest;
import com.pos.dto.LoginResponse;
import com.pos.models.Usuario;
import com.pos.repositories.UsuarioRepository;
import com.pos.security.JwtTokenProvider;
import com.pos.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("=== INICIO authenticateUser ===");
        logger.info("Username recibido: {}", loginRequest.getUsername());
        
        try {
            logger.info("Creando UsernamePasswordAuthenticationToken...");
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                );
            
            logger.info("Llamando authenticationManager.authenticate()...");
            Authentication authentication = authenticationManager.authenticate(authToken);
            logger.info("Autenticación exitosa!");
            
            logger.info("Estableciendo SecurityContext...");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.info("Generando token JWT...");
            String jwt = tokenProvider.generateToken(authentication);
            logger.info("Token generado: {}...", jwt.substring(0, Math.min(50, jwt.length())));
            
            logger.info("Obteniendo UserPrincipal...");
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("UserPrincipal ID: {}", userPrincipal.getId());
            logger.info("UserPrincipal Username: {}", userPrincipal.getUsername());
            
            logger.info("Buscando usuario en BD...");
            Usuario usuario = usuarioRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado en BD con ID: {}", userPrincipal.getId());
                    return new RuntimeException("Usuario no encontrado");
                });
            
            logger.info("Usuario encontrado: {}", usuario.getUsername());
            
            // Actualizar fecha de último login
            logger.info("Actualizando fecha de último login...");
            usuario.setFechaUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            // Obtener roles y permisos
            logger.info("Obteniendo roles...");
            List<String> roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toList());
            logger.info("Roles encontrados: {}", roles);
            
            logger.info("Obteniendo permisos...");
            List<String> permisos = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .map(permiso -> permiso.getNombre())
                .distinct()
                .collect(Collectors.toList());
            logger.info("Permisos encontrados: {} permisos", permisos.size());
            
            logger.info("=== FIN authenticateUser - ÉXITO ===");
            
            return new LoginResponse(
                jwt,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                roles,
                permisos
            );
            
        } catch (BadCredentialsException e) {
            logger.error("=== BadCredentialsException ===");
            logger.error("Credenciales incorrectas para usuario: {}", loginRequest.getUsername());
            logger.error("Mensaje: {}", e.getMessage());
            throw new RuntimeException("Usuario o contraseña incorrectos");
            
        } catch (Exception e) {
            logger.error("=== EXCEPCIÓN GENERAL ===");
            logger.error("Error en authenticateUser: {}", e.getMessage());
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("Error en autenticación: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
package com.pos.shared.auth.service;

import com.pos.shared.auth.dto.LoginRequest;
import com.pos.shared.auth.dto.LoginResponse;
import com.pos.shared.security.JwtTokenProvider;
import com.pos.shared.security.UserPrincipal;
import com.pos.user.model.User;
import com.pos.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtTokenProvider tokenProvider,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!user.getActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        if (user.isLocked()) {
            throw new RuntimeException("Cuenta bloqueada temporalmente. Intente más tarde.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        user.setLastLogin(LocalDateTime.now());
        user.setLoginAttempts(0);
        userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateTokenFromUsername(userPrincipal.getUsername());

        log.info("Login exitoso: {}", user.getUsername());

        return new LoginResponse(
            jwt, "Bearer",
            user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
            userPrincipal.getAuthorities()
        );
    }
}

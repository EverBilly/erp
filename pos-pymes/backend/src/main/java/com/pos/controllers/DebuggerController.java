package com.pos.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebuggerController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/test-auth")
    public Map<String, Object> testAuthentication(
            @RequestParam String username,
            @RequestParam String password) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authResult = authenticationManager.authenticate(authRequest);
            
            response.put("success", true);
            response.put("authenticated", authResult.isAuthenticated());
            response.put("principal", authResult.getName());
            response.put("authorities", authResult.getAuthorities().toString());
            
        } catch (AuthenticationException e) {
            response.put("success", false);
            response.put("error", e.getClass().getSimpleName());
            response.put("message", e.getMessage());
            
            // Información adicional
            response.put("errorDetails", e.toString());
        }
        
        return response;
    }
    
    @GetMapping("/generate-bcrypt")
    public Map<String, Object> generateBcrypt(@RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        String hash = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hash);
        
        response.put("password", password);
        response.put("hash", hash);
        response.put("hashLength", hash.length());
        response.put("selfCheck", matches);
        response.put("hashPrefix", hash.substring(0, Math.min(30, hash.length())));
        
        return response;
    }
}
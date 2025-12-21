package com.pos.shared.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-in-ms:86400000}")
    private int jwtExpirationInMs;

    private SecretKey getSigningKey() {
        // Si no hay secret o es muy corto, usar uno por defecto
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            jwtSecret = "MySuperSecureKeyForPOSApplicationThatIsAtLeast64CharactersLongForHS512Algorithm2024!";
        }

        // Asegurar que la clave tenga al menos 64 caracteres (512 bits)
        if (jwtSecret.length() < 64) {
            // Extender la clave si es muy corta
            StringBuilder extendedKey = new StringBuilder(jwtSecret);
            while (extendedKey.length() < 64) {
                extendedKey.append("0");
            }
            jwtSecret = extendedKey.toString();
        }

        // Convertir a bytes y crear clave segura
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            return Jwts.builder()
                    .setSubject(Long.toString(userPrincipal.getId()))
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .claim("username", userPrincipal.getUsername())
                    .claim("nombre", userPrincipal.getNombreCompleto())
                    .claim("email", userPrincipal.getEmail())
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            System.err.println("Error generating JWT token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error generating JWT token: " + e.getMessage());
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            System.err.println("Error parsing JWT token: " + e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (Exception ex) {
            System.err.println("Error validating token: " + ex.getMessage());
            return false;
        }
    }
}

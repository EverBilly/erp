package com.pos.shared.auth.dto;

import com.pos.menu.dto.MenuDto;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.List;

public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String nombreCompleto;
    private Collection<? extends GrantedAuthority> roles;
    private List<MenuDto> menus;

    public LoginResponse() {}
    
    public LoginResponse(String token, String tokenType, Long id, 
                        String username, String email, String nombreCompleto,
                        Collection<? extends GrantedAuthority> roles) {
        this.token = token;
        this.tokenType = tokenType;
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.roles = roles;
    }
    
    // Getters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    public Collection<? extends GrantedAuthority> getRoles() {
        return roles;
    }

    public void setRoles(Collection<? extends GrantedAuthority> roles) {
        this.roles = roles;
    }

    public List<MenuDto> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuDto> menus) {
        this.menus = menus;
    }
}
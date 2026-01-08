package com.pos.shared.security;

import com.pos.usuario.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String username;
    private String email;
    
    @JsonIgnore
    private String password;
    
    private String nombreCompleto;
    private Boolean activo;
    
    private Collection<? extends GrantedAuthority> authorities;
    
    public UserPrincipal(Long id, String username, String email, String password, 
                        String nombreCompleto, Boolean activo,
                        Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.activo = activo;
        this.authorities = authorities;
    }
    
    public static UserPrincipal create(Usuario usuario) {
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getNombre()))
                .collect(Collectors.toList());
        
        return new UserPrincipal(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getPasswordHash(),
            usuario.getNombreCompleto(),
            usuario.getActivo(),
            authorities
        );
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    // Implementación de UserDetails
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return activo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
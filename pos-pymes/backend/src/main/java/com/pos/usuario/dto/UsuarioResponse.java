package com.pos.usuario.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UsuarioResponse {
    
    private Long id;
    private String username;
    private String email;
    private String nombreCompleto;
    private String telefono;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoLogin;
    private List<String> roles;
    
    // Constructores
    public UsuarioResponse() {
    }
    
    public UsuarioResponse(Long id, String username, String email, String nombreCompleto, 
                          String telefono, boolean activo, LocalDateTime fechaCreacion,
                          LocalDateTime ultimoLogin, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.ultimoLogin = ultimoLogin;
        this.roles = roles;
    }
    
    // Getters y Setters
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
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public boolean getActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }
    
    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
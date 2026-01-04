package com.pos.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class ActualizarUsuarioRequest {
    
    @Size(min = 3, max = 50, message = "El nombre completo debe tener entre 3 y 50 caracteres")
    private String nombreCompleto;
    
    @Email(message = "El email debe ser válido")
    private String email;
    
    private String telefono;
    
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    // Getters y Setters
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
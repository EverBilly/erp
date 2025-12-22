package com.pos.usuario.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de usuario.
 * NUNCA incluye el password.
 *
 * Principio SOLID aplicado:
 * - Single Responsibility: Solo transporta datos de respuesta
 * - Este DTO es diferente de la Entity porque:
 *   1. No expone password
 *   2. Roles son solo strings (nombres), no objetos completos
 *   3. Desacopla la API de la base de datos
 */
public class UsuarioResponse {

    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimoLogin;
    private List<String> roles;

    // Constructor vacío (necesario para Jackson)
    public UsuarioResponse() {}

    // Constructor completo
    public UsuarioResponse(Long id, String username, String email, String nombre,
                          String apellido, boolean activo, LocalDateTime fechaCreacion,
                          LocalDateTime fechaUltimoLogin, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaUltimoLogin = fechaUltimoLogin;
        this.roles = roles;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaUltimoLogin() { return fechaUltimoLogin; }
    public void setFechaUltimoLogin(LocalDateTime fechaUltimoLogin) { this.fechaUltimoLogin = fechaUltimoLogin; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

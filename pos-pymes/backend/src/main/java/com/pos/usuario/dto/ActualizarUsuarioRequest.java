package com.pos.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO para actualizar un usuario existente.
 *
 * Todos los campos son opcionales porque solo se actualizan
 * los campos que se envían en el request.
 *
 * Nota: El username NO se puede cambiar (no está en este DTO).
 */
public class ActualizarUsuarioRequest {

    @Email(message = "El email debe tener un formato válido")
    private String email;

    private String nombre;

    private String apellido;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    private Boolean activo;

    private List<Long> rolIds;

    // Constructor vacío
    public ActualizarUsuarioRequest() {}

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public List<Long> getRolIds() { return rolIds; }
    public void setRolIds(List<Long> rolIds) { this.rolIds = rolIds; }
}

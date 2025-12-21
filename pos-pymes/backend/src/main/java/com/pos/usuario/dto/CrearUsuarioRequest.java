package com.pos.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO para crear un nuevo usuario.
 *
 * Las validaciones están aquí (no en la Entity) porque:
 * 1. Las reglas de validación pueden ser diferentes según el contexto
 * 2. Mantiene la Entity limpia
 * 3. Facilita mensajes de error específicos para la API
 */
public class CrearUsuarioRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private List<Long> rolIds;  // Opcional: IDs de roles a asignar

    // Constructor vacío
    public CrearUsuarioRequest() {}

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public List<Long> getRolIds() { return rolIds; }
    public void setRolIds(List<Long> rolIds) { this.rolIds = rolIds; }
}

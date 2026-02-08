package com.pos.usuario.dto;

public class RolDisplayDto {
    private String nombre;
    private String displayName;
    private String descripcion;
    private String color;
    private Integer nivelPrioridad;

    public RolDisplayDto() {}
    
    public RolDisplayDto(String nombre, String displayName, String descripcion, String color, Integer nivelPrioridad) {
        this.nombre = nombre;
        this.displayName = displayName;
        this.descripcion = descripcion;
        this.color = color;
        this.nivelPrioridad = nivelPrioridad;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public Integer getNivelPrioridad() { return nivelPrioridad; }
    public void setNivelPrioridad(Integer nivelPrioridad) { this.nivelPrioridad = nivelPrioridad; }
}
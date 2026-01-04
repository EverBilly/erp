package com.pos.permiso.model;

import com.pos.rol.model.Rol;  // <-- Import actualizado
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permisos")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    private String descripcion;

    @Column(length = 50)
    private String modulo;
    
    @Column(length = 50)
    private String categoria;
    
    @Column(name = "nivel_seguridad")
    private Integer nivelSeguridad = 1;
    
    private Boolean activo = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "permisos", fetch = FetchType.LAZY)
    private Set<Rol> roles = new HashSet<>();

    // Constructores
    public Permiso() {}

    public Permiso(String codigo, String nombre, String modulo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.modulo = modulo;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getModulo() {
        return modulo;
    }
    
    public void setModulo(String modulo) {
        this.modulo = modulo;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public Integer getNivelSeguridad() {
        return nivelSeguridad;
    }
    
    public void setNivelSeguridad(Integer nivelSeguridad) {
        this.nivelSeguridad = nivelSeguridad;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Set<Rol> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }
}
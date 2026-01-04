package com.pos.rol.model;

import com.pos.usuario.model.Usuario;  // <-- Import actualizado
import com.pos.permiso.model.Permiso;  // <-- Import actualizado
import com.pos.menu.model.Menu;      // <-- Import actualizado
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre;

    private String descripcion;

    @Column(name = "nivel_prioridad")
    private Integer nivelPrioridad = 0;

    private boolean activo = true;

    @Column(name = "es_sistema")
    private boolean esSistema = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Usuario> usuarios = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rol_permiso",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private Set<Permiso> permisos = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rol_menu",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private Set<Menu> menus = new HashSet<>();

    // Constructores
    public Rol() {}

    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
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

    public Integer getNivelPrioridad() {
        return nivelPrioridad;
    }
    
    public void setNivelPrioridad(Integer nivelPrioridad) {
        this.nivelPrioridad = nivelPrioridad;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public Boolean getEsSistema() {
        return esSistema;
    }
    
    public void setEsSistema(Boolean esSistema) {
        this.esSistema = esSistema;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Usuario getCreadoPor() {
        return creadoPor;
    }
    
    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
    }
    
    public Set<Usuario> getUsuarios() {
        return usuarios;
    }
    
    public void setUsuarios(Set<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
    
    public Set<Permiso> getPermisos() {
        return permisos;
    }
    
    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }
    
    public Set<Menu> getMenus() {
        return menus;
    }
    
    public void setMenus(Set<Menu> menus) {
        this.menus = menus;
    }
}

package com.pos.menu.model;

import com.pos.rol.model.Rol;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "menus")
public class Menu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 255)
    private String ruta;
    
    @Column(length = 50)
    private String icono;
    
    private Integer orden = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Menu> children = new HashSet<>();
    
    private Boolean visible = true;
    
    @Column(name = "requiere_permiso")
    private Boolean requierePermiso = true;
    
    @Column(length = 100)
    private String componente;
    
    private String descripcion;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> parametros = new HashMap<>();
    
    @Column(name = "es_externo")
    private Boolean esExterno = false;
    
    @Column(name = "abrir_en_nueva_ventana")
    private Boolean abrirEnNuevaVentana = false;
    
    @Column(name = "badge_text", length = 20)
    private String badgeText;
    
    @Column(name = "badge_color", length = 20)
    private String badgeColor;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToMany(mappedBy = "menus", fetch = FetchType.LAZY)
    private Set<Rol> roles = new HashSet<>();
    
    // Constructores
    public Menu() {
    }
    
    public Menu(String nombre, String ruta, String icono) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.icono = icono;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
    
    public String getRuta() {
        return ruta;
    }
    
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
    
    public String getIcono() {
        return icono;
    }
    
    public void setIcono(String icono) {
        this.icono = icono;
    }
    
    public Integer getOrden() {
        return orden;
    }
    
    public void setOrden(Integer orden) {
        this.orden = orden;
    }
    
    public Menu getParent() {
        return parent;
    }
    
    public void setParent(Menu parent) {
        this.parent = parent;
    }
    
    public Set<Menu> getChildren() {
        return children;
    }
    
    public void setChildren(Set<Menu> children) {
        this.children = children;
    }
    
    public Boolean getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    
    public Boolean getRequierePermiso() {
        return requierePermiso;
    }
    
    public void setRequierePermiso(Boolean requierePermiso) {
        this.requierePermiso = requierePermiso;
    }
    
    public String getComponente() {
        return componente;
    }
    
    public void setComponente(String componente) {
        this.componente = componente;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Map<String, Object> getParametros() {
        if (parametros == null) {
            parametros = new HashMap<>();
        }
        return parametros;
    }
    
    public void setParametros(Map<String, Object> parametros) {
        this.parametros = parametros;
    }
    
    public Boolean getEsExterno() {
        return esExterno;
    }
    
    public void setEsExterno(Boolean esExterno) {
        this.esExterno = esExterno;
    }
    
    public Boolean getAbrirEnNuevaVentana() {
        return abrirEnNuevaVentana;
    }
    
    public void setAbrirEnNuevaVentana(Boolean abrirEnNuevaVentana) {
        this.abrirEnNuevaVentana = abrirEnNuevaVentana;
    }
    
    public String getBadgeText() {
        return badgeText;
    }
    
    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }
    
    public String getBadgeColor() {
        return badgeColor;
    }
    
    public void setBadgeColor(String badgeColor) {
        this.badgeColor = badgeColor;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }
}
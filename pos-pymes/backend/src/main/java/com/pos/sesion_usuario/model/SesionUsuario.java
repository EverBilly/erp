package com.pos.sesion_usuario.model;

import com.pos.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sesiones_usuario")
public class SesionUsuario {
    
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "token_actual", nullable = false)
    private String tokenActual;
    
    @Column(name = "token_refresh", nullable = false)
    private String tokenRefresh;
    
    @Column(length = 100)
    private String dispositivo;
    
    @Column(length = 100)
    private String navegador;
    
    @Column(name = "sistema_operativo", length = 50)
    private String sistemaOperativo;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    private Double latitud;
    
    private Double longitud;
    
    @Column(length = 100)
    private String ciudad;
    
    @Column(length = 100)
    private String pais;
    
    private Boolean activa = true;
    
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "fecha_ultima_actividad")
    private LocalDateTime fechaUltimaActividad;
    
    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos = 0;
    
    // Constructores
    public SesionUsuario() {
    }
    
    public SesionUsuario(Usuario usuario, String tokenActual, String tokenRefresh) {
        this.usuario = usuario;
        this.tokenActual = tokenActual;
        this.tokenRefresh = tokenRefresh;
    }
    
    @PrePersist
    protected void onCreate() {
        id = UUID.randomUUID();
        fechaInicio = LocalDateTime.now();
        fechaUltimaActividad = LocalDateTime.now();
    }
    
    // Getters y Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public String getTokenActual() {
        return tokenActual;
    }
    
    public void setTokenActual(String tokenActual) {
        this.tokenActual = tokenActual;
    }
    
    public String getTokenRefresh() {
        return tokenRefresh;
    }
    
    public void setTokenRefresh(String tokenRefresh) {
        this.tokenRefresh = tokenRefresh;
    }
    
    public String getDispositivo() {
        return dispositivo;
    }
    
    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }
    
    public String getNavegador() {
        return navegador;
    }
    
    public void setNavegador(String navegador) {
        this.navegador = navegador;
    }
    
    public String getSistemaOperativo() {
        return sistemaOperativo;
    }
    
    public void setSistemaOperativo(String sistemaOperativo) {
        this.sistemaOperativo = sistemaOperativo;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Double getLatitud() {
        return latitud;
    }
    
    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }
    
    public Double getLongitud() {
        return longitud;
    }
    
    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
    
    public String getCiudad() {
        return ciudad;
    }
    
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    
    public String getPais() {
        return pais;
    }
    
    public void setPais(String pais) {
        this.pais = pais;
    }
    
    public Boolean getActiva() {
        return activa;
    }
    
    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
    
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }
    
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
    
    public LocalDateTime getFechaUltimaActividad() {
        return fechaUltimaActividad;
    }
    
    public void setFechaUltimaActividad(LocalDateTime fechaUltimaActividad) {
        this.fechaUltimaActividad = fechaUltimaActividad;
    }
    
    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }
    
    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }
}
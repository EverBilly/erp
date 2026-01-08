package com.pos.menu.dto;

public class MenuDto {
    private Long id;
    private String nombre;
    private String ruta;
    private String icono;
    private Integer orden;
    private Long parent_id;
    private Boolean visible;
    private Boolean esExterno;
    private Boolean abrirEnNuevaVentana;
    private String badgeText;
    private String badgeColor;

    // Constructor vacío
    public MenuDto() {}

    // Constructor completo
    public MenuDto(Long id, String nombre, String ruta, String icono, Integer orden,
                   Long parent_id, Boolean visible, Boolean esExterno,
                   Boolean abrirEnNuevaVentana, String badgeText, String badgeColor) {
        this.id = id;
        this.nombre = nombre;
        this.ruta = ruta;
        this.icono = icono;
        this.orden = orden;
        this.parent_id = parent_id;
        this.visible = visible;
        this.esExterno = esExterno;
        this.abrirEnNuevaVentana = abrirEnNuevaVentana;
        this.badgeText = badgeText;
        this.badgeColor = badgeColor;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public Long getParentId() { return parent_id; }
    public void setParentId(Long parent_id) { this.parent_id = parent_id; }

    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }

    public Boolean getEsExterno() { return esExterno; }
    public void setEsExterno(Boolean esExterno) { this.esExterno = esExterno; }

    public Boolean getAbrirEnNuevaVentana() { return abrirEnNuevaVentana; }
    public void setAbrirEnNuevaVentana(Boolean abrirEnNuevaVentana) { this.abrirEnNuevaVentana = abrirEnNuevaVentana; }

    public String getBadgeText() { return badgeText; }
    public void setBadgeText(String badgeText) { this.badgeText = badgeText; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }
}
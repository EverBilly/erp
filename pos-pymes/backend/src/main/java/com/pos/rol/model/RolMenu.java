package com.pos.rol.model;

import com.pos.menu.model.Menu;
import com.pos.rol.model.Rol;
import jakarta.persistence.*;

@Entity
@Table(name = "rol_menu")
@IdClass(RolMenuId.class)
public class RolMenu {

    @Id
    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @Id
    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private Boolean activo = true;
    private Boolean puedeVer = true;
    private Boolean puedeEditar = false;
    private Boolean puedeEliminar = false;

    // Getters y Setters
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Boolean getPuedeVer() { return puedeVer; }
    public void setPuedeVer(Boolean puedeVer) { this.puedeVer = puedeVer; }

    public Boolean getPuedeEditar() { return puedeEditar; }
    public void setPuedeEditar(Boolean puedeEditar) { this.puedeEditar = puedeEditar; }

    public Boolean getPuedeEliminar() { return puedeEliminar; }
    public void setPuedeEliminar(Boolean puedeEliminar) { this.puedeEliminar = puedeEliminar; }
}
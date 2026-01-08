package com.pos.rol.model;

import java.io.Serializable;
import java.util.Objects;

public class RolMenuId implements Serializable {
    private Long rol;
    private Long menu;

    // Constructor vacío obligatorio
    public RolMenuId() {}

    public RolMenuId(Long rol, Long menu) {
        this.rol = rol;
        this.menu = menu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolMenuId that = (RolMenuId) o;
        return Objects.equals(rol, that.rol) && Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rol, menu);
    }
}
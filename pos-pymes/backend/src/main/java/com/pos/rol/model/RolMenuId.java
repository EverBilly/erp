package com.pos.rol.model;

import java.io.Serializable;
import java.util.Objects;

public class RolMenuId implements Serializable {
    private Long role;
    private Long menu;

    public RolMenuId() {}

    public RolMenuId(Long role, Long menu) {
        this.role = role;
        this.menu = menu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolMenuId that = (RolMenuId) o;
        return Objects.equals(role, that.role) && Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, menu);
    }
}

package com.pos.role.model;

import java.io.Serializable;
import java.util.Objects;

public class RoleMenuId implements Serializable {
    private Long role;
    private Long menu;

    public RoleMenuId() {}

    public RoleMenuId(Long role, Long menu) {
        this.role = role;
        this.menu = menu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleMenuId that = (RoleMenuId) o;
        return Objects.equals(role, that.role) && Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, menu);
    }
}

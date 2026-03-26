package com.pos.role.model;

import com.pos.menu.model.Menu;
import jakarta.persistence.*;

@Entity
@Table(name = "role_menus")
@IdClass(RoleMenuId.class)
public class RoleMenu {

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Id
    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private Boolean active = true;

    @Column(name = "can_view")
    private Boolean canView = true;

    @Column(name = "can_edit")
    private Boolean canEdit = false;

    @Column(name = "can_delete")
    private Boolean canDelete = false;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getCanView() { return canView; }
    public void setCanView(Boolean canView) { this.canView = canView; }

    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }

    public Boolean getCanDelete() { return canDelete; }
    public void setCanDelete(Boolean canDelete) { this.canDelete = canDelete; }
}

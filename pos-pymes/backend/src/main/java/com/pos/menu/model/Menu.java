package com.pos.menu.model;

import com.pos.rol.model.Rol;
import com.pos.tenant.model.Tenant;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String path;

    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "parent_id")
    private Long parentId;

    private Boolean visible = true;

    @Column(name = "requires_permission")
    private Boolean requiresPermission = true;

    @Column(length = 100)
    private String component;

    private String description;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> params = new HashMap<>();

    @Column(name = "is_external")
    private Boolean isExternal = false;

    @Column(name = "open_in_new_tab")
    private Boolean openInNewTab = false;

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

    public Menu() {}

    public Menu(String name, String path, String icon) {
        this.name = name;
        this.path = path;
        this.icon = icon;
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }

    public Boolean getRequiresPermission() { return requiresPermission; }
    public void setRequiresPermission(Boolean requiresPermission) { this.requiresPermission = requiresPermission; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getParams() {
        if (params == null) { params = new HashMap<>(); }
        return params;
    }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public Boolean getIsExternal() { return isExternal; }
    public void setIsExternal(Boolean isExternal) { this.isExternal = isExternal; }

    public Boolean getOpenInNewTab() { return openInNewTab; }
    public void setOpenInNewTab(Boolean openInNewTab) { this.openInNewTab = openInNewTab; }

    public String getBadgeText() { return badgeText; }
    public void setBadgeText(String badgeText) { this.badgeText = badgeText; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}

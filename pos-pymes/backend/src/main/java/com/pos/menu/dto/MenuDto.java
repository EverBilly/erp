package com.pos.menu.dto;

public class MenuDto {
    private Long id;
    private String name;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Long parent_id;
    private Boolean visible;
    private Boolean isExternal;
    private Boolean openInNewTab;
    private String badgeText;
    private String badgeColor;

    // Constructor vacío
    public MenuDto() {}

    // Constructor completo
    public MenuDto(Long id, String name, String path, String icon, Integer sortOrder,
                   Long parent_id, Boolean visible, Boolean isExternal,
                   Boolean openInNewTab, String badgeText, String badgeColor) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.parent_id = parent_id;
        this.visible = visible;
        this.isExternal = isExternal;
        this.openInNewTab = openInNewTab;
        this.badgeText = badgeText;
        this.badgeColor = badgeColor;
    }

    // Getters y Setters
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

    public Long getParentId() { return parent_id; }
    public void setParentId(Long parent_id) { this.parent_id = parent_id; }

    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }

    public Boolean getIsExternal() { return isExternal; }
    public void setIsExternal(Boolean isExternal) { this.isExternal = isExternal; }

    public Boolean getOpenInNewTab() { return openInNewTab; }
    public void setOpenInNewTab(Boolean openInNewTab) { this.openInNewTab = openInNewTab; }

    public String getBadgeText() { return badgeText; }
    public void setBadgeText(String badgeText) { this.badgeText = badgeText; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }
}

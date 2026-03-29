package com.pos.usuario.dto;

public class RolDisplayDto {
    private String name;
    private String displayName;
    private String description;
    private String color;
    private Integer priorityLevel;

    public RolDisplayDto() {}

    public RolDisplayDto(String name, String displayName, String description, String color, Integer priorityLevel) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.priorityLevel = priorityLevel;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
}

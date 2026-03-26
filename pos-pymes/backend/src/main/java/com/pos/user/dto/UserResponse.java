package com.pos.user.dto;

import com.pos.shared.auth.dto.RoleDisplayDto;
import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<RoleDisplayDto> roles;

    public UserResponse() {}

    public UserResponse(Long id, String username, String email, String fullName,
                       String phone, boolean active, LocalDateTime createdAt,
                       LocalDateTime lastLogin, List<RoleDisplayDto> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.active = active;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean getActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public List<RoleDisplayDto> getRoles() { return roles; }
    public void setRoles(List<RoleDisplayDto> roles) { this.roles = roles; }
}

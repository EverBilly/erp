package com.pos.role.service;

import com.pos.shared.auth.dto.RoleDisplayDto;
import com.pos.role.model.Role;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RoleDisplayService {

    private static final Map<String, String> ROLE_COLOR_MAP = new HashMap<>();
    static {
        ROLE_COLOR_MAP.put("SUPER_ADMIN", "error");
        ROLE_COLOR_MAP.put("ADMIN", "warning");
        ROLE_COLOR_MAP.put("USER", "info");
    }

    public RoleDisplayDto convertToDisplayDto(Role role) {
        String displayName = role.getDescription() != null && !role.getDescription().trim().isEmpty()
            ? role.getDescription()
            : role.getName();

        String color = ROLE_COLOR_MAP.getOrDefault(role.getName(), "default");

        return new RoleDisplayDto(
            role.getId(),
            role.getName(),
            displayName,
            role.getDescription() != null ? role.getDescription() : "Sin descripción",
            color,
            role.getPriorityLevel()
        );
    }
}

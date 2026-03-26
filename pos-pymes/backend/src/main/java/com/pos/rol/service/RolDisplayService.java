package com.pos.rol.service;

import com.pos.usuario.dto.RolDisplayDto;
import com.pos.rol.model.Rol;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RolDisplayService {

    private static final Map<String, String> ROLE_COLOR_MAP = new HashMap<>();
    static {
        ROLE_COLOR_MAP.put("SUPER_ADMIN", "error");
        ROLE_COLOR_MAP.put("ADMIN", "warning");
        ROLE_COLOR_MAP.put("USER", "info");
        // Se pueden añadir más dinámicamente
    }

    public RolDisplayDto convertToDisplayDto(Rol rol) {
        String displayName = rol.getDescription() != null && !rol.getDescription().trim().isEmpty()
            ? rol.getDescription()
            : rol.getName();

        String color = ROLE_COLOR_MAP.getOrDefault(rol.getName(), "default");

        return new RolDisplayDto(
            rol.getName(),
            displayName,
            rol.getDescription() != null ? rol.getDescription() : "Sin descripción",
            color,
            rol.getPriorityLevel()
        );
    }
}

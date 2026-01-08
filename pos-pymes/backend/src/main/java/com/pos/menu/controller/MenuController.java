package com.pos.menu.controller;

import com.pos.menu.service.MenuService;
import com.pos.menu.dto.MenuDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/menu")
    public ResponseEntity<List<MenuDto>> getMenuActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Obtener roles del usuario actual (usa tu servicio de usuarios)
        List<String> roles = menuService.getRolesByUsername(username);
        Set<String> rolesSet = Set.copyOf(roles);

        // Cargar menú por roles
        List<MenuDto> menu = menuService.getMenuForUserRoles(rolesSet);
        return ResponseEntity.ok(menu);
    }
}
package com.pos.menu.controller;

import com.pos.menu.service.MenuService;
import com.pos.menu.dto.MenuDto;
import com.pos.shared.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuDto>> getMenuActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();

        Set<String> rolesSet = userPrincipal.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toSet());

        // Cargar menú por roles
        List<MenuDto> menu = menuService.getMenuForUserRoles(rolesSet);
        return ResponseEntity.ok(menu);
    }
}
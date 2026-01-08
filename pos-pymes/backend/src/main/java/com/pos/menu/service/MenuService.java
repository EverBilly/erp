package com.pos.menu.service;

import com.pos.menu.model.Menu;
import com.pos.menu.repository.MenuRepository;
import com.pos.menu.dto.MenuDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;


    public List<MenuDto> getMenuForUserRoles(Set<String> roles) {
        List<Menu> menus = menuRepository.findMenusByRoles(roles);
        return menus.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private MenuDto convertToDto(Menu menu) {
        return new MenuDto(
            menu.getId(),
            menu.getNombre(),
            menu.getRuta(),
            menu.getIcono(),
            menu.getOrden(),
            menu.getParentId(),
            menu.getVisible(),
            menu.getEsExterno(),
            menu.getAbrirEnNuevaVentana(),
            menu.getBadgeText(),
            menu.getBadgeColor()
        );
    }
}
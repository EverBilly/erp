# FT04 - Menus Dinamicos

## Que hace
Sistema de menus que se construyen dinamicamente segun los roles del usuario logueado.
Soporta jerarquia (menus padre/hijo), iconos, badges y links externos.
El frontend renderiza el menu en un Sidebar lateral.

## Endpoints

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/api/menus` | Autenticado | Menus del usuario segun sus roles |

## Response

### GET /api/menus
```json
[
  {
    "id": 1,
    "name": "Usuarios",
    "path": "/usuarios",
    "icon": "users",
    "sortOrder": 1,
    "parentId": null,
    "visible": true,
    "isExternal": false,
    "openInNewTab": false,
    "badgeText": null,
    "badgeColor": null
  },
  {
    "id": 5,
    "name": "Crear Usuario",
    "path": "/usuarios/nuevo",
    "icon": "person_add",
    "sortOrder": 1,
    "parentId": 1,
    "visible": true,
    "isExternal": false,
    "openInNewTab": false,
    "badgeText": null,
    "badgeColor": null
  }
]
```

## Flujo
```
1. Usuario se loguea -> frontend llama GET /api/menus
2. MenuController extrae roles del SecurityContext (authorities)
3. MenuService.getMenuForUserRoles(roles) consulta menus visibles para esos roles
4. MenuRepository.findMenusByRoles() hace JOIN con role_menus
5. Backend retorna lista plana de MenuDto
6. Frontend en AuthContext guarda menuTree
7. Sidebar.jsx renderiza menus recursivamente (parentId para jerarquia)
8. iconMapper.js convierte string "users" -> componente MUI <PeopleIcon/>
```

## Modelo de datos
- **menus** - id, tenant_id, name, path, icon, sort_order, parent_id, visible, requires_permission, componente, description, params (JSONB), is_external, open_in_new_tab, badge_text, badge_color, created_at, updated_at
- **role_menus** - rol_id, menu_id, active, can_view, can_edit, can_delete

## Archivos involucrados

### Backend
- `menu/controller/MenuController.java` - GET /api/menus
- `menu/service/MenuService.java` - Logica de filtrado y conversion a DTO
- `menu/repository/MenuRepository.java` - Query JOIN con role_menus
- `menu/model/Menu.java` - Entidad JPA
- `menu/dto/MenuDto.java` - DTO de respuesta

### Frontend
- `context/AuthContext.jsx` - Guarda menuTree despues del login
- `components/Sidebar.jsx` - Renderiza menu lateral recursivo
- `pages/MainMenu.jsx` - Muestra tarjetas de modulos disponibles
- `utils/iconMapper.js` - Mapea strings a componentes de icono MUI

## Iconos disponibles (iconMapper.js)
`home`, `dashboard`, `users`, `person`, `person_add`, `settings`, `inventory`,
`shopping_cart`, `store`, `receipt`, `assessment`, `analytics`, `bar_chart`,
`pie_chart`, `trending_up`, `category`, `local_offer`, `payment`, `account_balance`,
`business`, `warehouse`, `local_shipping`, `support`, `help`, `info`,
`notifications`, `security`, `admin_panel`, `build`, `description`, `calendar`

## Estado
- [x] Backend: menus filtrados por roles
- [x] Frontend: sidebar recursivo con iconos
- [x] Jerarquia padre/hijo via parent_id
- [x] MainMenu con tarjetas de modulos
- [ ] CRUD de menus (no se pueden crear/editar desde la app)
- [ ] Badges funcionales (campos existen, sin logica)
- [ ] Drag & drop para reordenar
- [ ] Permisos granulares de RolMenu (can_view/can_edit/can_delete no se validan)

## Problemas conocidos
- MenuController castea principal sin null check (riesgo de NullPointerException)
- menuUtils.js existe en frontend pero nunca se usa (codigo muerto)
- DashboardController.getMenu() retorna string hardcodeado, no usa MenuService
- Menu entity tiene EAGER fetch en tenant (innecesario)

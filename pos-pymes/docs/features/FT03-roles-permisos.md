# FT03 - Roles y Permisos

## Que hace
Sistema de roles para controlar acceso. Los roles se asignan a usuarios y determinan
que menus puede ver cada uno. Los permisos estan definidos en el modelo pero NO
se usan en la logica de autorizacion todavia.

## Endpoints

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/api/roles` | Autenticado | Listar todos los roles |

## Response

### GET /api/roles
```json
[
  {
    "id": 1,
    "name": "SUPER_ADMIN",
    "description": "Administrador del sistema",
    "priorityLevel": 1000,
    "active": true,
    "isSystem": true,
    "createdAt": "2025-01-01T00:00:00"
  }
]
```

## Modelo de datos
- **roles** - id, name (unico), description, priority_level, active, is_system, created_at, created_by
- **permissions** - id, code (unico), name, description, module, category, security_level, active (SIN USAR)
- **user_roles** - user_id, role_id
- **role_permissions** - role_id, permission_id (SIN USAR en logica)
- **role_menus** - role_id, menu_id, active, can_view, can_edit, can_delete

## Logica de colores de rol (para UI)
```
priority_level >= 900 -> "error" (rojo)     -> SUPER_ADMIN
priority_level >= 500 -> "warning" (amarillo) -> ADMIN
priority_level > 0    -> "info" (azul)        -> USER
otro                   -> "default" (gris)
```

## Archivos involucrados

### Backend
- `role/controller/RoleController.java` - Solo GET /api/roles
- `role/service/RoleDisplayService.java` - Conversion a RoleDisplayDto con colores
- `role/repository/RoleRepository.java` - Queries (findByName, findByActiveTrue, etc.)
- `role/model/Role.java` - Entidad JPA
- `role/model/RoleMenu.java` - Entidad puente rol-menu con permisos granulares
- `role/model/RoleMenuId.java` - Clave compuesta para RoleMenu
- `permission/model/Permission.java` - Entidad JPA (existe pero no se usa)
- `permission/repository/PermissionRepository.java` - Queries (existe pero no se usa)
- `shared/auth/dto/RoleDisplayDto.java` - DTO para mostrar rol en UI

### Frontend
- `pages/users/UserForm.jsx` - Autocomplete de roles al crear/editar usuario

## Estado
- [x] Modelo de roles en BD
- [x] Listado de roles (endpoint)
- [x] Asignacion de roles a usuarios
- [x] Roles determinan menus visibles
- [x] Display con colores por prioridad
- [ ] CRUD de roles (crear, editar, eliminar)
- [ ] Permisos funcionales (modelo existe, logica no)
- [ ] Asignacion de permisos a roles
- [ ] Autorizacion basada en permisos (solo hay por roles)
- [ ] Permisos granulares en RoleMenu (can_view, can_edit, can_delete no se validan)

## Problemas conocidos
- Solo hay endpoint GET, no se pueden crear/editar/eliminar roles desde la app
- Logica de colores duplicada en UserController y RoleDisplayService
- Permisos (tabla permissions) definidos pero nunca consultados en autorizacion
- RoleMenu tiene campos granulares (can_view, can_edit, can_delete) que no se usan
- GET /api/roles retorna la entidad Role directa, no un DTO

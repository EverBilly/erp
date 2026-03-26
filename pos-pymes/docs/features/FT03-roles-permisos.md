# FT03 - Roles y Permisos

## Que hace
Sistema de roles para controlar acceso. Los roles se asignan a usuarios y determinan
que menus puede ver cada uno. Los permisos estan definidos en el modelo pero NO
se usan en la logica de autorizacion todavia.

## Endpoints

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/api/roles` | Autenticado | Listar todos los roles |
| GET | `/api/dashboard/permissions` | Autenticado | Roles del usuario actual |
| GET | `/api/dashboard/roles` | Autenticado | Roles del usuario actual (duplicado) |

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
- **user_roles** - usuario_id, rol_id
- **role_permissions** - rol_id, permiso_id (SIN USAR en logica)
- **role_menus** - rol_id, menu_id, active, can_view, can_edit, can_delete

## Logica de colores de rol (para UI)
```
priority_level >= 900 -> "error" (rojo)     -> SUPER_ADMIN
priority_level >= 500 -> "warning" (amarillo) -> ADMIN
priority_level > 0    -> "info" (azul)        -> USER
otro                   -> "default" (gris)
```

## Archivos involucrados

### Backend
- `rol/controller/RolController.java` - Solo GET /api/roles
- `rol/service/RolDisplayService.java` - Conversion a RolDisplayDto con colores
- `rol/repository/RolRepository.java` - Queries (findByName, findByActiveTrue, etc.)
- `rol/model/Rol.java` - Entidad JPA
- `rol/model/RolMenu.java` - Entidad puente rol-menu con permisos granulares
- `rol/model/RolMenuId.java` - Clave compuesta para RolMenu
- `permiso/model/Permiso.java` - Entidad JPA (existe pero no se usa)
- `permiso/repository/PermisoRepository.java` - Queries (existe pero no se usa)
- `shared/auth/dto/RolDisplayDto.java` - DTO para mostrar rol en UI

### Frontend
- `pages/usuarios/UsuarioForm.jsx` - Autocomplete de roles al crear/editar usuario

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
- [ ] Permisos granulares en RolMenu (can_view, can_edit, can_delete no se validan)

## Problemas conocidos
- Solo hay endpoint GET, no se pueden crear/editar/eliminar roles desde la app
- Logica de colores duplicada en UsuarioController y RolDisplayService
- /dashboard/permissions y /dashboard/roles hacen exactamente lo mismo
- Permisos (tabla permissions) definidos pero nunca consultados en autorizacion
- RolMenu tiene campos granulares (can_view, can_edit, can_delete) que no se usan
- GET /api/roles retorna la entidad Rol directa, no un DTO

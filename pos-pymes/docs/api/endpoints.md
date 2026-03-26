# Referencia de Endpoints - POS-PyMEs API

**Base URL:** `http://localhost:8080/api`

## Autenticacion

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| POST | `/auth/login` | No | Login con username/password |
| POST | `/auth/logout` | No | Limpia SecurityContext |
| GET | `/auth/validate?token=X` | No | Valida token JWT |
| GET | `/auth/health` | No | Health check |
| GET | `/auth/test` | No | Test endpoint |

## Usuarios

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/usuarios` | SUPER_ADMIN | Listar todos |
| GET | `/usuarios/activos` | Autenticado | Listar activos |
| GET | `/usuarios/{id}` | Autenticado | Obtener por ID |
| GET | `/usuarios/buscar?nombre=X` | Autenticado | Buscar por nombre |
| GET | `/usuarios/contar` | Autenticado | Estadisticas |
| GET | `/usuarios/rol/{rolNombre}` | Autenticado | Por nombre de rol |
| GET | `/usuarios/check-username/{username}` | Autenticado | Verificar username |
| GET | `/usuarios/check-email/{email}` | Autenticado | Verificar email |
| POST | `/usuarios` | Autenticado | Crear usuario |
| PUT | `/usuarios/{id}` | Autenticado | Actualizar usuario |
| DELETE | `/usuarios/{id}` | Autenticado | Soft delete |
| PATCH | `/usuarios/{id}/activar` | Autenticado | Reactivar |
| PATCH | `/usuarios/{id}/desactivar` | Autenticado | Desactivar |

## Roles

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/roles` | Autenticado | Listar todos los roles |

## Menus

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/menus` | Autenticado | Menus del usuario segun roles |

## Dashboard

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/dashboard/menu` | Autenticado | Menu del usuario (hardcodeado) |
| GET | `/dashboard/permissions` | Autenticado | Roles del usuario |
| GET | `/dashboard/roles` | Autenticado | Roles del usuario (duplicado) |
| GET | `/dashboard/stats` | ADMIN/SUPER_ADMIN | Stats (hardcodeado) |
| GET | `/dashboard/profile` | Autenticado | Perfil del usuario actual |

## Otros

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/test` | No | Backend status check |

---

## Autenticacion

Todas las requests a endpoints autenticados deben incluir:
```
Authorization: Bearer {jwt_token}
```

## Codigos de respuesta usados

| Codigo | Significado | Cuando |
|--------|------------|--------|
| 200 | OK | Operacion exitosa |
| 201 | Created | Usuario creado |
| 400 | Bad Request | Validacion fallida, datos duplicados |
| 401 | Unauthorized | Token invalido o expirado |
| 404 | Not Found | Recurso no existe |
| 409 | Conflict | Usuario duplicado |
| 500 | Internal Server Error | Error no manejado |

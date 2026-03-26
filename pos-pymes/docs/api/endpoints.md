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
| GET | `/users` | SUPER_ADMIN | Listar todos |
| GET | `/users/active` | Autenticado | Listar activos |
| GET | `/users/{id}` | Autenticado | Obtener por ID |
| GET | `/users/search?name=X` | Autenticado | Buscar por nombre |
| GET | `/users/stats` | Autenticado | Estadisticas |
| GET | `/users/role/{roleName}` | Autenticado | Por nombre de rol |
| GET | `/users/check-username/{username}` | Autenticado | Verificar username |
| GET | `/users/check-email/{email}` | Autenticado | Verificar email |
| POST | `/users` | Autenticado | Crear usuario |
| PUT | `/users/{id}` | Autenticado | Actualizar usuario |
| DELETE | `/users/{id}` | Autenticado | Soft delete |
| PATCH | `/users/{id}/activate` | Autenticado | Reactivar |
| PATCH | `/users/{id}/deactivate` | Autenticado | Desactivar |

## Roles

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/roles` | Autenticado | Listar todos los roles |

## Menus

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/menus` | Autenticado | Menus del usuario segun roles |

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

# FT02 - Gestion de Usuarios

## Que hace
CRUD completo de usuarios con soft delete, busqueda, activacion/desactivacion
y asignacion de roles. Solo SUPER_ADMIN puede ver todos los usuarios.

## Endpoints

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/api/users` | SUPER_ADMIN | Listar todos los usuarios |
| GET | `/api/users/active` | Autenticado | Listar usuarios activos |
| GET | `/api/users/{id}` | Autenticado | Obtener usuario por ID |
| GET | `/api/users/search?name=X` | Autenticado | Buscar por nombre |
| GET | `/api/users/stats` | Autenticado | Estadisticas (total, activos, inactivos) |
| GET | `/api/users/role/{roleName}` | Autenticado | Usuarios por nombre de rol |
| GET | `/api/users/check-username/{username}` | Autenticado | Verificar si username existe |
| GET | `/api/users/check-email/{email}` | Autenticado | Verificar si email existe |
| POST | `/api/users` | Autenticado | Crear usuario |
| PUT | `/api/users/{id}` | Autenticado | Actualizar usuario |
| DELETE | `/api/users/{id}` | Autenticado | Soft delete (desactivar) |
| PATCH | `/api/users/{id}/activate` | Autenticado | Reactivar usuario |
| PATCH | `/api/users/{id}/deactivate` | Autenticado | Desactivar usuario |

## Request/Response

### POST /api/users (crear)

**Request:**
```json
{
  "username": "string (requerido, min 3, max 50)",
  "email": "string (requerido, formato email)",
  "password": "string (requerido, min 6)",
  "fullName": "string (requerido)",
  "phone": "string (opcional)",
  "active": true,
  "avatarUrl": "string (opcional)",
  "timezone": "string (opcional)",
  "locale": "string (opcional)",
  "roleIds": [1, 2]
}
```

### PUT /api/users/{id} (actualizar)

**Request (todos los campos opcionales):**
```json
{
  "fullName": "string (min 3, max 50)",
  "email": "string (formato email)",
  "password": "string (min 6, solo si se quiere cambiar)",
  "phone": "string",
  "active": true,
  "avatarUrl": "string",
  "timezone": "string",
  "locale": "string",
  "roleIds": [1, 3]
}
```

### Response (UserResponse)
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@pos.com",
  "fullName": "Administrador",
  "phone": "12345678",
  "active": true,
  "createdAt": "2025-01-15T10:30:00",
  "lastLogin": "2025-03-26T08:00:00",
  "roles": [
    {
      "id": 1,
      "name": "SUPER_ADMIN",
      "description": "Administrador del sistema",
      "color": "error",
      "priorityLevel": 1000
    }
  ]
}
```

### GET /api/users/stats
```json
{
  "total": 15,
  "activos": 12,
  "inactivos": 3
}
```

## Reglas de negocio
- No se puede eliminar el usuario con ID 1 (Super Admin)
- No se puede eliminar/desactivar al usuario actualmente logueado
- Username no se puede cambiar despues de creado
- Email debe ser unico (se valida contra otros usuarios al actualizar)
- Password se hashea con BCrypt automaticamente
- Tenant siempre se asigna como 1 (hardcodeado)
- DELETE hace soft delete (marca active=false), no borra de la BD
- Activar limpia intentos de login y desbloquea la cuenta

## Modelo de datos
- **users** - id, tenant_id, username, email, password_hash, full_name, phone, active, avatar_url, timezone, locale, metadata, login_attempts, locked_until, created_at, last_login
- **user_roles** - user_id, role_id (tabla puente)

## Archivos involucrados

### Backend
- `user/controller/UserController.java` - Endpoints REST
- `user/service/UserService.java` - Logica de negocio
- `user/repository/UserRepository.java` - Queries a BD
- `user/model/User.java` - Entidad JPA
- `user/dto/CreateUserRequest.java` - DTO para crear
- `user/dto/UpdateUserRequest.java` - DTO para actualizar
- `user/dto/UserResponse.java` - DTO de respuesta
- `user/exception/UserNotFoundException.java` - Excepcion 404
- `user/exception/DuplicateUserException.java` - Excepcion 409

### Frontend
- `pages/users/UsersView.jsx` - Router del modulo
- `pages/users/UsersList.jsx` - Listado con busqueda y paginacion
- `pages/users/UserForm.jsx` - Formulario crear/editar con Formik + Yup

## Estado
- [x] CRUD completo (crear, leer, actualizar, soft delete)
- [x] Busqueda por nombre
- [x] Estadisticas (contar)
- [x] Asignacion de roles
- [x] Frontend integrado (lista + formulario)
- [x] Validaciones en DTO (@NotBlank, @Email, @Size)
- [ ] Paginacion server-side (actualmente carga todos)
- [ ] @PreAuthorize en todos los endpoints (solo GET all tiene)
- [ ] Tests eliminados (se reescribiran)

## Problemas conocidos
- Logica de negocio mezclada en UserController (conversion DTOs, colores de rol)
- Busqueda y paginacion client-side en frontend (ineficiente)
- check-username y check-email permiten enumeracion de usuarios
- Tenant hardcodeado a 1

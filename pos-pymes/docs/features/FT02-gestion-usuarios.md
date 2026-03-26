# FT02 - Gestion de Usuarios

## Que hace
CRUD completo de usuarios con soft delete, busqueda, activacion/desactivacion
y asignacion de roles. Solo SUPER_ADMIN puede ver todos los usuarios.

## Endpoints

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| GET | `/api/usuarios` | SUPER_ADMIN | Listar todos los usuarios |
| GET | `/api/usuarios/activos` | Autenticado | Listar usuarios activos |
| GET | `/api/usuarios/{id}` | Autenticado | Obtener usuario por ID |
| GET | `/api/usuarios/buscar?nombre=X` | Autenticado | Buscar por nombre |
| GET | `/api/usuarios/contar` | Autenticado | Estadisticas (total, activos, inactivos) |
| GET | `/api/usuarios/rol/{rolNombre}` | Autenticado | Usuarios por nombre de rol |
| GET | `/api/usuarios/check-username/{username}` | Autenticado | Verificar si username existe |
| GET | `/api/usuarios/check-email/{email}` | Autenticado | Verificar si email existe |
| POST | `/api/usuarios` | Autenticado | Crear usuario |
| PUT | `/api/usuarios/{id}` | Autenticado | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Autenticado | Soft delete (desactivar) |
| PATCH | `/api/usuarios/{id}/activar` | Autenticado | Reactivar usuario |
| PATCH | `/api/usuarios/{id}/desactivar` | Autenticado | Desactivar usuario |

## Request/Response

### POST /api/usuarios (crear)

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

### PUT /api/usuarios/{id} (actualizar)

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

### Response (UsuarioResponse)
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

### GET /api/usuarios/contar
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
- **user_roles** - usuario_id, rol_id (tabla puente)

## Archivos involucrados

### Backend
- `usuario/controller/UsuarioController.java` - Endpoints REST
- `usuario/service/UsuarioService.java` - Logica de negocio
- `usuario/repository/UsuarioRepository.java` - Queries a BD
- `usuario/model/Usuario.java` - Entidad JPA
- `usuario/dto/CrearUsuarioRequest.java` - DTO para crear
- `usuario/dto/ActualizarUsuarioRequest.java` - DTO para actualizar
- `usuario/dto/UsuarioResponse.java` - DTO de respuesta
- `usuario/exception/UsuarioNotFoundException.java` - Excepcion 404
- `usuario/exception/UsuarioDuplicadoException.java` - Excepcion 409

### Frontend
- `pages/usuarios/UsuariosView.jsx` - Router del modulo
- `pages/usuarios/UsuariosList.jsx` - Listado con busqueda y paginacion
- `pages/usuarios/UsuarioForm.jsx` - Formulario crear/editar con Formik + Yup

## Estado
- [x] CRUD completo (crear, leer, actualizar, soft delete)
- [x] Busqueda por nombre
- [x] Estadisticas (contar)
- [x] Asignacion de roles
- [x] Frontend integrado (lista + formulario)
- [x] Validaciones en DTO (@NotBlank, @Email, @Size)
- [ ] Paginacion server-side (actualmente carga todos)
- [ ] @PreAuthorize en todos los endpoints (solo GET all tiene)
- [ ] Tests actualizados (existen pero no compilan)

## Problemas conocidos
- Logica de negocio mezclada en UsuarioController (conversion DTOs, colores de rol)
- Busqueda y paginacion client-side en frontend (ineficiente)
- check-username y check-email permiten enumeracion de usuarios
- Tenant hardcodeado a 1
- Tests prueban metodos que no existen en el service actual

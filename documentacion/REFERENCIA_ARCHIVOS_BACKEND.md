# Referencia de Archivos del Backend

Guía rápida de cada archivo Java en el proyecto con su propósito.

---

## Estructura Visual

```
src/main/java/com/pos/
│
├── PosApplication.java              ← PUNTO DE ENTRADA
│
├── config/
│   └── SecurityConfig.java          ← Configura qué rutas son públicas/privadas
│
├── controllers/                      ← RECIBEN PETICIONES HTTP
│   ├── AuthController.java          ← Login, validación de token
│   ├── UserController.java          ← CRUD usuarios (vacío)
│   └── DebuggerController.java      ← Endpoints de prueba (eliminar en prod)
│
├── dto/                              ← OBJETOS DE TRANSFERENCIA
│   ├── LoginRequest.java            ← { username, password }
│   └── LoginResponse.java           ← { token, user, roles, permisos }
│
├── models/                           ← ENTIDADES (TABLAS BD)
│   ├── Usuario.java                 ← Tabla usuarios
│   ├── Rol.java                     ← Tabla roles
│   └── Permiso.java                 ← Tabla permisos
│
├── repositories/                     ← ACCESO A BASE DE DATOS
│   ├── UsuarioRepository.java       ← Consultas de usuarios
│   ├── RolRepository.java           ← Consultas de roles
│   └── PermisoRepository.java       ← Consultas de permisos
│
├── security/                         ← AUTENTICACIÓN JWT
│   ├── JwtTokenProvider.java        ← Genera y valida tokens
│   ├── JwtAuthenticationFilter.java ← Intercepta cada petición
│   ├── CustomUserDetailsService.java← Carga usuario de BD
│   └── UserPrincipal.java           ← Datos del usuario autenticado
│
└── services/                         ← LÓGICA DE NEGOCIO
    ├── AuthService.java             ← Proceso de login
    └── UsuarioService.java          ← Operaciones CRUD usuarios
```

---

## Archivos Detallados

### PosApplication.java
```
Propósito: Punto de entrada de la aplicación
Equivalente: index.js en Node.js
Cuándo modificar: Casi nunca
```

---

### config/SecurityConfig.java
```
Propósito: Define qué rutas son públicas y cuáles requieren autenticación

Rutas públicas actuales:
- /api/auth/**     → Login y endpoints de auth
- /api/public/**   → Endpoints públicos
- /                → Home
- /error           → Página de error

Cuándo modificar:
- Cuando agregues nuevos endpoints públicos
- Cuando quieras proteger rutas específicas por rol
```

---

### controllers/AuthController.java
```
Propósito: Maneja autenticación (login)

Endpoints:
- POST /api/auth/login     → Autenticar usuario, retorna JWT
- GET  /api/auth/validate  → Validar token actual

NOTA: Tiene muchos endpoints de debugging que DEBEN ELIMINARSE:
- /api/auth/diagnostic
- /api/auth/test-db
- /api/auth/generate-hash
- /api/auth/check-all-users
- /api/auth/reset-admin-password
- etc.

Cuándo modificar:
- Agregar registro de usuarios
- Agregar recuperación de contraseña
- Agregar refresh token
```

---

### controllers/UserController.java
```
Propósito: CRUD de usuarios
Estado: VACÍO - Por implementar

Endpoints sugeridos:
- GET    /api/users          → Listar usuarios
- GET    /api/users/{id}     → Obtener usuario
- POST   /api/users          → Crear usuario
- PUT    /api/users/{id}     → Actualizar usuario
- DELETE /api/users/{id}     → Eliminar usuario
```

---

### controllers/DebuggerController.java
```
Propósito: Endpoints de prueba/desarrollo

IMPORTANTE: ELIMINAR EN PRODUCCIÓN

Endpoints:
- GET /api/debug/test-auth?username=X&password=X
- GET /api/debug/generate-bcrypt?password=X
```

---

### dto/LoginRequest.java
```
Propósito: Estructura del body para POST /api/auth/login

Campos:
- username (String)
- password (String)

Ejemplo JSON:
{
    "username": "admin",
    "password": "admin123"
}
```

---

### dto/LoginResponse.java
```
Propósito: Estructura de respuesta del login exitoso

Campos:
- token (String)      → JWT para usar en Authorization header
- id (Long)           → ID del usuario
- username (String)
- email (String)
- nombre (String)
- apellido (String)
- roles (List<String>)    → ["ADMIN", "CAJERO"]
- permisos (List<String>) → ["VENTA_CREAR", "PRODUCTO_VER"]

Ejemplo respuesta:
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "id": 1,
    "username": "admin",
    "email": "admin@pos.com",
    "nombre": "Administrador",
    "apellido": "Sistema",
    "roles": ["ADMIN"],
    "permisos": ["VENTA_CREAR", "PRODUCTO_EDITAR", ...]
}
```

---

### models/Usuario.java
```
Propósito: Entidad que mapea la tabla "usuarios"

Campos:
- id (Long, PK, auto-increment)
- username (String, único)
- password (String, hasheado con BCrypt)
- email (String, único)
- nombre (String)
- apellido (String)
- activo (Boolean, default true)
- fechaCreacion (LocalDateTime)
- fechaUltimoLogin (LocalDateTime)
- roles (Set<Rol>, relación ManyToMany)

Relación: Usuario ←→ Rol (muchos a muchos via usuarios_roles)
```

---

### models/Rol.java
```
Propósito: Entidad que mapea la tabla "roles"

Campos:
- id (Long, PK)
- nombre (String, único) → "ADMIN", "CAJERO", etc.
- descripcion (String)
- usuarios (Set<Usuario>)
- permisos (Set<Permiso>, relación ManyToMany)

Roles predefinidos:
- ADMIN
- CAJERO
- INVENTARIO
- REPORTES
```

---

### models/Permiso.java
```
Propósito: Entidad que mapea la tabla "permisos"

Campos:
- id (Long, PK)
- nombre (String, único) → "VENTA_CREAR", etc.
- descripcion (String)
- roles (Set<Rol>)

Permisos predefinidos:
- VENTA_CREAR, VENTA_VER, VENTA_CANCELAR
- PRODUCTO_CREAR, PRODUCTO_EDITAR, PRODUCTO_ELIMINAR, PRODUCTO_VER
- CLIENTE_GESTIONAR
- REPORTE_VER
- CONFIGURACION_EDITAR
```

---

### repositories/UsuarioRepository.java
```
Propósito: Acceso a la tabla usuarios

Métodos disponibles:
- findByUsername(String) → Optional<Usuario>
- findByEmail(String) → Optional<Usuario>
- findByUsernameOrEmail(String, String) → Optional<Usuario>
- existsByUsername(String) → Boolean
- existsByEmail(String) → Boolean

Heredados de JpaRepository:
- save(usuario)
- findById(id)
- findAll()
- deleteById(id)
- count()
```

---

### repositories/RolRepository.java
```
Propósito: Acceso a la tabla roles

Métodos:
- findByNombre(String) → Optional<Rol>
```

---

### repositories/PermisoRepository.java
```
Propósito: Acceso a la tabla permisos

Métodos:
- findByNombre(String) → Optional<Permiso>
```

---

### security/JwtTokenProvider.java
```
Propósito: Generar y validar tokens JWT

Métodos principales:
- generateToken(UserPrincipal) → String
  Crea un token con: userId, username, nombre, email
  Expira en 24 horas

- getUserIdFromToken(String token) → Long
  Extrae el ID del usuario del token

- validateToken(String token) → boolean
  Verifica que el token sea válido y no esté expirado

Configuración (application.properties):
- app.jwt.secret → Clave secreta para firmar
- app.jwt.expiration-in-ms → Tiempo de expiración
```

---

### security/JwtAuthenticationFilter.java
```
Propósito: Filtro que intercepta CADA petición HTTP

Flujo:
1. Extrae token del header "Authorization: Bearer xxx"
2. Valida el token con JwtTokenProvider
3. Carga el usuario con CustomUserDetailsService
4. Establece el usuario en el SecurityContext
5. Permite que la petición continúe

Si no hay token o es inválido:
- La petición continúa sin usuario autenticado
- SecurityConfig decidirá si permitir o denegar
```

---

### security/CustomUserDetailsService.java
```
Propósito: Carga usuario de la BD para Spring Security

Métodos:
- loadUserByUsername(String usernameOrEmail) → UserDetails
  Busca por username O email
  Retorna UserPrincipal

- loadUserById(Long id) → UserDetails
  Busca por ID (usado por JwtAuthenticationFilter)

Implementa: UserDetailsService (interfaz de Spring Security)
```

---

### security/UserPrincipal.java
```
Propósito: Representa al usuario autenticado en el sistema

Contiene:
- id, username, email, nombre, apellido
- password (hasheado)
- authorities (permisos + roles)

Método importante:
- getAuthorities() → Retorna permisos como PERMISO_X y roles como ROLE_X

Implementa: UserDetails (interfaz de Spring Security)
```

---

### services/AuthService.java
```
Propósito: Lógica de autenticación

Método principal:
- authenticateUser(LoginRequest) → LoginResponse

Flujo:
1. Crea UsernamePasswordAuthenticationToken
2. Autentica con AuthenticationManager
3. Genera token JWT
4. Obtiene roles y permisos del usuario
5. Actualiza fecha de último login
6. Retorna LoginResponse con todos los datos
```

---

### services/UsuarioService.java
```
Propósito: Operaciones CRUD de usuarios

Métodos:
- findAll() → List<Usuario>
- findById(Long) → Optional<Usuario>
- findByUsername(String) → Optional<Usuario>
- save(Usuario) → Usuario (hashea password si es nuevo)
- update(Long, Usuario) → Optional<Usuario>
- deleteById(Long)
- existsByUsername(String) → Boolean
- existsByEmail(String) → Boolean
- searchByNombre(String) → List<Usuario>
- countActivos() → Long
- findAllActivos() → List<Usuario>
```

---

## Resumen: ¿Dónde modificar para...?

| Tarea | Archivo(s) a modificar |
|-------|------------------------|
| Agregar nuevo endpoint | controllers/ + services/ |
| Nueva tabla en BD | models/ + repositories/ |
| Cambiar rutas públicas | config/SecurityConfig.java |
| Modificar token JWT | security/JwtTokenProvider.java |
| Agregar campo a usuario | models/Usuario.java |
| Cambiar lógica de login | services/AuthService.java |
| Nuevo permiso | 1. INSERT en BD 2. Usar en código |

---

## Próximos Archivos a Crear

Para completar el sistema, necesitarás crear:

### Entidades (models/)
- Producto.java
- Cliente.java
- Venta.java
- VentaDetalle.java

### Repositorios (repositories/)
- ProductoRepository.java
- ClienteRepository.java
- VentaRepository.java
- VentaDetalleRepository.java

### Servicios (services/)
- ProductoService.java
- ClienteService.java
- VentaService.java

### Controladores (controllers/)
- ProductoController.java
- ClienteController.java
- VentaController.java
- ReporteController.java

### DTOs (dto/)
- ProductoDTO.java
- ClienteDTO.java
- VentaDTO.java
- VentaDetalleDTO.java

---

*Referencia del proyecto POS-PYMES - Diciembre 2024*

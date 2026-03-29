# FT01 - Autenticacion

## Que hace
Login con JWT stateless. El usuario envia credenciales, el backend valida y devuelve
un token que el frontend incluye en cada request posterior.

## Endpoints

| Metodo | Path | Auth | Descripcion |
|--------|------|------|-------------|
| POST | `/api/auth/login` | No | Login con username/password |
| POST | `/api/auth/logout` | No | Limpia SecurityContext (cliente borra token) |
| GET | `/api/auth/validate?token=X` | No | Valida un token JWT |
| GET | `/api/auth/health` | No | Health check del servicio auth |
| GET | `/api/auth/test` | No | Test endpoint (en PosApplication) |
| GET | `/test` | No | Verifica que el backend esta corriendo |

## Request/Response

### POST /api/auth/login

**Request:**
```json
{
  "username": "string (requerido)",
  "password": "string (requerido)"
}
```

**Response exitoso (200):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@pos.com",
  "fullName": "Administrador",
  "roles": [{"authority": "SUPER_ADMIN"}]
}
```

**Response error (401):**
```json
{
  "error": "Credenciales invalidas",
  "message": "Usuario no encontrado"
}
```

## Flujo paso a paso

```
1. Cliente envia POST /api/auth/login con {username, password}
2. AuthController recibe y delega a AuthService.authenticateUser()
3. AuthService:
   a. Busca usuario por username en BD (UsuarioRepository.findByUsername)
   b. Verifica que usuario.active == true
   c. Compara password con BCrypt (passwordEncoder.matches)
   d. Actualiza lastLogin del usuario
   e. Crea UserPrincipal desde el usuario
   f. Genera JWT con JwtTokenProvider.generateTokenFromUsername()
   g. Retorna LoginResponse con token + datos del usuario
4. Cliente guarda token en localStorage
5. En cada request posterior:
   a. Axios interceptor agrega header: Authorization: Bearer {token}
   b. JwtAuthenticationFilter intercepta el request
   c. Extrae token del header (quita "Bearer ")
   d. Valida firma y expiracion con JwtTokenProvider.validateToken()
   e. Si es valido: carga usuario de BD y setea SecurityContext
   f. Si no es valido: request continua sin autenticacion
```

## Modelo de datos
- **users** - tabla principal (campos: username, password_hash, active, last_login)
- **roles** - roles del sistema (SUPER_ADMIN, ADMIN, USER, etc.)
- **user_roles** - tabla puente usuario <-> rol

## Archivos involucrados

### Backend
- `shared/auth/controller/AuthController.java` - Endpoints de login/logout
- `shared/auth/service/AuthService.java` - Logica de autenticacion
- `shared/auth/dto/LoginRequest.java` - DTO de entrada
- `shared/auth/dto/LoginResponse.java` - DTO de salida
- `shared/security/JwtTokenProvider.java` - Genera y valida tokens JWT
- `shared/security/JwtAuthenticationFilter.java` - Filtro que intercepta requests
- `shared/security/CustomUserDetailsService.java` - Carga usuario de BD
- `shared/security/UserPrincipal.java` - Wrapper de usuario para Spring Security
- `shared/config/SecurityConfig.java` - Config de seguridad (rutas publicas/protegidas)

### Frontend
- `services/api.js` - Interceptor que agrega token a cada request
- `services/authService.js` - Funciones login/logout/getCurrentUser
- `context/AuthContext.jsx` - Estado global de autenticacion
- `pages/Login.jsx` - Formulario de login

## Estado
- [x] Login funcional
- [x] JWT generacion y validacion
- [x] Filtro de autenticacion
- [x] Frontend integrado
- [ ] Refresh token (no implementado)
- [ ] Logout server-side (solo limpia contexto, no invalida token)
- [ ] Rate limiting en login
- [ ] Bloqueo de cuenta por intentos fallidos (campos existen, logica no activa)

## Problemas conocidos
- Catch vacios en JwtTokenProvider: atrapan excepciones de seguridad y solo loguean
- JwtAuthenticationFilter: catch generico deja pasar requests sin autenticar
- `/api/auth/validate` siempre retorna valid:true si el token pasa el filtro
- System.out.println en AuthController en vez de logger
- AuthService lanza RuntimeException en vez de excepciones custom

# Documentación del Proyecto ERP - POS-PYMES

---

## 1. Estructura General del Proyecto

```
erp/
└── pos-pymes/
    ├── backend/                    # Aplicación Spring Boot Java
    │   ├── src/main/java/com/pos/
    │   │   ├── controllers/        # Controladores REST
    │   │   ├── services/           # Lógica de negocio
    │   │   ├── models/             # Entidades JPA
    │   │   ├── repositories/       # Acceso a datos
    │   │   ├── security/           # Configuración de seguridad y JWT
    │   │   ├── config/             # Configuración de la aplicación
    │   │   └── dto/                # Data Transfer Objects
    │   ├── src/main/resources/
    │   │   └── application.properties
    │   ├── pom.xml                 # Dependencias Maven
    │   ├── Dockerfile              # Imagen Docker para backend
    │   └── target/                 # Archivos compilados
    ├── frontend/                   # Aplicación React
    │   ├── src/
    │   │   ├── pages/              # Páginas (Login, Dashboard)
    │   │   ├── components/         # Componentes (Layout, PrivateRoute)
    │   │   ├── context/            # Context API (AuthContext)
    │   │   ├── services/           # Servicios HTTP (api, authService)
    │   │   ├── App.js              # Componente raíz
    │   │   └── index.js            # Punto de entrada
    │   ├── public/                 # Archivos estáticos
    │   ├── package.json            # Dependencias npm
    │   ├── nginx.conf              # Configuración Nginx
    │   └── Dockerfile              # Imagen Docker para frontend
    ├── database/
    │   └── init.sql                # Script de inicialización de BD
    ├── docker-compose.yml          # Orquestación de contenedores
    ├── .env                        # Variables de entorno
    └── README.md
```

---

## 2. Tecnologías Utilizadas

### Backend (Java Spring Boot)

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Spring Boot | 3.1.5 | Framework principal |
| Java | 17 | Lenguaje de programación |
| PostgreSQL | 15 | Base de datos |
| Spring Data JPA | - | ORM (Hibernate) |
| Spring Security | - | Seguridad |
| JWT (jjwt) | 0.11.5 | Tokens de autenticación |
| Maven | 3.9.6 | Gestión de dependencias |
| Lombok | - | Reducción de boilerplate |

### Frontend (React)

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| React | 18.2.0 | Framework principal |
| React Router DOM | 6.20.1 | Enrutamiento |
| Material-UI (MUI) | 5.14.20 | Componentes UI |
| Axios | 1.6.2 | Cliente HTTP |
| Formik | 2.4.5 | Manejo de formularios |
| Yup | 1.3.3 | Validación de esquemas |
| Emotion | 11.11.1 | CSS-in-JS |

### Infraestructura

| Tecnología | Descripción |
|------------|-------------|
| Docker | Contenedores |
| Docker Compose | Orquestación multi-contenedor |
| Nginx | Servidor web/proxy inverso |

---

## 3. Arquitectura del Backend

### 3.1 Estructura de Paquetes

```
com.pos/
├── PosApplication.java          # Clase principal
├── config/
│   └── SecurityConfig.java      # Configuración de seguridad
├── controllers/
│   ├── AuthController.java      # Endpoints de autenticación
│   ├── UserController.java      # Endpoints de usuarios
│   └── DebuggerController.java  # Endpoints de debugging
├── dto/
│   ├── LoginRequest.java        # DTO para login
│   └── LoginResponse.java       # DTO de respuesta login
├── models/
│   ├── Usuario.java             # Entidad usuario
│   ├── Rol.java                 # Entidad rol
│   └── Permiso.java             # Entidad permiso
├── repositories/
│   ├── UsuarioRepository.java   # JPA repository para usuarios
│   ├── RolRepository.java       # JPA repository para roles
│   └── PermisoRepository.java   # JPA repository para permisos
├── security/
│   ├── JwtTokenProvider.java    # Proveedor de tokens JWT
│   ├── CustomUserDetailsService.java  # Servicio de detalles de usuario
│   ├── UserPrincipal.java       # Implementación de UserDetails
│   └── JwtAuthenticationFilter.java   # Filtro de autenticación JWT
└── services/
    ├── AuthService.java         # Lógica de autenticación
    └── UsuarioService.java      # Lógica de usuarios
```

### 3.2 Modelos de Datos

#### Usuario
```java
- id (Long, PK)
- username (String, UNIQUE)
- password (String, BCrypt hash)
- email (String, UNIQUE)
- nombre (String)
- apellido (String)
- activo (Boolean, default: true)
- fechaCreacion (LocalDateTime)
- fechaUltimoLogin (LocalDateTime)
- roles (Set<Rol>, relación M:M)
```

#### Rol
```java
- id (Long, PK)
- nombre (String, UNIQUE)
- descripcion (String)
- usuarios (Set<Usuario>, relación M:M)
- permisos (Set<Permiso>, relación M:M, EAGER)
```

#### Permiso
```java
- id (Long, PK)
- nombre (String, UNIQUE)
- descripcion (String)
- roles (Set<Rol>, relación M:M)
```

### 3.3 Modelos de Negocio (definidos en SQL)

#### Producto
```sql
- id (SERIAL, PK)
- codigo (VARCHAR, UNIQUE)
- nombre (VARCHAR)
- descripcion (TEXT)
- precio (DECIMAL)
- stock (INTEGER)
- categoria (VARCHAR)
```

#### Cliente
```sql
- id (SERIAL, PK)
- nombre (VARCHAR)
- email (VARCHAR, UNIQUE)
- telefono (VARCHAR)
- direccion (TEXT)
- rfc (VARCHAR)
```

#### Venta
```sql
- id (SERIAL, PK)
- folio (VARCHAR, UNIQUE)
- cliente_id (FK)
- total (DECIMAL)
- estado (VARCHAR)
- fecha (TIMESTAMP)
```

#### VentaDetalle
```sql
- id (SERIAL, PK)
- venta_id (FK)
- producto_id (FK)
- cantidad (INTEGER)
- precio_unitario (DECIMAL)
- subtotal (DECIMAL)
```

---

## 4. Endpoints de la API

### 4.1 Autenticación (`/api/auth/`)

| Método | Endpoint | Público | Descripción |
|--------|----------|---------|-------------|
| POST | `/api/auth/login` | Sí | Login principal - retorna JWT |
| GET | `/api/auth/validate` | No | Validación de token |

### 4.2 Endpoints de Debugging (REMOVER EN PRODUCCIÓN)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/auth/public-test` | Test de endpoint público |
| GET | `/api/auth/diagnostic` | Diagnóstico del sistema |
| GET | `/api/auth/test-db` | Test de conexión a BD |
| POST | `/api/auth/test-auth-manual` | Test manual de autenticación |
| GET | `/api/auth/generate-hash` | Generador de hashes BCrypt |
| GET | `/api/auth/check-all-users` | Lista todos los usuarios |
| POST | `/api/auth/reset-admin-password` | Reset de contraseña admin |
| POST | `/api/auth/hash-all-passwords` | Hashea todas las contraseñas |
| POST | `/api/auth/create-correct-user` | Crea usuario con hash correcto |
| POST | `/api/auth/fix-admin-complete` | Repara usuario admin |
| GET | `/api/auth/check-admin-roles` | Verifica roles de admin |

### 4.3 Debug (`/api/debug/`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/debug/test-auth` | Test de autenticación con parámetros |
| GET | `/api/debug/generate-bcrypt` | Generador BCrypt |

### 4.4 Home

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/` | Mensaje de bienvenida |
| GET | `/health` | Health check |

---

## 5. Sistema de Autenticación

### 5.1 Flujo de Autenticación

```
Frontend (Login)
    ↓
POST /api/auth/login {username, password}
    ↓
AuthController.authenticateUser()
    ↓
AuthService.authenticateUser()
    - UsernamePasswordAuthenticationToken
    - authenticationManager.authenticate()
    ↓
CustomUserDetailsService.loadUserByUsername()
    - Busca usuario en BD
    - Retorna UserPrincipal con autoridades
    ↓
BCryptPasswordEncoder.matches() - Validación de contraseña
    ↓
JwtTokenProvider.generateToken() - Crea JWT con HS512
    ↓
LoginResponse (token, user info, roles, permisos)
    ↓
Frontend: localStorage.setItem('token', token)
    ↓
Axios interceptor agrega: Authorization: Bearer {token}
```

### 5.2 Características de Seguridad

| Característica | Descripción |
|----------------|-------------|
| Encriptación | BCrypt (10 rounds) |
| JWT Algorithm | HS512 (HMAC SHA-512) |
| Expiración | 24 horas |
| Sesiones | STATELESS |
| CSRF | Deshabilitado (API REST) |
| CORS | localhost:3000, localhost:8080 |

### 5.3 Rutas Públicas

- `/` - Home
- `/api/auth/**` - Autenticación
- `/api/public/**` - Rutas públicas
- `/error`, `/favicon.ico`

### 5.4 JWT Claims

```json
{
  "sub": "user_id",
  "iat": "issued_at",
  "exp": "expiration",
  "username": "admin",
  "nombre": "Usuario Admin",
  "email": "admin@pos.com"
}
```

---

## 6. Arquitectura del Frontend

### 6.1 Estructura de Carpetas

```
src/
├── App.js                  # Componente raíz con Router
├── index.js                # Punto de entrada (React 18)
├── pages/
│   ├── Login.jsx           # Página de login
│   └── Dashboard.jsx       # Página principal (protegida)
├── components/
│   ├── Layout.jsx          # Layout con navbar y sidebar
│   └── PrivateRoute.jsx    # Componente para rutas protegidas
├── context/
│   └── AuthContext.jsx     # Context API para autenticación
└── services/
    ├── api.js              # Instancia Axios configurada
    └── authService.js      # Servicios de autenticación
```

### 6.2 Componentes Principales

#### AuthContext
- Almacena estado global: `user`, `loading`
- Métodos: `login()`, `logout()`
- Hook `useAuth()` para acceder al contexto

#### PrivateRoute
- Wrapper para rutas protegidas
- Verifica `isAuthenticated`
- Redirige a `/login` si no autenticado

#### Layout
- AppBar superior con nombre usuario
- Sidebar con navegación
- Responsive para móviles

#### API (Axios)
- BaseURL: `http://localhost:8080/api`
- Interceptor de request: agrega token Bearer
- Interceptor de response: maneja 401

### 6.3 Servicios

#### authService.js
```javascript
login(username, password)  // POST /api/auth/login
logout()                   // Limpia localStorage
getCurrentUser()           // Obtiene usuario de localStorage
isAuthenticated()          // Verifica si hay token
getAuthHeader()            // Retorna header de autenticación
```

### 6.4 Páginas

| Página | Ruta | Descripción |
|--------|------|-------------|
| Login | `/login` | Formulario de autenticación |
| Dashboard | `/` | Panel principal (protegido) |

### 6.5 Módulos en Dashboard (UI)

- Punto de Venta
- Productos
- Clientes
- Reportes
- Configuración

---

## 7. Configuración Docker

### 7.1 Servicios (docker-compose.yml)

| Servicio | Imagen | Puerto | Descripción |
|----------|--------|--------|-------------|
| db | postgres:15-alpine | 5432 | Base de datos |
| backend | Build local | 8080 | API Spring Boot |
| frontend | Build local | 3000 | React + Nginx |
| adminer | adminer | 8081 | Gestor web de BD |

### 7.2 Variables de Entorno (.env)

```env
POSTGRES_DB=pos_db
POSTGRES_USER=pos_user
POSTGRES_PASSWORD=pos_password
```

### 7.3 Red

- Nombre: `pos-network`
- Driver: bridge

### 7.4 Volúmenes

- `db-data` - Persistencia de PostgreSQL
- `maven-repo` - Cache de Maven

---

## 8. Configuración de la Aplicación

### 8.1 application.properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/pos_db
spring.datasource.username=pos_user
spring.datasource.password=pos_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
app.jwt.secret=MySuperSecureKeyForPOSApplicationThatIsAtLeast64CharactersLongForHS512Algorithm2024!
app.jwt.expiration-in-ms=86400000
```

### 8.2 Nginx (frontend)

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/;
    }
}
```

---

## 9. Dependencias

### 9.1 Backend (pom.xml)

```xml
<!-- Spring Boot -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation

<!-- Database -->
postgresql

<!-- JWT -->
jjwt-api (0.11.5)
jjwt-impl (0.11.5)
jjwt-jackson (0.11.5)

<!-- Utilities -->
lombok

<!-- Testing -->
spring-boot-starter-test
spring-security-test
```

### 9.2 Frontend (package.json)

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.1",
    "@mui/material": "^5.14.20",
    "@mui/icons-material": "^5.14.19",
    "@emotion/react": "^11.11.1",
    "@emotion/styled": "^11.11.0",
    "axios": "^1.6.2",
    "formik": "^2.4.5",
    "yup": "^1.3.3"
  }
}
```

---

## 10. Roles y Permisos

### 10.1 Roles Predefinidos

| Rol | Descripción |
|-----|-------------|
| ADMIN | Administrador del sistema |
| CAJERO | Operador de punto de venta |
| INVENTARIO | Gestión de productos |
| REPORTES | Acceso a reportes |

### 10.2 Permisos

| Permiso | Descripción |
|---------|-------------|
| VENTA_CREAR | Crear ventas |
| VENTA_VER | Ver ventas |
| VENTA_CANCELAR | Cancelar ventas |
| PRODUCTO_CREAR | Crear productos |
| PRODUCTO_EDITAR | Editar productos |
| PRODUCTO_ELIMINAR | Eliminar productos |
| PRODUCTO_VER | Ver productos |
| CLIENTE_GESTIONAR | Gestionar clientes |
| REPORTE_VER | Ver reportes |
| CONFIGURACION_EDITAR | Editar configuración |

---

## 11. Guía de Ejecución

### 11.1 Requisitos Previos

- Docker y Docker Compose instalados
- Puertos 3000, 5432, 8080, 8081 disponibles

### 11.2 Comandos

```bash
# Clonar repositorio
git clone <repository-url>
cd erp

# Levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

### 11.3 Accesos

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3000 |
| API Backend | http://localhost:8080 |
| Adminer (BD) | http://localhost:8081 |

### 11.4 Credenciales por Defecto

```
Usuario: admin
Password: admin123
```

---

## 12. Estado del Proyecto

### 12.1 Funcionalidades Completadas

- [x] Autenticación JWT
- [x] Sistema de roles y permisos
- [x] Login/logout en frontend
- [x] Dashboard con información del usuario
- [x] Layout con sidebar y navbar
- [x] Rutas protegidas
- [x] Configuración Docker completa
- [x] Base de datos con estructura inicial

### 12.2 Funcionalidades Pendientes

- [ ] CRUD de Productos
- [ ] CRUD de Clientes
- [ ] Módulo de Ventas (POS)
- [ ] Generación de Reportes
- [ ] Configuración del sistema
- [ ] Entidades Java para Producto, Cliente, Venta
- [ ] Endpoints de negocio

---

## 13. Consideraciones de Seguridad

### 13.1 Problemas Actuales

| Problema | Severidad | Recomendación |
|----------|-----------|---------------|
| Endpoints de debugging públicos | Alta | Eliminar en producción |
| JWT secret hardcodeado | Media | Mover a variable de entorno |
| Logging TRACE/DEBUG | Baja | Deshabilitar en producción |
| Password admin sin hashear | Alta | Ejecutar reset inicial |

### 13.2 Buenas Prácticas Implementadas

- Contraseñas hasheadas con BCrypt
- Tokens JWT con firma HS512
- Sesiones stateless
- CORS configurado
- Validación de tokens en cada request

---

## 14. Diagrama de Arquitectura

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│    Frontend     │────▶│    Backend      │────▶│   PostgreSQL    │
│   (React/Nginx) │     │  (Spring Boot)  │     │                 │
│   Port: 3000    │     │   Port: 8080    │     │   Port: 5432    │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        └───────────────────────┴───────────────────────┘
                                │
                        ┌───────┴───────┐
                        │  pos-network  │
                        │   (Docker)    │
                        └───────────────┘
```

---

## 15. Contacto y Soporte

Para reportar problemas o solicitar nuevas funcionalidades, crear un issue en el repositorio del proyecto.

---

*Documentación generada el: Diciembre 2024*

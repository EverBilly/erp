# MO01 - Plan de Desarrollo: Módulo Usuario

**Fecha:** 2025-12-21
**Estado:** En desarrollo
**Prioridad:** Alta

---

## 1. Descripción del Módulo

El módulo de Usuario gestiona la información de los usuarios del sistema POS, incluyendo su creación, consulta, edición y desactivación. Es la base para el sistema de autenticación y autorización.

---

## 2. Estado Actual

| Componente | Estado | Archivo |
|------------|--------|---------|
| Entity | Completado | `usuario/model/Usuario.java` |
| Repository | Completado | `usuario/repository/UsuarioRepository.java` |
| Service | Parcial | `usuario/service/UsuarioService.java` |
| DTOs | Pendiente | `usuario/dto/` |
| Controller | Pendiente | `usuario/controller/` |
| Tests | Pendiente | `usuario/test/` |
| Validaciones | Pendiente | - |

---

## 3. Historias de Usuario

| ID | Historia | Prioridad | Estado |
|----|----------|-----------|--------|
| US-01 | Como administrador, quiero listar todos los usuarios para ver quiénes tienen acceso al sistema | Alta | Pendiente |
| US-02 | Como administrador, quiero crear un nuevo usuario para dar acceso a un empleado | Alta | Pendiente |
| US-03 | Como administrador, quiero ver los detalles de un usuario específico | Alta | Pendiente |
| US-04 | Como administrador, quiero editar los datos de un usuario | Media | Pendiente |
| US-05 | Como administrador, quiero desactivar un usuario sin eliminarlo | Media | Pendiente |
| US-06 | Como administrador, quiero buscar usuarios por nombre | Baja | Pendiente |

---

## 4. Casos de Uso Detallados

### CU-01: Listar Usuarios

- **Actor:** Administrador (autenticado)
- **Endpoint:** `GET /api/usuarios`
- **Entrada:** Ninguna (opcional: filtro de activos)
- **Salida:** Lista de usuarios sin contraseña
- **Reglas de negocio:**
  - Por defecto lista solo usuarios activos
  - Nunca exponer el campo password
- **Códigos de respuesta:**
  - 200: Lista obtenida exitosamente
  - 401: No autenticado
  - 403: Sin permisos

### CU-02: Crear Usuario

- **Actor:** Administrador (autenticado)
- **Endpoint:** `POST /api/usuarios`
- **Entrada:**
  ```json
  {
    "username": "string (3-50 chars, único)",
    "email": "string (email válido, único)",
    "password": "string (mín 6 chars)",
    "nombre": "string (obligatorio)",
    "apellido": "string (obligatorio)",
    "rolIds": [1, 2]  // opcional
  }
  ```
- **Salida:** Usuario creado (sin password)
- **Reglas de negocio:**
  - Username debe ser único
  - Email debe ser único y formato válido
  - Password se almacena hasheado con BCrypt
  - Usuario se crea activo por defecto
- **Códigos de respuesta:**
  - 201: Usuario creado
  - 400: Datos inválidos
  - 409: Username o email duplicado

### CU-03: Obtener Usuario por ID

- **Actor:** Administrador (autenticado)
- **Endpoint:** `GET /api/usuarios/{id}`
- **Entrada:** ID del usuario (path param)
- **Salida:** Detalles del usuario (sin password)
- **Reglas de negocio:**
  - Nunca exponer password
- **Códigos de respuesta:**
  - 200: Usuario encontrado
  - 404: Usuario no existe

### CU-04: Actualizar Usuario

- **Actor:** Administrador (autenticado)
- **Endpoint:** `PUT /api/usuarios/{id}`
- **Entrada:**
  ```json
  {
    "email": "string (opcional)",
    "nombre": "string (opcional)",
    "apellido": "string (opcional)",
    "activo": boolean (opcional),
    "rolIds": [1, 2] (opcional)
  }
  ```
- **Salida:** Usuario actualizado
- **Reglas de negocio:**
  - No se puede cambiar el username
  - Si se proporciona password, se hashea
  - Email debe seguir siendo único
- **Códigos de respuesta:**
  - 200: Usuario actualizado
  - 400: Datos inválidos
  - 404: Usuario no existe
  - 409: Email duplicado

### CU-05: Desactivar Usuario

- **Actor:** Administrador (autenticado)
- **Endpoint:** `PATCH /api/usuarios/{id}/desactivar`
- **Entrada:** ID del usuario
- **Salida:** Usuario desactivado
- **Reglas de negocio:**
  - Soft delete (cambia activo a false)
  - Usuario no puede loguearse si está inactivo
- **Códigos de respuesta:**
  - 200: Usuario desactivado
  - 404: Usuario no existe

---

## 5. Tareas Técnicas (TDD)

### Fase 1: DTOs y Estructura (Sprint actual)

| # | Tarea | Archivo | Descripción |
|---|-------|---------|-------------|
| T-01 | Crear DTO Response | `UsuarioResponse.java` | DTO para respuestas (sin password) |
| T-02 | Crear DTO Request | `CrearUsuarioRequest.java` | DTO para crear usuario con validaciones |
| T-03 | Crear DTO Update | `ActualizarUsuarioRequest.java` | DTO para actualizar usuario |

### Fase 2: Service con Tests

| # | Tarea | Test | Descripción |
|---|-------|------|-------------|
| T-04 | Test listar usuarios | `UsuarioServiceTest` | Test para findAll() |
| T-05 | Test crear usuario | `UsuarioServiceTest` | Test para save() con validaciones |
| T-06 | Test buscar por ID | `UsuarioServiceTest` | Test para findById() |
| T-07 | Refactorizar Service | `UsuarioService` | Implementar lógica según tests |

### Fase 3: Controller con Tests

| # | Tarea | Test | Descripción |
|---|-------|------|-------------|
| T-08 | Test GET /usuarios | `UsuarioControllerTest` | Test endpoint listar |
| T-09 | Test POST /usuarios | `UsuarioControllerTest` | Test endpoint crear |
| T-10 | Test GET /usuarios/{id} | `UsuarioControllerTest` | Test endpoint obtener |
| T-11 | Implementar Controller | `UsuarioController` | Implementar endpoints |

### Fase 4: Validaciones y Errores

| # | Tarea | Descripción |
|---|-------|-------------|
| T-12 | Validaciones Jakarta | Agregar @NotBlank, @Email, @Size |
| T-13 | Exception Handler | Manejo centralizado de errores |
| T-14 | Tests de validación | Tests para casos de error |

---

## 6. Estructura de Archivos Objetivo

```
src/main/java/com/pos/usuario/
├── controller/
│   └── UsuarioController.java
├── dto/
│   ├── UsuarioResponse.java
│   ├── CrearUsuarioRequest.java
│   └── ActualizarUsuarioRequest.java
├── model/
│   └── Usuario.java
├── repository/
│   └── UsuarioRepository.java
├── service/
│   └── UsuarioService.java
└── exception/
    └── UsuarioNotFoundException.java

src/test/java/com/pos/usuario/
├── service/
│   └── UsuarioServiceTest.java
└── controller/
    └── UsuarioControllerTest.java
```

---

## 7. Principios SOLID a Aplicar

| Principio | Aplicación |
|-----------|------------|
| **S** - Single Responsibility | Service solo lógica de negocio, Controller solo HTTP |
| **O** - Open/Closed | DTOs separados para Request/Response |
| **L** - Liskov | No aplica aún |
| **I** - Interface Segregation | No crear interfaces innecesarias por ahora |
| **D** - Dependency Inversion | Service depende de Repository (abstracción) |

---

## 8. Criterios de Aceptación

- [ ] Todos los endpoints responden correctamente
- [ ] Password nunca se expone en respuestas
- [ ] Validaciones funcionan y retornan mensajes claros
- [ ] Cobertura de tests > 80% en Service
- [ ] Tests de integración para Controller
- [ ] Documentación de API actualizada

---

## 9. Notas de Implementación

- Usar BCrypt para hashear passwords (ya configurado)
- Los roles se asignan por ID, no se crean desde usuario
- Mantener compatibilidad con el AuthService existente

---

## Changelog

| Fecha | Cambio |
|-------|--------|
| 2025-12-21 | Documento inicial creado |

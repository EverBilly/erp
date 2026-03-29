# Diccionario de Datos - POS-PyMEs ERP

Referencia de nombres en ingles con su equivalente en español.

---

## Tablas

| Tabla (ingles) | Concepto (español) | Descripcion |
|----------------|-------------------|-------------|
| `tenants` | Inquilinos/Empresas | Empresas u organizaciones que usan el sistema |
| `users` | Usuarios | Cuentas de usuario del sistema |
| `roles` | Roles | Roles del sistema (SUPER_ADMIN, ADMIN, USER) |
| `menus` | Menus | Items de navegacion del sidebar |
| `permissions` | Permisos | Permisos granulares por modulo |
| `user_sessions` | Sesiones de usuario | Registro de sesiones activas con JWT |
| `user_roles` | Usuario-Rol | Tabla puente: que roles tiene cada usuario |
| `role_permissions` | Rol-Permiso | Tabla puente: que permisos tiene cada rol |
| `role_menus` | Rol-Menu | Tabla puente: que menus puede ver cada rol |

---

## Columnas por tabla

### tenants

| Columna | Español | Tipo | Descripcion |
|---------|---------|------|-------------|
| `id` | id | BIGSERIAL PK | Identificador unico |
| `identifier` | identificador | VARCHAR(50) UNIQUE | Slug unico (ej: 'tenant-default') |
| `name` | nombre | VARCHAR(150) | Nombre de la empresa |
| `plan` | plan | VARCHAR(50) | Nivel de plan (free, pro, enterprise) |
| `active` | activo | BOOLEAN | Si el tenant esta habilitado |
| `created_at` | fecha de creacion | TIMESTAMP | Cuando se creo |
| `config` | configuracion | JSONB | Configuracion flexible en JSON |
| `logo_url` | URL del logo | VARCHAR(255) | URL del logo de la empresa |

### users

| Columna | Español | Tipo | Descripcion |
|---------|---------|------|-------------|
| `id` | id | BIGSERIAL PK | Identificador unico |
| `tenant_id` | id del inquilino | BIGINT FK | A que empresa pertenece |
| `username` | nombre de usuario | VARCHAR(50) UNIQUE | Login del usuario |
| `email` | correo | VARCHAR(100) UNIQUE | Email del usuario |
| `password_hash` | hash de contraseña | VARCHAR(255) | Password encriptado con BCrypt |
| `active` | activo | BOOLEAN | Si la cuenta esta habilitada |
| `created_at` | fecha de creacion | TIMESTAMP | Cuando se creo la cuenta |
| `last_login` | ultimo login | TIMESTAMP | Ultima vez que inicio sesion |
| `login_attempts` | intentos de login | INTEGER | Intentos fallidos acumulados |
| `locked_until` | bloqueado hasta | TIMESTAMP | Fecha hasta la que esta bloqueado |
| `full_name` | nombre completo | VARCHAR(150) | Nombre y apellido |
| `phone` | telefono | VARCHAR(20) | Numero de telefono |
| `avatar_url` | URL del avatar | VARCHAR(255) | URL de la foto de perfil |
| `timezone` | zona horaria | VARCHAR(255) | Zona horaria del usuario |
| `locale` | idioma | VARCHAR(255) | Idioma preferido (es, en) |
| `metadata` | metadatos | JSONB | Datos flexibles en JSON |

### roles

| Columna | Español | Tipo | Descripcion |
|---------|---------|------|-------------|
| `id` | id | BIGSERIAL PK | Identificador unico |
| `name` | nombre | VARCHAR(50) UNIQUE | Nombre del rol (SUPER_ADMIN, ADMIN, USER) |
| `description` | descripcion | VARCHAR(255) | Que puede hacer este rol |
| `priority_level` | nivel de prioridad | INTEGER | Prioridad (1000=super, 500=admin, 10=user) |
| `active` | activo | BOOLEAN | Si el rol esta habilitado |
| `is_system` | es del sistema | BOOLEAN | Si es un rol predefinido que no se puede borrar |
| `created_at` | fecha de creacion | TIMESTAMP | Cuando se creo |
| `created_by` | creado por | BIGINT FK | ID del usuario que lo creo |

### menus

| Columna | Español | Tipo | Descripcion |
|---------|---------|------|-------------|
| `id` | id | BIGSERIAL PK | Identificador unico |
| `tenant_id` | id del inquilino | BIGINT FK | A que empresa pertenece |
| `name` | nombre | VARCHAR(100) | Texto que se muestra en el sidebar |
| `path` | ruta | VARCHAR(255) | URL de navegacion (ej: /usuarios) |
| `icon` | icono | VARCHAR(50) | Nombre del icono MUI (ej: users, home) |
| `sort_order` | orden | INTEGER | Posicion en el menu (menor = primero) |
| `parent_id` | id del padre | BIGINT FK | Menu padre (NULL = raiz) |
| `visible` | visible | BOOLEAN | Si se muestra en el sidebar |
| `requires_permission` | requiere permiso | BOOLEAN | Si necesita validar permisos |
| `component` | componente | VARCHAR(100) | Componente React asociado |
| `description` | descripcion | VARCHAR(255) | Descripcion del menu |
| `params` | parametros | JSONB | Parametros adicionales en JSON |
| `is_external` | es externo | BOOLEAN | Si es un link externo |
| `open_in_new_tab` | abrir en nueva pestaña | BOOLEAN | Si abre en tab nueva |
| `badge_text` | texto del badge | VARCHAR(20) | Texto del indicador (ej: "Nuevo") |
| `badge_color` | color del badge | VARCHAR(20) | Color del indicador |
| `created_at` | fecha de creacion | TIMESTAMP | Cuando se creo |
| `updated_at` | fecha de actualizacion | TIMESTAMP | Ultima modificacion |

### permissions

| Columna | Español | Tipo | Descripcion |
|---------|---------|------|-------------|
| `id` | id | BIGSERIAL PK | Identificador unico |
| `code` | codigo | VARCHAR(100) UNIQUE | Codigo unico (ej: users.view, roles.create) |
| `name` | nombre | VARCHAR(150) | Nombre descriptivo |
| `description` | descripcion | VARCHAR(255) | Que permite hacer |
| `module` | modulo | VARCHAR(50) | A que modulo pertenece (users, roles, menus) |
| `category` | categoria | VARCHAR(50) | Tipo de permiso (read, write, access) |
| `security_level` | nivel de seguridad | INTEGER | Nivel de restriccion (1=bajo, 10=critico) |
| `active` | activo | BOOLEAN | Si esta habilitado |
| `created_at` | fecha de creacion | TIMESTAMP | Cuando se creo |

### user_sessions

| Columna | Español | Tipo | Descripcion |
|---------|---------|------|-------------|
| `id` | id | UUID PK | Identificador unico de sesion |
| `user_id` | id del usuario | BIGINT FK | Usuario dueño de la sesion |
| `access_token` | token de acceso | VARCHAR(255) | JWT token actual |
| `refresh_token` | token de refresco | VARCHAR(255) | Token para renovar sesion |
| `device` | dispositivo | VARCHAR(100) | Tipo de dispositivo |
| `browser` | navegador | VARCHAR(100) | Navegador web usado |
| `operating_system` | sistema operativo | VARCHAR(50) | SO del dispositivo |
| `ip_address` | direccion IP | VARCHAR(255) | IP desde donde se conecto |
| `latitude` | latitud | DOUBLE | Ubicacion geografica |
| `longitude` | longitud | DOUBLE | Ubicacion geografica |
| `city` | ciudad | VARCHAR(100) | Ciudad de conexion |
| `country` | pais | VARCHAR(100) | Pais de conexion |
| `active` | activa | BOOLEAN | Si la sesion sigue vigente |
| `started_at` | fecha de inicio | TIMESTAMP | Cuando inicio la sesion |
| `expires_at` | fecha de expiracion | TIMESTAMP | Cuando expira |
| `last_activity_at` | ultima actividad | TIMESTAMP | Ultimo request del usuario |
| `failed_attempts` | intentos fallidos | INTEGER | Intentos fallidos en esta sesion |

### role_menus (tabla puente con permisos granulares)

| Columna | Español | Tipo | Descripcion |
|---------|---------|------|-------------|
| `role_id` | id del rol | BIGINT PK/FK | Rol que tiene acceso |
| `menu_id` | id del menu | BIGINT PK/FK | Menu al que puede acceder |
| `active` | activo | BOOLEAN | Si la asignacion esta activa |
| `can_view` | puede ver | BOOLEAN | Permiso de lectura |
| `can_edit` | puede editar | BOOLEAN | Permiso de escritura |
| `can_delete` | puede eliminar | BOOLEAN | Permiso de eliminacion |

---

## Convenciones de nombres

| Patron SQL | Ejemplo | Regla |
|-----------|---------|-------|
| Tablas | `user_roles` | snake_case, plural, ingles |
| Columnas | `full_name` | snake_case, ingles |
| Primary key | `id` | Siempre `id` |
| Foreign key | `user_id` | `{tabla_singular}_id` |
| Timestamps | `created_at`, `updated_at` | Siempre con sufijo `_at` |
| Booleanos | `active`, `is_system` | Sin prefijo o con `is_`/`can_` |
| JSONB | `config`, `metadata`, `params` | Nombre corto sin tipo |

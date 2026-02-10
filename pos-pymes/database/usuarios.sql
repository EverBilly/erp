-- ============================================
-- SISTEMA COMPLETO DE USUARIOS, ROLES, PERMISOS Y MENÚS
-- ============================================

-- 1. Crear tabla de Tenants (Inquilinos/Empresas)
CREATE TABLE IF NOT EXISTS tenants (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    identificador VARCHAR(50) UNIQUE NOT NULL, -- ej: 'restaurante-mario', 'colegio-san-judas'
    plan VARCHAR(50) DEFAULT 'free', -- free, pro, enterprise
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    configuracion JSONB DEFAULT '{}',
    logo_url TEXT
);


-- 1. TABLA DE USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_login TIMESTAMP,
    intentos_login INTEGER DEFAULT 0,
    bloqueado_hasta TIMESTAMP,
    nombre_completo VARCHAR(150),
    telefono VARCHAR(20),
    avatar_url TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC',
    idioma VARCHAR(10) DEFAULT 'es',
    metadata JSONB,
    tenant_id INTEGER REFERENCES tenants(id) ON DELETE CASCADE
);

-- 2. TABLA DE ROLES
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT,
    nivel_prioridad INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    es_sistema BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creado_por INTEGER REFERENCES usuarios(id),
    tenant_id INTEGER REFERENCES tenants(id) ON DELETE CASCADE
);

-- 3. TABLA DE MENÚS
CREATE TABLE IF NOT EXISTS menus (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ruta VARCHAR(255),
    icono VARCHAR(50),
    orden INTEGER DEFAULT 0,
    parent_id INTEGER REFERENCES menus(id) ON DELETE CASCADE,
    visible BOOLEAN DEFAULT true,
    requiere_permiso BOOLEAN DEFAULT true,
    componente VARCHAR(100),
    descripcion TEXT,
    parametros JSONB,
    es_externo BOOLEAN DEFAULT false,
    abrir_en_nueva_ventana BOOLEAN DEFAULT false,
    badge_text VARCHAR(20),
    badge_color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id INTEGER REFERENCES tenants(id) ON DELETE CASCADE
);

-- 4. TABLA DE PERMISOS
CREATE TABLE IF NOT EXISTS permisos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    modulo VARCHAR(50),
    categoria VARCHAR(50),
    nivel_seguridad INTEGER DEFAULT 1,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. TABLA USUARIO_ROL
CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    asignado_por INTEGER REFERENCES usuarios(id),
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expira_en TIMESTAMP,
    PRIMARY KEY (usuario_id, rol_id)
);

-- 6. TABLA ROL_PERMISO
CREATE TABLE IF NOT EXISTS rol_permiso (
    rol_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    permiso_id INTEGER REFERENCES permisos(id) ON DELETE CASCADE,
    concedido BOOLEAN DEFAULT true,
    restricciones JSONB,
    PRIMARY KEY (rol_id, permiso_id)
);

-- 7. TABLA ROL_MENU
CREATE TABLE IF NOT EXISTS rol_menu (
    rol_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    menu_id INTEGER REFERENCES menus(id) ON DELETE CASCADE,
    activo BOOLEAN DEFAULT true,
    puede_ver BOOLEAN DEFAULT true,
    puede_editar BOOLEAN DEFAULT false,
    puede_eliminar BOOLEAN DEFAULT false,
    restricciones JSONB,
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    asignado_por INTEGER REFERENCES usuarios(id),
    PRIMARY KEY (rol_id, menu_id)
);

-- 8. TABLA USUARIO_MENU (PERMISOS DIRECTOS)
CREATE TABLE IF NOT EXISTS usuario_menu (
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    menu_id INTEGER REFERENCES menus(id) ON DELETE CASCADE,
    activo BOOLEAN DEFAULT true,
    habilitado BOOLEAN DEFAULT true,
    puede_ver BOOLEAN DEFAULT true,
    puede_editar BOOLEAN DEFAULT false,
    puede_eliminar BOOLEAN DEFAULT false,
    restricciones JSONB,
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    asignado_por INTEGER REFERENCES usuarios(id),
    PRIMARY KEY (usuario_id, menu_id)
);

-- 9. TABLA USUARIO_PERMISO (PERMISOS DIRECTOS)
CREATE TABLE IF NOT EXISTS usuario_permiso (
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    permiso_id INTEGER REFERENCES permisos(id) ON DELETE CASCADE,
    concedido BOOLEAN DEFAULT true,
    restricciones JSONB,
    PRIMARY KEY (usuario_id, permiso_id)
);

-- 10. TABLA DE SESIONES
CREATE TABLE IF NOT EXISTS sesiones_usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    token_actual TEXT NOT NULL,
    token_refresh TEXT NOT NULL,
    dispositivo VARCHAR(100),
    navegador VARCHAR(100),
    sistema_operativo VARCHAR(50),
    ip_address INET,
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    ciudad VARCHAR(100),
    pais VARCHAR(100),
    activa BOOLEAN DEFAULT true,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP,
    fecha_ultima_actividad TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    intentos_fallidos INTEGER DEFAULT 0
);

-- 11. TABLA DE ACTIVIDAD
CREATE TABLE IF NOT EXISTS actividad_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuarios(id),
    tipo_actividad VARCHAR(50) NOT NULL,
    modulo VARCHAR(50),
    descripcion TEXT,
    detalles JSONB,
    ip_address INET,
    user_agent TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. TABLA DE AUDITORÍA
CREATE TABLE IF NOT EXISTS auditoria_permisos (
    id BIGSERIAL PRIMARY KEY,
    tabla_afectada VARCHAR(50) NOT NULL,
    registro_id INTEGER NOT NULL,
    accion VARCHAR(10) NOT NULL,
    valores_anteriores JSONB,
    valores_nuevos JSONB,
    usuario_id INTEGER REFERENCES usuarios(id),
    ip_address INET,
    user_agent TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. TABLA DE CONFIGURACIÓN
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id SERIAL PRIMARY KEY,
    clave VARCHAR(100) UNIQUE NOT NULL,
    valor TEXT,
    tipo VARCHAR(50) DEFAULT 'string',
    categoria VARCHAR(50) DEFAULT 'general',
    descripcion TEXT,
    editable BOOLEAN DEFAULT true,
    visible BOOLEAN DEFAULT true,
    requerida BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER REFERENCES usuarios(id)
);

-- 14. TABLA DE PLANTILLAS DE ROL
CREATE TABLE IF NOT EXISTS plantillas_rol (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    permisos_predeterminados JSONB,
    menus_predeterminados JSONB,
    activa BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creado_por INTEGER REFERENCES usuarios(id)
);

-- 15. TABLA DE REGLAS
CREATE TABLE IF NOT EXISTS reglas_permisos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    condicion_sql TEXT NOT NULL,
    mensaje_error TEXT,
    activa BOOLEAN DEFAULT true,
    prioridad INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 16. TABLA DE BACKUPS
CREATE TABLE IF NOT EXISTS backup_permisos (
    id BIGSERIAL PRIMARY KEY,
    fecha_backup TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(100),
    realizado_por INTEGER REFERENCES usuarios(id),
    datos JSONB NOT NULL,
    version_schema VARCHAR(20) DEFAULT '1.0'
);

-- ============================================
-- ÍNDICES PARA MEJOR PERFORMANCE
-- ============================================

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);

CREATE INDEX idx_roles_activo ON roles(activo);
CREATE INDEX idx_roles_prioridad ON roles(nivel_prioridad);

CREATE INDEX idx_menus_parent ON menus(parent_id);
CREATE INDEX idx_menus_orden ON menus(orden);
CREATE INDEX idx_menus_visible ON menus(visible);
CREATE INDEX idx_menus_ruta ON menus(ruta);

CREATE INDEX idx_permisos_codigo ON permisos(codigo);
CREATE INDEX idx_permisos_modulo ON permisos(modulo);
CREATE INDEX idx_permisos_activo ON permisos(activo);

CREATE INDEX idx_usuario_rol_usuario ON usuario_rol(usuario_id);
CREATE INDEX idx_usuario_rol_rol ON usuario_rol(rol_id);

CREATE INDEX idx_rol_permiso_rol ON rol_permiso(rol_id);
CREATE INDEX idx_rol_permiso_permiso ON rol_permiso(permiso_id);

CREATE INDEX idx_rol_menu_rol ON rol_menu(rol_id);
CREATE INDEX idx_rol_menu_menu ON rol_menu(menu_id);

CREATE INDEX idx_usuario_menu_usuario ON usuario_menu(usuario_id);
CREATE INDEX idx_usuario_menu_menu ON usuario_menu(menu_id);

CREATE INDEX idx_usuario_permiso_usuario ON usuario_permiso(usuario_id);
CREATE INDEX idx_usuario_permiso_permiso ON usuario_permiso(permiso_id);

CREATE INDEX idx_sesiones_usuario ON sesiones_usuario(usuario_id);
CREATE INDEX idx_sesiones_activa ON sesiones_usuario(activa);
CREATE INDEX idx_sesiones_token ON sesiones_usuario(token_actual);
CREATE INDEX idx_sesiones_expira ON sesiones_usuario(fecha_expiracion);

CREATE INDEX idx_actividad_usuario ON actividad_usuario(usuario_id);
CREATE INDEX idx_actividad_fecha ON actividad_usuario(fecha DESC);
CREATE INDEX idx_actividad_tipo ON actividad_usuario(tipo_actividad);

CREATE INDEX idx_auditoria_tabla ON auditoria_permisos(tabla_afectada);
CREATE INDEX idx_auditoria_fecha ON auditoria_permisos(fecha DESC);
CREATE INDEX idx_auditoria_usuario ON auditoria_permisos(usuario_id);

CREATE INDEX idx_config_clave ON configuracion_sistema(clave);
CREATE INDEX idx_config_categoria ON configuracion_sistema(categoria);

-- ============================================
-- DATOS INICIALES ESENCIALES
-- ============================================

-- 2. Insertar un Tenant por defecto para tus datos actuales
INSERT INTO tenants (nombre, identificador, plan) 
VALUES ('Sistema Principal', 'tenant-default', 'enterprise');

-- Password: password123 -> bcrypt hash
INSERT INTO usuarios (username, email, password_hash, activo, nombre_completo, telefono, idioma, metadata, tenant_id) VALUES
-- Super Administrador
('superadmin', 'superadmin@sistema.com', '$2a$10$YeyMj3Ki4cVOcfuE3MIaDu98qZqrG/TJ4hNGrcgqliE/DqGMgO0fm', true, 'Super Administrador Principal', '+525512345678', 'es', '{"notificaciones": true, "tema": "oscuro"}', 1),

-- Administradores
('admin', 'admin1@sistema.com', '$2a$10$YeyMj3Ki4cVOcfuE3MIaDu98qZqrG/TJ4hNGrcgqliE/DqGMgO0fm', true, 'Ana López Rodríguez', '+525511112222', 'es', '{"notificaciones": true, "tema": "claro"}', 1),
('admin2', 'admin2@sistema.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Carlos Martínez García', '+525522223333', 'es', '{"notificaciones": false, "tema": "claro"}', 1),

-- Usuarios con diferentes roles
('gerente1', 'gerente1@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Roberto Sánchez Pérez', '+525533334444', 'es', '{"departamento": "ventas", "puesto": "gerente"}', 1),
('gerente2', 'gerente2@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'María Fernández Castro', '+525544445555', 'es', '{"departamento": "marketing", "puesto": "gerente"}', 1),

-- Supervisores
('supervisor1', 'supervisor1@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Jorge Ramírez Díaz', '+525555556666', 'es', '{"departamento": "producción", "puesto": "supervisor"}', 1),
('supervisor2', 'supervisor2@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Laura Gómez Méndez', '+525566667777', 'es', '{"departamento": "calidad", "puesto": "supervisor"}', 1),
-- Usuarios regulares
('empleado1', 'empleado1@empresa.com', '$2a$10$YeyMj3Ki4cVOcfuE3MIaDu98qZqrG/TJ4hNGrcgqliE/DqGMgO0fm', true, 'Pedro Hernández Luna', '+525577778888', 'es', '{"departamento": "ventas", "puesto": "vendedor"}', 1),
('empleado2', 'empleado2@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Sofía Vargas Ruíz', '+525588889999', 'es', '{"departamento": "marketing", "puesto": "diseñador"}', 1),
('empleado3', 'empleado3@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Miguel Torres Ortega', '+525599990000', 'es', '{"departamento": "producción", "puesto": "operador"}', 1),
('empleado4', 'empleado4@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Gabriela Reyes Soto', '+525500001111', 'es', '{"departamento": "calidad", "puesto": "inspector"}', 1),

-- Usuario inactivo (bloqueado)
('inactivo1', 'inactivo@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', false, 'Usuario Bloqueado', '+525511110000', 'es', '{"motivo_bloqueo": "intentos fallidos excedidos"}', 1),

-- Usuario con múltiples roles
('multiroles', 'multiroles@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Usuario Multirol', '+525522221111', 'en', '{"departamento": "sistemas", "puesto": "desarrollador"}', 1),

-- Usuario para auditor
('auditor1', 'auditor1@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Auditor Interno', '+525533332222', 'es', '{"departamento": "auditoria", "puesto": "auditor"}', 1),

-- Usuario de solo lectura
('lectura1', 'lectura1@empresa.com', '$2b$10$4V1qVl5nYwQY1ZQ6WQ7QjOe8v8YwL1XqL9N8tR7S6T5U4V3W2E1R', true, 'Usuario Solo Lectura', '+525544443333', 'es', '{"departamento": "consultoria", "puesto": "consultor"}',1);

-- Insertar rol super administrador
INSERT INTO roles (nombre, descripcion, nivel_prioridad, es_sistema, tenant_id) 
VALUES 
('SUPER_ADMIN', 'Acceso completo a todo el sistema', 1000, true, 1),
('ADMIN', 'Administra usuarios y permisos', 100, true, 1),
('USER', 'Usuario básico del sistema', 10, true, 1);

-- ============================================
-- 3. Asignar rol Super Administrador al superadmin
-- ============================================
INSERT INTO usuario_rol (usuario_id, rol_id, asignado_por)
VALUES
(1, 1, 1),
(2, 2, 1),
(8, 3, 1)
ON CONFLICT (usuario_id, rol_id) DO NOTHING;

-- Insertar menús básicos del sistema
INSERT INTO menus (nombre, ruta, icono, orden, parent_id, descripcion, tenant_id) VALUES
('Dashboard', '/dashboard', 'home', 1, NULL, 'Panel principal', 1),
('Administración', '/admin', 'settings', 100, NULL, 'Módulo de administración', 1),
('Usuarios', '/usuarios', 'users', 1, 2, 'Gestión de usuarios', 1),
('Roles', '/admin/roles', 'shield', 2, 2, 'Gestión de roles', 1),
('Permisos', '/admin/permisos', 'key', 3, 2, 'Gestión de permisos', 1),
('Menús', '/admin/menus', 'menu', 4, 2, 'Gestión de menús', 1),
('Configuración', '/admin/config', 'sliders', 5, 2, 'Configuración del sistema', 1),
('Auditoría', '/admin/auditoria', 'activity', 6, 2, 'Registros de auditoría', 1),
('Reportes', '/admin/reportes', 'bar-chart', 7, 2, 'Reportes del sistema', 1),
('Mi Perfil', '/perfil', 'user', 2, NULL, 'Perfil del usuario', 1),
('Cambiar Contraseña', '/perfil/password', 'lock', 1, 10, 'Cambiar contraseña', 1),
('Mis Sesiones', '/perfil/sesiones', 'monitor', 2, 10, 'Sesiones activas', 1),
('Mi Actividad', '/perfil/actividad', 'list', 3, 10, 'Historial de actividad', 1);

-- INSERT INTO menus (id, nombre, ruta, icono, orden, visible) VALUES
-- (1, 'Usuarios', '/usuarios', '👤', 10, true),
-- (2, 'Productos', '/productos', '📦', 20, true),
-- (3, 'Ventas', '/ventas', '💰', 30, true),
-- (4, 'Reportes', '/reportes', '📊', 40, true);
-- ============================================
-- 4. Menús esenciales
-- ============================================
-- INSERT INTO menus (id, nombre, ruta, icono, orden, parent_id, visible, requiere_permiso, componente)
-- VALUES
-- (1, 'Dashboard', '/dashboard', 'home', 1, NULL, true, false, 'DashboardPage'),
-- (2, 'Administración', '/admin', 'settings', 100, NULL, true, true, 'AdminLayout'),
-- (3, 'Usuarios', '/admin/usuarios', 'users', 1, 2, true, true, 'UsuariosPage'),
-- (4, 'Roles', '/admin/roles', 'shield', 2, 2, true, true, 'RolesPage'),
-- (5, 'Permisos', '/admin/permisos', 'key', 3, 2, true, true, 'PermisosPage'),
-- (6, 'Menús', '/admin/menus', 'menu', 4, 2, true, true, 'MenusPage'),
-- (7, 'Mi Perfil', '/perfil', 'user', 2, NULL, true, false, 'PerfilPage')
-- ON CONFLICT (ruta) DO NOTHING;

-- Super Admin (rol_id=1) puede ver todos los menús
-- Admin (rol_id=2) puede ver Productos, Ventas, Reportes
INSERT INTO rol_menu (rol_id, menu_id, activo, puede_ver) 
VALUES 
(1, 1, true, true),
(1, 2, true, true),
(1, 3, true, true),
(1, 4, true, true),
(2, 2, true, true),
(2, 3, true, true),
(2, 4, true, true);


-- Insertar permisos básicos del sistema
INSERT INTO permisos (codigo, nombre, descripcion, modulo, categoria) VALUES
-- Módulo de usuarios
('usuarios.ver', 'Ver usuarios', 'Ver lista de usuarios', 'usuarios', 'lectura'),
('usuarios.crear', 'Crear usuarios', 'Crear nuevos usuarios', 'usuarios', 'escritura'),
('usuarios.editar', 'Editar usuarios', 'Editar usuarios existentes', 'usuarios', 'escritura'),
('usuarios.eliminar', 'Eliminar usuarios', 'Eliminar usuarios', 'usuarios', 'escritura'),
('usuarios.asignar_roles', 'Asignar roles', 'Asignar roles a usuarios', 'usuarios', 'privilegios'),

-- Módulo de roles
('roles.ver', 'Ver roles', 'Ver lista de roles', 'roles', 'lectura'),
('roles.crear', 'Crear roles', 'Crear nuevos roles', 'roles', 'escritura'),
('roles.editar', 'Editar roles', 'Editar roles existentes', 'roles', 'escritura'),
('roles.eliminar', 'Eliminar roles', 'Eliminar roles', 'roles', 'escritura'),
('roles.asignar_permisos', 'Asignar permisos', 'Asignar permisos a roles', 'roles', 'privilegios'),

-- Módulo de menús
('menus.ver', 'Ver menús', 'Ver estructura de menús', 'menus', 'lectura'),
('menus.crear', 'Crear menús', 'Crear nuevos menús', 'menus', 'escritura'),
('menus.editar', 'Editar menús', 'Editar menús existentes', 'menus', 'escritura'),
('menus.eliminar', 'Eliminar menús', 'Eliminar menús', 'menus', 'escritura'),
('menus.asignar_roles', 'Asignar a roles', 'Asignar menús a roles', 'menus', 'privilegios'),

-- Módulo de permisos
('permisos.ver', 'Ver permisos', 'Ver lista de permisos', 'permisos', 'lectura'),
('permisos.crear', 'Crear permisos', 'Crear nuevos permisos', 'permisos', 'escritura'),
('permisos.editar', 'Editar permisos', 'Editar permisos existentes', 'permisos', 'escritura'),
('permisos.eliminar', 'Eliminar permisos', 'Eliminar permisos', 'permisos', 'escritura'),

-- Módulo de auditoría
('auditoria.ver', 'Ver auditoría', 'Ver registros de auditoría', 'auditoria', 'lectura'),
('auditoria.eliminar', 'Eliminar auditoría', 'Eliminar registros de auditoría', 'auditoria', 'escritura'),

-- Módulo de configuración
('config.ver', 'Ver configuración', 'Ver configuración del sistema', 'config', 'lectura'),
('config.editar', 'Editar configuración', 'Editar configuración del sistema', 'config', 'escritura'),

-- Permisos especiales
('sistema.super_admin', 'Super administrador', 'Acceso completo sin restricciones', 'sistema', 'privilegios'),
('dashboard.acceso', 'Acceso al dashboard', 'Acceso al panel principal', 'dashboard', 'acceso');

-- ============================================
-- 5. Permisos esenciales
-- ============================================
-- INSERT INTO permisos (id, codigo, nombre, descripcion, modulo, categoria)
-- VALUES
-- (1, 'dashboard.acceso', 'Acceso al dashboard', 'Acceso al panel principal', 'dashboard', 'acceso'),
-- (2, 'usuarios.ver', 'Ver usuarios', 'Ver lista de usuarios', 'usuarios', 'lectura'),
-- (3, 'usuarios.crear', 'Crear usuarios', 'Crear nuevos usuarios', 'usuarios', 'escritura'),
-- (4, 'roles.ver', 'Ver roles', 'Ver lista de roles', 'roles', 'lectura'),
-- (5, 'roles.crear', 'Crear roles', 'Crear nuevos roles', 'roles', 'escritura'),
-- (6, 'permisos.ver', 'Ver permisos', 'Ver lista de permisos', 'permisos', 'lectura'),
-- (7, 'menus.ver', 'Ver menús', 'Ver estructura de menús', 'menus', 'lectura')
-- ON CONFLICT (codigo) DO NOTHING;

-- ============================================
-- 6. Asignar permisos al rol Super Administrador
-- ============================================
INSERT INTO rol_permiso (rol_id, permiso_id, concedido)
SELECT 1, id, true
FROM permisos
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- ============================================
-- 7. Asignar menús al rol Super Administrador
-- ============================================
INSERT INTO rol_menu (rol_id, menu_id, activo, puede_ver, puede_editar, puede_eliminar, asignado_por)
SELECT 1, id, true, true, true, true, 1
FROM menus
ON CONFLICT (rol_id, menu_id) DO NOTHING;

-- ============================================
-- 8. Configuración del sistema
-- ============================================
-- INSERT INTO configuracion_sistema (clave, valor, tipo, categoria, descripcion)
-- VALUES
-- ('permisos.super_admin_rol_id', '1', 'number', 'permisos', 'ID del rol super administrador')
-- ON CONFLICT (clave) DO NOTHING;

-- Configuraciones del sistema
INSERT INTO configuracion_sistema (clave, valor, tipo, categoria, descripcion) VALUES
('seguridad.intentos_login_maximos', '5', 'number', 'seguridad', 'Intentos máximos de login antes de bloquear'),
('seguridad.bloqueo_temporal_minutos', '15', 'number', 'seguridad', 'Minutos de bloqueo tras intentos fallidos'),
('seguridad.duracion_sesion_minutos', '120', 'number', 'seguridad', 'Duración de sesión activa'),
('seguridad.requerir_verificacion_email', 'true', 'boolean', 'seguridad', 'Requerir verificación de email'),
('permisos.herencia_roles', 'true', 'boolean', 'permisos', 'Habilitar herencia entre roles'),
('permisos.super_admin_rol_id', '1', 'number', 'permisos', 'ID del rol super administrador'),
('ui.menu_max_profundidad', '3', 'number', 'interfaz', 'Máxima profundidad de menús anidados'),
('ui.idioma_default', 'es', 'string', 'interfaz', 'Idioma por defecto del sistema'),
('ui.timezone_default', 'America/Mexico_City', 'string', 'interfaz', 'Zona horaria por defecto'),
('notificaciones.habilitar_email', 'true', 'boolean', 'notificaciones', 'Habilitar notificaciones por email'),
('backup.automatico_dias', '7', 'number', 'backup', 'Frecuencia de backups automáticos en días');

-- -- ============================================
-- -- FUNCIONES ESENCIALES
-- -- ============================================

-- -- Función para obtener el ID del usuario actual
-- CREATE OR REPLACE FUNCTION current_user_id()
-- RETURNS INTEGER AS $$
-- BEGIN
--     -- En una aplicación real, esto vendría del contexto de la sesión
--     -- Por ahora devolvemos un valor por defecto (podría ser de una variable de sesión)
--     RETURN NULLIF(current_setting('app.current_user_id', TRUE), '')::INTEGER;
-- END;
-- $$ LANGUAGE plpgsql;

-- -- Función para verificar si usuario tiene permiso
-- CREATE OR REPLACE FUNCTION tiene_permiso(
--     p_usuario_id INTEGER,
--     p_permiso_codigo VARCHAR,
--     p_modulo VARCHAR DEFAULT NULL
-- ) RETURNS BOOLEAN AS $$
-- DECLARE
--     v_tiene_permiso BOOLEAN;
--     v_rol_super_admin INTEGER;
-- BEGIN
--     -- Obtener ID del rol super admin desde configuración
--     SELECT valor::INTEGER INTO v_rol_super_admin 
--     FROM configuracion_sistema 
--     WHERE clave = 'permisos.super_admin_rol_id';
    
--     -- Verificar si el usuario tiene rol super admin
--     IF EXISTS (
--         SELECT 1 FROM usuario_rol ur
--         WHERE ur.usuario_id = p_usuario_id 
--         AND ur.rol_id = v_rol_super_admin
--     ) THEN
--         RETURN TRUE;
--     END IF;
    
--     -- Verificar permiso directo del usuario
--     SELECT EXISTS (
--         SELECT 1 FROM usuario_permiso up
--         JOIN permisos p ON up.permiso_id = p.id
--         WHERE up.usuario_id = p_usuario_id
--         AND up.concedido = true
--         AND p.codigo = p_permiso_codigo
--         AND (p_modulo IS NULL OR p.modulo = p_modulo)
--     ) INTO v_tiene_permiso;
    
--     -- Si no tiene permiso directo, verificar a través de roles
--     IF NOT v_tiene_permiso THEN
--         SELECT EXISTS (
--             SELECT 1 FROM permisos p
--             JOIN rol_permiso rp ON p.id = rp.permiso_id AND rp.concedido = true
--             JOIN usuario_rol ur ON rp.rol_id = ur.rol_id
--             WHERE ur.usuario_id = p_usuario_id
--             AND p.codigo = p_permiso_codigo
--             AND (p_modulo IS NULL OR p.modulo = p_modulo)
--         ) INTO v_tiene_permiso;
--     END IF;
    
--     RETURN COALESCE(v_tiene_permiso, FALSE);
-- END;
-- $$ LANGUAGE plpgsql SECURITY DEFINER;

-- -- Función para obtener menús del usuario
-- CREATE OR REPLACE FUNCTION obtener_menus_usuario(p_usuario_id INTEGER)
-- RETURNS TABLE (
--     id INTEGER,
--     nombre VARCHAR,
--     ruta VARCHAR,
--     icono VARCHAR,
--     orden INTEGER,
--     parent_id INTEGER,
--     nivel INTEGER,
--     puede_ver BOOLEAN,
--     puede_editar BOOLEAN,
--     puede_eliminar BOOLEAN,
--     path_ruta TEXT,
--     path_nombre TEXT,
--     es_externo BOOLEAN,
--     abrir_en_nueva_ventana BOOLEAN,
--     badge_text VARCHAR,
--     badge_color VARCHAR
-- ) AS $$
-- BEGIN
--     RETURN QUERY
--     WITH RECURSIVE menu_recursivo AS (
--         -- Menús raíz desde roles
--         SELECT 
--             m.id,
--             m.nombre,
--             m.ruta,
--             m.icono,
--             m.orden,
--             m.parent_id,
--             1 as nivel,
--             COALESCE(MAX(rm.puede_ver), FALSE) as puede_ver,
--             COALESCE(MAX(rm.puede_editar), FALSE) as puede_editar,
--             COALESCE(MAX(rm.puede_eliminar), FALSE) as puede_eliminar,
--             ARRAY[m.ruta] as path_ruta_arr,
--             ARRAY[m.nombre] as path_nombre_arr,
--             m.es_externo,
--             m.abrir_en_nueva_ventana,
--             m.badge_text,
--             m.badge_color
--         FROM menus m
--         LEFT JOIN rol_menu rm ON m.id = rm.menu_id AND rm.activo = true
--         LEFT JOIN usuario_rol ur ON rm.rol_id = ur.rol_id AND ur.usuario_id = p_usuario_id
--         WHERE m.parent_id IS NULL 
--           AND m.visible = true
--           AND (m.requiere_permiso = false OR rm.puede_ver = true)
--         GROUP BY m.id, m.nombre, m.ruta, m.icono, m.orden, m.parent_id, 
--                  m.es_externo, m.abrir_en_nueva_ventana, m.badge_text, m.badge_color
        
--         UNION
        
--         -- Menús raíz directos del usuario
--         SELECT 
--             m.id,
--             m.nombre,
--             m.ruta,
--             m.icono,
--             m.orden,
--             m.parent_id,
--             1 as nivel,
--             COALESCE(MAX(um.puede_ver), FALSE) as puede_ver,
--             COALESCE(MAX(um.puede_editar), FALSE) as puede_editar,
--             COALESCE(MAX(um.puede_eliminar), FALSE) as puede_eliminar,
--             ARRAY[m.ruta] as path_ruta_arr,
--             ARRAY[m.nombre] as path_nombre_arr,
--             m.es_externo,
--             m.abrir_en_nueva_ventana,
--             m.badge_text,
--             m.badge_color
--         FROM menus m
--         JOIN usuario_menu um ON m.id = um.menu_id 
--             AND um.usuario_id = p_usuario_id 
--             AND um.activo = true 
--             AND um.habilitado = true
--         WHERE m.parent_id IS NULL 
--           AND m.visible = true
--           AND um.puede_ver = true
--         GROUP BY m.id, m.nombre, m.ruta, m.icono, m.orden, m.parent_id,
--                  m.es_externo, m.abrir_en_nueva_ventana, m.badge_text, m.badge_color
        
--         UNION ALL
        
--         -- Submenús recursivos
--         SELECT 
--             m.id,
--             m.nombre,
--             m.ruta,
--             m.icono,
--             m.orden,
--             m.parent_id,
--             mr.nivel + 1,
--             COALESCE(MAX(rm.puede_ver), FALSE) as puede_ver,
--             COALESCE(MAX(rm.puede_editar), FALSE) as puede_editar,
--             COALESCE(MAX(rm.puede_eliminar), FALSE) as puede_eliminar,
--             mr.path_ruta_arr || m.ruta,
--             mr.path_nombre_arr || m.nombre,
--             m.es_externo,
--             m.abrir_en_nueva_ventana,
--             m.badge_text,
--             m.badge_color
--         FROM menus m
--         JOIN menu_recursivo mr ON m.parent_id = mr.id
--         LEFT JOIN rol_menu rm ON m.id = rm.menu_id AND rm.activo = true
--         LEFT JOIN usuario_rol ur ON rm.rol_id = ur.rol_id AND ur.usuario_id = p_usuario_id
--         WHERE m.visible = true
--           AND (m.requiere_permiso = false OR rm.puede_ver = true)
--         GROUP BY m.id, m.nombre, m.ruta, m.icono, m.orden, m.parent_id, mr.nivel, 
--                  mr.path_ruta_arr, mr.path_nombre_arr, m.es_externo, 
--                  m.abrir_en_nueva_ventana, m.badge_text, m.badge_color
--     )
--     SELECT DISTINCT ON (mr.id)
--         mr.id,
--         mr.nombre,
--         mr.ruta,
--         mr.icono,
--         mr.orden,
--         mr.parent_id,
--         mr.nivel,
--         mr.puede_ver,
--         mr.puede_editar,
--         mr.puede_eliminar,
--         array_to_string(mr.path_ruta_arr, '/') as path_ruta,
--         array_to_string(mr.path_nombre_arr, ' > ') as path_nombre,
--         mr.es_externo,
--         mr.abrir_en_nueva_ventana,
--         mr.badge_text,
--         mr.badge_color
--     FROM menu_recursivo mr
--     WHERE mr.puede_ver = true
--     ORDER BY mr.id, mr.nivel;
-- END;
-- $$ LANGUAGE plpgsql SECURITY DEFINER;

-- -- Función para registrar actividad
-- CREATE OR REPLACE FUNCTION registrar_actividad(
--     p_usuario_id INTEGER,
--     p_tipo_actividad VARCHAR,
--     p_modulo VARCHAR,
--     p_descripcion TEXT,
--     p_detalles JSONB DEFAULT NULL,
--     p_ip_address INET DEFAULT NULL,
--     p_user_agent TEXT DEFAULT NULL
-- ) RETURNS VOID AS $$
-- BEGIN
--     INSERT INTO actividad_usuario (
--         usuario_id, tipo_actividad, modulo, descripcion, 
--         detalles, ip_address, user_agent
--     ) VALUES (
--         p_usuario_id, p_tipo_actividad, p_modulo, p_descripcion,
--         p_detalles, p_ip_address, p_user_agent
--     );
-- END;
-- $$ LANGUAGE plpgsql;

-- -- Función para actualizar timestamp automáticamente
-- CREATE OR REPLACE FUNCTION actualizar_timestamp()
-- RETURNS TRIGGER AS $$
-- BEGIN
--     NEW.updated_at = CURRENT_TIMESTAMP;
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;

-- -- Triggers para actualizar timestamps
-- CREATE TRIGGER trg_menus_updated_at BEFORE UPDATE ON menus
--     FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();

-- CREATE TRIGGER trg_config_updated_at BEFORE UPDATE ON configuracion_sistema
--     FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();

-- CREATE TRIGGER trg_reglas_updated_at BEFORE UPDATE ON reglas_permisos
--     FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();

-- -- ============================================
-- -- VISTAS PARA EL PANEL DE ADMINISTRACIÓN
-- -- ============================================

-- -- Vista: Resumen de permisos por usuario
-- CREATE OR REPLACE VIEW vista_resumen_usuarios AS
-- SELECT 
--     u.id,
--     u.username,
--     u.email,
--     u.nombre_completo,
--     u.activo,
--     u.ultimo_login,
--     STRING_AGG(DISTINCT r.nombre, ', ' ORDER BY r.nombre) as roles,
--     COUNT(DISTINCT r.id) as num_roles,
--     COUNT(DISTINCT CASE WHEN rm.activo = true THEN rm.menu_id END) as menus_acceso,
--     COUNT(DISTINCT CASE WHEN rp.concedido = true THEN rp.permiso_id END) as permisos_acceso,
--     MAX(a.fecha) as ultima_actividad,
--     BOOL_OR(s.activa) as tiene_sesion_activa
-- FROM usuarios u
-- LEFT JOIN usuario_rol ur ON u.id = ur.usuario_id
-- LEFT JOIN roles r ON ur.rol_id = r.id AND r.activo = true
-- LEFT JOIN rol_menu rm ON r.id = rm.rol_id AND rm.activo = true AND rm.puede_ver = true
-- LEFT JOIN rol_permiso rp ON r.id = rp.rol_id AND rp.concedido = true
-- LEFT JOIN actividad_usuario a ON u.id = a.usuario_id
-- LEFT JOIN sesiones_usuario s ON u.id = s.usuario_id AND s.activa = true
-- GROUP BY u.id, u.username, u.email, u.nombre_completo, u.activo, u.ultimo_login
-- ORDER BY u.fecha_creacion DESC;

-- -- Vista: Detalle de permisos por rol
-- CREATE OR REPLACE VIEW vista_permisos_rol AS
-- SELECT 
--     r.id as rol_id,
--     r.nombre as rol,
--     r.descripcion as rol_descripcion,
--     p.codigo as permiso,
--     p.nombre as permiso_nombre,
--     p.modulo,
--     p.categoria,
--     rp.concedido,
--     rp.restricciones,
--     COUNT(DISTINCT ur.usuario_id) as usuarios_asignados
-- FROM roles r
-- LEFT JOIN rol_permiso rp ON r.id = rp.rol_id
-- LEFT JOIN permisos p ON rp.permiso_id = p.id
-- LEFT JOIN usuario_rol ur ON r.id = ur.rol_id
-- GROUP BY r.id, r.nombre, r.descripcion, p.codigo, p.nombre, p.modulo, p.categoria, rp.concedido, rp.restricciones
-- ORDER BY r.nivel_prioridad DESC, p.modulo, p.categoria, p.codigo;

-- -- Vista: Uso de menús
-- CREATE OR REPLACE VIEW vista_uso_menus AS
-- SELECT 
--     m.id,
--     m.nombre,
--     m.ruta,
--     COUNT(DISTINCT ur.usuario_id) as total_usuarios_acceso,
--     COUNT(DISTINCT CASE WHEN u.activo = true THEN ur.usuario_id END) as usuarios_activos_acceso,
--     COUNT(DISTINCT a.id) as accesos_registrados,
--     MAX(a.fecha) as ultimo_acceso,
--     STRING_AGG(DISTINCT r.nombre, ', ' ORDER BY r.nombre) as roles_con_acceso
-- FROM menus m
-- LEFT JOIN rol_menu rm ON m.id = rm.menu_id AND rm.activo = true AND rm.puede_ver = true
-- LEFT JOIN usuario_rol ur ON rm.rol_id = ur.rol_id
-- LEFT JOIN roles r ON ur.rol_id = r.id
-- LEFT JOIN usuarios u ON ur.usuario_id = u.id
-- LEFT JOIN actividad_usuario a ON ur.usuario_id = a.usuario_id 
--     AND a.tipo_actividad = 'VIEW_MENU' 
--     AND a.detalles->>'menu_id' = m.id::text
-- WHERE m.visible = true
-- GROUP BY m.id, m.nombre, m.ruta
-- ORDER BY m.orden;

-- -- Vista: Auditoría reciente
-- CREATE OR REPLACE VIEW vista_auditoria_reciente AS
-- SELECT 
--     a.id,
--     a.tabla_afectada,
--     a.accion,
--     a.valores_anteriores,
--     a.valores_nuevos,
--     u.username as usuario,
--     a.ip_address,
--     a.fecha,
--     CASE 
--         WHEN a.accion = 'INSERT' THEN 'Creación'
--         WHEN a.accion = 'UPDATE' THEN 'Actualización'
--         WHEN a.accion = 'DELETE' THEN 'Eliminación'
--         ELSE a.accion
--     END as accion_legible
-- FROM auditoria_permisos a
-- LEFT JOIN usuarios u ON a.usuario_id = u.id
-- ORDER BY a.fecha DESC
-- LIMIT 1000;

-- -- Vista: Sesiones activas
-- CREATE OR REPLACE VIEW vista_sesiones_activas AS
-- SELECT 
--     s.id as sesion_id,
--     u.username,
--     u.email,
--     u.nombre_completo,
--     s.dispositivo,
--     s.navegador,
--     s.sistema_operativo,
--     s.ip_address,
--     s.ciudad,
--     s.pais,
--     s.fecha_inicio,
--     s.fecha_ultima_actividad,
--     s.fecha_expiracion,
--     EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - s.fecha_ultima_actividad))/60 as minutos_inactivo
-- FROM sesiones_usuario s
-- JOIN usuarios u ON s.usuario_id = u.id
-- WHERE s.activa = true
-- ORDER BY s.fecha_ultima_actividad DESC;

-- -- ============================================
-- -- PROCEDIMIENTOS DE MANTENIMIENTO
-- -- ============================================

-- -- Procedimiento: Limpiar sesiones expiradas
-- CREATE OR REPLACE PROCEDURE limpiar_sesiones_expiradas()
-- LANGUAGE plpgsql
-- AS $$
-- DECLARE
--     v_duracion_sesion INTEGER;
--     v_sesiones_limpiadas INTEGER;
-- BEGIN
--     -- Obtener duración de configuración
--     SELECT valor::INTEGER INTO v_duracion_sesion
--     FROM configuracion_sistema 
--     WHERE clave = 'seguridad.duracion_sesion_minutos';
    
--     -- Inactivar sesiones expiradas
--     WITH sesiones_desactivadas AS (
--         UPDATE sesiones_usuario 
--         SET activa = false, 
--             fecha_ultima_actividad = CURRENT_TIMESTAMP
--         WHERE activa = true 
--           AND fecha_ultima_actividad < CURRENT_TIMESTAMP - 
--               (COALESCE(v_duracion_sesion, 120) || ' minutes')::INTERVAL
--         RETURNING id
--     )
--     SELECT COUNT(*) INTO v_sesiones_limpiadas FROM sesiones_desactivadas;
    
--     -- Registrar actividad del sistema
--     INSERT INTO actividad_usuario (usuario_id, tipo_actividad, modulo, descripcion, detalles)
--     VALUES (NULL, 'SISTEMA', 'mantenimiento', 'Limpieza automática de sesiones expiradas',
--             jsonb_build_object('sesiones_limpiadas', v_sesiones_limpiadas));
    
--     RAISE NOTICE 'Sesiones expiradas limpiadas: %', v_sesiones_limpiadas;
-- END;
-- $$;

-- -- Procedimiento: Crear backup de permisos
-- CREATE OR REPLACE PROCEDURE crear_backup_permisos(
--     p_motivo VARCHAR DEFAULT 'Backup programado',
--     p_usuario_id INTEGER DEFAULT NULL
-- )
-- LANGUAGE plpgsql
-- AS $$
-- DECLARE
--     v_datos JSONB;
--     v_registros_backup INTEGER;
-- BEGIN
--     -- Recolectar todos los datos
--     SELECT jsonb_build_object(
--         'fecha', CURRENT_TIMESTAMP,
--         'usuarios', (SELECT jsonb_agg(row_to_json(u)) FROM usuarios u),
--         'roles', (SELECT jsonb_agg(row_to_json(r)) FROM roles r),
--         'menus', (SELECT jsonb_agg(row_to_json(m)) FROM menus m),
--         'permisos', (SELECT jsonb_agg(row_to_json(p)) FROM permisos p),
--         'usuario_rol', (SELECT jsonb_agg(row_to_json(ur)) FROM usuario_rol ur),
--         'rol_permiso', (SELECT jsonb_agg(row_to_json(rp)) FROM rol_permiso rp),
--         'rol_menu', (SELECT jsonb_agg(row_to_json(rm)) FROM rol_menu rm),
--         'usuario_menu', (SELECT jsonb_agg(row_to_json(um)) FROM usuario_menu um),
--         'usuario_permiso', (SELECT jsonb_agg(row_to_json(up)) FROM usuario_permiso up),
--         'configuraciones', (SELECT jsonb_agg(row_to_json(c)) FROM configuracion_sistema c)
--     ) INTO v_datos;
    
--     -- Contar registros para el log
--     SELECT 
--         (SELECT COUNT(*) FROM usuarios) +
--         (SELECT COUNT(*) FROM roles) +
--         (SELECT COUNT(*) FROM menus) +
--         (SELECT COUNT(*) FROM permisos) +
--         (SELECT COUNT(*) FROM usuario_rol) +
--         (SELECT COUNT(*) FROM rol_permiso) +
--         (SELECT COUNT(*) FROM rol_menu) +
--         (SELECT COUNT(*) FROM usuario_menu) +
--         (SELECT COUNT(*) FROM usuario_permiso) +
--         (SELECT COUNT(*) FROM configuracion_sistema)
--     INTO v_registros_backup;
    
--     -- Insertar backup
--     INSERT INTO backup_permisos (motivo, realizado_por, datos, version_schema)
--     VALUES (p_motivo, p_usuario_id, v_datos, '1.0');
    
--     -- Registrar en auditoría
--     INSERT INTO auditoria_permisos (tabla_afectada, accion, valores_nuevos, usuario_id)
--     VALUES ('backup_permisos', 'CREATE', 
--             jsonb_build_object(
--                 'motivo', p_motivo,
--                 'registros', v_registros_backup,
--                 'backup_id', currval('backup_permisos_id_seq'::regclass)
--             ),
--             p_usuario_id);
    
--     RAISE NOTICE 'Backup creado con % registros', v_registros_backup;
-- END;
-- $$;

-- -- ============================================
-- -- REGLAS DE NEGOCIO BÁSICAS
-- -- ============================================

-- INSERT INTO reglas_permisos (nombre, descripcion, condicion_sql, mensaje_error, prioridad) VALUES
-- ('no_auto_eliminacion_roles', 'Evitar que usuarios eliminen sus propios roles asignados',
--  'NOT EXISTS (SELECT 1 FROM usuario_rol WHERE usuario_id = current_user_id() AND rol_id = OLD.id) OR tiene_permiso(current_user_id(), ''sistema.super_admin'')',
--  'No puedes eliminar un rol que tienes asignado', 100),

-- ('no_auto_desactivacion', 'Evitar que usuarios se desactiven a sí mismos',
--  'NEW.id != current_user_id() OR tiene_permiso(current_user_id(), ''sistema.super_admin'')',
--  'No puedes desactivar tu propia cuenta', 90),

-- ('max_profundidad_menus', 'Limitar profundidad de menús anidados',
--  '(SELECT nivel FROM (WITH RECURSIVE cte AS (SELECT id, parent_id, 1 as nivel FROM menus WHERE id = NEW.id UNION ALL SELECT m.id, m.parent_id, cte.nivel + 1 FROM menus m JOIN cte ON m.id = cte.parent_id) SELECT MAX(nivel) as nivel FROM cte) as niveles) <= (SELECT valor::INTEGER FROM configuracion_sistema WHERE clave = ''ui.menu_max_profundidad'')',
--  'Profundidad máxima de menús excedida', 50);

-- -- ============================================
-- -- EJEMPLO DE USO BÁSICO
-- -- ============================================

-- /*
-- -- 1. Crear un usuario administrador
-- INSERT INTO usuarios (username, email, password_hash, nombre_completo) 
-- VALUES ('admin', 'admin@sistema.com', 'hash_seguro', 'Administrador Principal');

-- -- 2. Asignar rol de super administrador
-- INSERT INTO usuario_rol (usuario_id, rol_id) 
-- VALUES (1, 1);

-- -- 3. Verificar permisos
-- SELECT tiene_permiso(1, 'usuarios.ver'); -- Devuelve true

-- -- 4. Obtener menús del usuario
-- SELECT * FROM obtener_menus_usuario(1);

-- -- 5. Registrar actividad
-- SELECT registrar_actividad(1, 'LOGIN', 'autenticacion', 'Inicio de sesión exitoso');

-- -- 6. Ver resumen de usuarios
-- SELECT * FROM vista_resumen_usuarios;

-- -- 7. Crear backup
-- CALL crear_backup_permisos('Backup inicial', 1);

-- -- 8. Limpiar sesiones expiradas
-- CALL limpiar_sesiones_expiradas();
-- */
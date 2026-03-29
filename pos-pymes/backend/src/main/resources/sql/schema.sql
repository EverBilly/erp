-- ============================================
-- POS-PyMEs ERP - Complete Schema
-- Last updated: 2026-03-26
-- ============================================
-- This file defines the ENTIRE database structure.
-- It runs ONCE when creating the database.
-- For future changes, create files in migrations/
-- ============================================

-- ============================================
-- MAIN TABLES
-- ============================================

-- 1. TENANTS (companies/organizations)
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    plan VARCHAR(50) DEFAULT 'free',
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    config JSONB DEFAULT '{}',
    logo_url VARCHAR(255)
);

-- 2. USERS
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    full_name VARCHAR(150),
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    timezone VARCHAR(255) DEFAULT 'UTC',
    locale VARCHAR(255) DEFAULT 'es',
    metadata JSONB DEFAULT '{}'
);

-- 3. ROLES
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    priority_level INTEGER DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- 4. MENUS
CREATE TABLE IF NOT EXISTS menus (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(255),
    icon VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    parent_id BIGINT REFERENCES menus(id) ON DELETE CASCADE,
    visible BOOLEAN DEFAULT true,
    requires_permission BOOLEAN DEFAULT true,
    component VARCHAR(100),
    description VARCHAR(255),
    params JSONB DEFAULT '{}',
    is_external BOOLEAN DEFAULT false,
    open_in_new_tab BOOLEAN DEFAULT false,
    badge_text VARCHAR(20),
    badge_color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. PERMISSIONS
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    module VARCHAR(50),
    category VARCHAR(50),
    security_level INTEGER DEFAULT 1,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. USER SESSIONS
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token VARCHAR(255) NOT NULL,
    refresh_token VARCHAR(255) NOT NULL,
    device VARCHAR(100),
    browser VARCHAR(100),
    operating_system VARCHAR(50),
    ip_address VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    city VARCHAR(100),
    country VARCHAR(100),
    active BOOLEAN DEFAULT true,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    failed_attempts INTEGER DEFAULT 0
);

-- ============================================
-- JOIN TABLES (many-to-many relationships)
-- ============================================

-- 7. USER <-> ROLE
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 8. ROLE <-> PERMISSION
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 9. ROLE <-> MENU (with granular permissions)
CREATE TABLE IF NOT EXISTS role_menus (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    menu_id BIGINT REFERENCES menus(id) ON DELETE CASCADE,
    active BOOLEAN DEFAULT true,
    can_view BOOLEAN DEFAULT true,
    can_edit BOOLEAN DEFAULT false,
    can_delete BOOLEAN DEFAULT false,
    PRIMARY KEY (role_id, menu_id)
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_tenant ON users(tenant_id);

CREATE INDEX IF NOT EXISTS idx_roles_active ON roles(active);
CREATE INDEX IF NOT EXISTS idx_roles_priority ON roles(priority_level);

CREATE INDEX IF NOT EXISTS idx_menus_parent ON menus(parent_id);
CREATE INDEX IF NOT EXISTS idx_menus_sort_order ON menus(sort_order);
CREATE INDEX IF NOT EXISTS idx_menus_visible ON menus(visible);
CREATE INDEX IF NOT EXISTS idx_menus_tenant ON menus(tenant_id);

CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code);
CREATE INDEX IF NOT EXISTS idx_permissions_module ON permissions(module);
CREATE INDEX IF NOT EXISTS idx_permissions_active ON permissions(active);

CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

CREATE INDEX IF NOT EXISTS idx_role_menus_role ON role_menus(role_id);
CREATE INDEX IF NOT EXISTS idx_role_menus_menu ON role_menus(menu_id);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_active ON user_sessions(active);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON user_sessions(access_token);

-- ============================================
-- SEED DATA
-- ============================================

-- Default tenant
INSERT INTO tenants (name, identifier, plan)
VALUES ('Sistema Principal', 'tenant-default', 'enterprise')
ON CONFLICT (identifier) DO NOTHING;

-- Initial users (password: password123)
INSERT INTO users (username, email, password_hash, active, full_name, phone, locale, metadata, tenant_id)
VALUES
('superadmin', 'superadmin@sistema.com', '$2a$10$YeyMj3Ki4cVOcfuE3MIaDu98qZqrG/TJ4hNGrcgqliE/DqGMgO0fm', true, 'Super Administrador Principal', '+525512345678', 'es', '{"notificaciones": true, "tema": "oscuro"}', 1),
('admin', 'admin1@sistema.com', '$2a$10$YeyMj3Ki4cVOcfuE3MIaDu98qZqrG/TJ4hNGrcgqliE/DqGMgO0fm', true, 'Ana Lopez Rodriguez', '+525511112222', 'es', '{"notificaciones": true, "tema": "claro"}', 1),
('empleado1', 'empleado1@empresa.com', '$2a$10$YeyMj3Ki4cVOcfuE3MIaDu98qZqrG/TJ4hNGrcgqliE/DqGMgO0fm', true, 'Pedro Hernandez Luna', '+525577778888', 'es', '{"departamento": "ventas"}', 1)
ON CONFLICT (username) DO NOTHING;

-- System roles
INSERT INTO roles (name, description, priority_level, is_system, created_at)
VALUES
('SUPER_ADMIN', 'Full system access', 1000, true, CURRENT_TIMESTAMP),
('ADMIN', 'User and permission management', 500, true, CURRENT_TIMESTAMP),
('USER', 'Basic system user', 10, true, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
VALUES
(1, 1),  -- superadmin -> SUPER_ADMIN
(2, 2),  -- admin -> ADMIN
(3, 3)   -- empleado1 -> USER
ON CONFLICT (user_id, role_id) DO NOTHING;

-- System menus
INSERT INTO menus (name, path, icon, sort_order, parent_id, description, tenant_id)
VALUES
('Dashboard', '/dashboard', 'home', 1, NULL, 'Main dashboard', 1),
('Administration', '/admin', 'settings', 100, NULL, 'Administration module', 1),
('Users', '/usuarios', 'users', 1, 2, 'User management', 1),
('Roles', '/admin/roles', 'security', 2, 2, 'Role management', 1),
('My Profile', '/perfil', 'person', 2, NULL, 'User profile', 1)
ON CONFLICT DO NOTHING;

-- SUPER_ADMIN sees all menus
INSERT INTO role_menus (role_id, menu_id, active, can_view, can_edit, can_delete)
SELECT 1, id, true, true, true, true
FROM menus
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- ADMIN sees Administration and children
INSERT INTO role_menus (role_id, menu_id, active, can_view)
VALUES
(2, 1, true, true),  -- Dashboard
(2, 2, true, true),  -- Administration
(2, 3, true, true),  -- Users
(2, 5, true, true)   -- My Profile
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- USER sees Dashboard and Profile
INSERT INTO role_menus (role_id, menu_id, active, can_view)
VALUES
(3, 1, true, true),  -- Dashboard
(3, 5, true, true)   -- My Profile
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- System permissions
INSERT INTO permissions (code, name, description, module, category)
VALUES
('users.view', 'View users', 'View user list', 'users', 'read'),
('users.create', 'Create users', 'Create new users', 'users', 'write'),
('users.edit', 'Edit users', 'Edit existing users', 'users', 'write'),
('users.delete', 'Delete users', 'Delete users', 'users', 'write'),
('roles.view', 'View roles', 'View role list', 'roles', 'read'),
('roles.create', 'Create roles', 'Create new roles', 'roles', 'write'),
('roles.edit', 'Edit roles', 'Edit existing roles', 'roles', 'write'),
('menus.view', 'View menus', 'View menu structure', 'menus', 'read'),
('menus.edit', 'Edit menus', 'Edit existing menus', 'menus', 'write'),
('dashboard.access', 'Dashboard access', 'Access main dashboard', 'dashboard', 'access')
ON CONFLICT (code) DO NOTHING;

-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ADMIN gets user and dashboard permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE module IN ('users', 'dashboard')
ON CONFLICT (role_id, permission_id) DO NOTHING;

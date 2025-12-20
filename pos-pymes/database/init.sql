-- Tabla de productos
CREATE TABLE IF NOT EXISTS productos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    categoria VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de clientes - AÑADIR UNIQUE A EMAIL
CREATE TABLE IF NOT EXISTS clientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    email VARCHAR(100) UNIQUE,  -- AÑADIDO: UNIQUE
    telefono VARCHAR(20),
    direccion TEXT,
    rfc VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de ventas
CREATE TABLE IF NOT EXISTS ventas (
    id SERIAL PRIMARY KEY,
    folio VARCHAR(50) UNIQUE NOT NULL,
    cliente_id INTEGER REFERENCES clientes(id),
    total DECIMAL(10,2) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'COMPLETADA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de detalles de venta
CREATE TABLE IF NOT EXISTS venta_detalles (
    id SERIAL PRIMARY KEY,
    venta_id INTEGER REFERENCES ventas(id),
    producto_id INTEGER REFERENCES productos(id),
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL
);

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultimo_login TIMESTAMP
);

-- Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT
);

-- Tabla de permisos
CREATE TABLE IF NOT EXISTS permisos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
);

-- Tabla intermedia: usuarios_roles
CREATE TABLE IF NOT EXISTS usuarios_roles (
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

-- Tabla intermedia: roles_permisos
CREATE TABLE IF NOT EXISTS roles_permisos (
    rol_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    permiso_id INTEGER REFERENCES permisos(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);

-- Insertar roles básicos - QUITAR ON CONFLICT o usar otra estrategia
INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Administrador del sistema'),
('CAJERO', 'Cajero - puede realizar ventas'),
('INVENTARIO', 'Gestor de inventario'),
('REPORTES', 'Solo visualización de reportes');

-- Insertar permisos comunes
INSERT INTO permisos (nombre, descripcion) VALUES
('VENTA_CREAR', 'Crear nuevas ventas'),
('VENTA_ANULAR', 'Anular ventas'),
('PRODUCTO_CREAR', 'Crear productos'),
('PRODUCTO_EDITAR', 'Editar productos'),
('PRODUCTO_ELIMINAR', 'Eliminar productos'),
('INVENTARIO_VER', 'Ver inventario'),
('INVENTARIO_AJUSTAR', 'Ajustar inventario'),
('REPORTES_VER', 'Ver reportes'),
('USUARIOS_MANEJAR', 'Gestionar usuarios'),
('CONFIGURACION', 'Configurar sistema');

-- Asignar permisos a roles
INSERT INTO roles_permisos (rol_id, permiso_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 1), (2, 6),
(3, 3), (3, 4), (3, 6), (3, 7),
(4, 6), (4, 8);

-- Crear usuario administrador por defecto
INSERT INTO usuarios (username, password, email, nombre, apellido) VALUES
('admin', '$2a$10$YourHashedPasswordHere', 'admin@pos.com', 'Administrador', 'Sistema');

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (1, 1);

-- Insertar datos de ejemplo - SIN ON CONFLICT o con columnas UNIQUE
INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria) VALUES
('PROD001', 'Coca-Cola 600ml', 'Refresco de cola', 18.50, 100, 'Bebidas'),
('PROD002', 'Sabritas Saladas', 'Papas fritas', 15.00, 50, 'Botana'),
('PROD003', 'Galletas Oreo', 'Galletas de chocolate', 12.50, 80, 'Dulces'),
('PROD004', 'Leche Lala 1L', 'Leche entera', 25.00, 30, 'Lácteos'),
('PROD005', 'Pan Bimbo', 'Pan de caja', 35.00, 40, 'Panadería');

-- INSERT de clientes SIN ON CONFLICT (o agregar UNIQUE a email primero)
INSERT INTO clientes (nombre, email, telefono, direccion, rfc) VALUES
('Cliente General', 'general@email.com', '555-1234', 'Dirección general', 'XAXX010101000'),
('Juan Pérez', 'juan@email.com', '555-5678', 'Calle Falsa 123', 'PEJU010101ABC'),
('María García', 'maria@email.com', '555-9012', 'Avenida Real 456', 'GAMA010101DEF');

-- Crear índices
CREATE INDEX idx_productos_codigo ON productos(codigo);
CREATE INDEX idx_ventas_folio ON ventas(folio);
CREATE INDEX idx_ventas_fecha ON ventas(fecha);
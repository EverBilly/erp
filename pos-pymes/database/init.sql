CREATE DATABASE pos_db;

\c pos_db;

-- Tabla de productos
CREATE TABLE productos (
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

-- Tabla de clientes
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion TEXT,
    rfc VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de ventas
CREATE TABLE ventas (
    id SERIAL PRIMARY KEY,
    folio VARCHAR(50) UNIQUE NOT NULL,
    cliente_id INTEGER REFERENCES clientes(id),
    total DECIMAL(10,2) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'COMPLETADA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de detalles de venta
CREATE TABLE venta_detalles (
    id SERIAL PRIMARY KEY,
    venta_id INTEGER REFERENCES ventas(id),
    producto_id INTEGER REFERENCES productos(id),
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL
);

-- Insertar datos de ejemplo
INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria) VALUES
('PROD001', 'Coca-Cola 600ml', 'Refresco de cola', 18.50, 100, 'Bebidas'),
('PROD002', 'Sabritas Saladas', 'Papas fritas', 15.00, 50, 'Botana'),
('PROD003', 'Galletas Oreo', 'Galletas de chocolate', 12.50, 80, 'Dulces'),
('PROD004', 'Leche Lala 1L', 'Leche entera', 25.00, 30, 'Lácteos'),
('PROD005', 'Pan Bimbo', 'Pan de caja', 35.00, 40, 'Panadería');

INSERT INTO clientes (nombre, email, telefono, direccion, rfc) VALUES
('Cliente General', 'general@email.com', '555-1234', 'Dirección general', 'XAXX010101000'),
('Juan Pérez', 'juan@email.com', '555-5678', 'Calle Falsa 123', 'PEJU010101ABC'),
('María García', 'maria@email.com', '555-9012', 'Avenida Real 456', 'GAMA010101DEF');

-- Crear índices para mejor performance
CREATE INDEX idx_productos_codigo ON productos(codigo);
CREATE INDEX idx_ventas_folio ON ventas(folio);
CREATE INDEX idx_ventas_fecha ON ventas(fecha);
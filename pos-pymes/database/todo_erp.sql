-- ============================================
-- MÓDULO BASE - CONFIGURACIÓN DEL SISTEMA
-- ============================================

-- Tabla de Módulos del Sistema
CREATE TABLE modulos_sistema (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    version VARCHAR(20) DEFAULT '1.0.0',
    activo BOOLEAN DEFAULT false,
    es_core BOOLEAN DEFAULT false,
    orden_instalacion INTEGER DEFAULT 0,
    dependencias JSONB, -- Módulos requeridos
    configuraciones JSONB, -- Configuración por defecto
    icono VARCHAR(50),
    ruta_base VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar módulos base
INSERT INTO modulos_sistema (codigo, nombre, descripcion, activo, es_core, orden_instalacion) VALUES
('base', 'Sistema Base', 'Funcionalidades base del sistema', true, true, 1),
('seguridad', 'Seguridad y Permisos', 'Gestión de usuarios, roles y permisos', true, true, 2),
('pos', 'Punto de Venta', 'Sistema de punto de venta', true, false, 3),
('inventario', 'Inventario', 'Gestión de inventario', true, false, 4),
('compras', 'Compras', 'Módulo de compras', false, false, 5),
('ventas', 'Ventas', 'Módulo de ventas', false, false, 6),
('clientes', 'Clientes', 'Gestión de clientes', true, false, 7),
('proveedores', 'Proveedores', 'Gestión de proveedores', false, false, 8),
('reportes', 'Reportes', 'Sistema de reportes', false, false, 9),
('contabilidad', 'Contabilidad', 'Módulo contable', false, false, 10);

-- ============================================
-- MÓDULO POS - PUNTO DE VENTA
-- ============================================

-- 1. SUCURSALES / LOCALES
CREATE TABLE sucursales (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(100),
    ciudad VARCHAR(100),
    pais VARCHAR(100) DEFAULT 'México',
    moneda_base VARCHAR(3) DEFAULT 'MXN',
    timezone VARCHAR(50) DEFAULT 'America/Mexico_City',
    activa BOOLEAN DEFAULT true,
    es_principal BOOLEAN DEFAULT false,
    configuracion_pos JSONB, -- Configuración específica del POS
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id),
    updated_by INTEGER REFERENCES usuarios(id)
);

-- 2. ALMACENES / BODEGAS
CREATE TABLE almacenes (
    id SERIAL PRIMARY KEY,
    sucursal_id INTEGER REFERENCES sucursales(id),
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) DEFAULT 'principal', -- principal, temporal, devoluciones, etc.
    direccion TEXT,
    capacidad_maxima INTEGER,
    unidad_capacidad VARCHAR(20) DEFAULT 'unidades',
    activo BOOLEAN DEFAULT true,
    es_default BOOLEAN DEFAULT false,
    responsable_id INTEGER REFERENCES usuarios(id),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sucursal_id, codigo)
);

-- 3. CATEGORÍAS DE PRODUCTOS
CREATE TABLE categorias_productos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    parent_id INTEGER REFERENCES categorias_productos(id) ON DELETE CASCADE,
    nivel INTEGER DEFAULT 1,
    path_categoria TEXT, -- Ruta jerárquica
    imagen_url TEXT,
    activa BOOLEAN DEFAULT true,
    impuesto_id INTEGER, -- FK a tabla de impuestos (se crea después)
    orden INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);

-- 4. PRODUCTOS
CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    codigo_barras VARCHAR(100) UNIQUE,
    codigo_interno VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    descripcion_corta VARCHAR(500),
    categoria_id INTEGER REFERENCES categorias_productos(id),
    tipo_producto VARCHAR(50) DEFAULT 'producto', -- producto, servicio, combo, digital
    unidad_medida VARCHAR(50) DEFAULT 'unidad',
    peso DECIMAL(10,3),
    dimensiones JSONB, -- {ancho, alto, profundidad, unidad}
    imagen_principal TEXT,
    imagenes JSONB, -- Array de URLs
    activo BOOLEAN DEFAULT true,
    vendible BOOLEAN DEFAULT true,
    comprable BOOLEAN DEFAULT true,
    inventariable BOOLEAN DEFAULT true,
    es_serializado BOOLEAN DEFAULT false,
    es_lote BOOLEAN DEFAULT false,
    dias_garantia INTEGER,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id),
    updated_by INTEGER REFERENCES usuarios(id)
);

-- 5. VARIANTES DE PRODUCTOS (Tallas, Colores, etc.)
CREATE TABLE variantes_producto (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES productos(id) ON DELETE CASCADE,
    sku VARCHAR(100) UNIQUE NOT NULL,
    codigo_barras VARCHAR(100),
    nombre VARCHAR(200),
    atributos JSONB NOT NULL, -- {talla: 'M', color: 'Rojo'}
    precio_base DECIMAL(15,2) NOT NULL DEFAULT 0,
    costo_promedio DECIMAL(15,2) DEFAULT 0,
    costo_ultimo DECIMAL(15,2) DEFAULT 0,
    existencia_minima INTEGER DEFAULT 0,
    existencia_maxima INTEGER DEFAULT 0,
    activa BOOLEAN DEFAULT true,
    imagen_url TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. PRECIOS DE PRODUCTOS
CREATE TABLE precios_producto (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES productos(id) ON DELETE CASCADE,
    variante_id INTEGER REFERENCES variantes_producto(id) ON DELETE CASCADE,
    tipo_precio VARCHAR(50) NOT NULL, -- venta, mayoreo, oferta, especial
    nombre VARCHAR(100) NOT NULL, -- Precio público, Precio mayoreo, etc.
    precio DECIMAL(15,2) NOT NULL,
    moneda VARCHAR(3) DEFAULT 'MXN',
    activo BOOLEAN DEFAULT true,
    es_default BOOLEAN DEFAULT false,
    condiciones JSONB, -- {min_cantidad: 10, valido_hasta: '2024-12-31'}
    aplica_desde TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    aplica_hasta TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id),
    UNIQUE(producto_id, variante_id, tipo_precio)
);

-- 7. INVENTARIO POR ALMACÉN
CREATE TABLE inventario (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES productos(id) ON DELETE CASCADE,
    variante_id INTEGER REFERENCES variantes_producto(id) ON DELETE CASCADE,
    almacen_id INTEGER REFERENCES almacenes(id) ON DELETE CASCADE,
    existencia INTEGER NOT NULL DEFAULT 0,
    existencia_disponible INTEGER NOT NULL DEFAULT 0,
    existencia_reservada INTEGER NOT NULL DEFAULT 0,
    ubicacion VARCHAR(100), -- Estante, rack, etc.
    stock_minimo INTEGER DEFAULT 0,
    stock_maximo INTEGER DEFAULT 0,
    ultimo_conteo TIMESTAMP,
    costo_promedio DECIMAL(15,2) DEFAULT 0,
    valor_inventario DECIMAL(15,2) GENERATED ALWAYS AS (existencia * costo_promedio) STORED,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(producto_id, variante_id, almacen_id)
);

-- 8. MOVIMIENTOS DE INVENTARIO
CREATE TABLE movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    tipo_movimiento VARCHAR(50) NOT NULL, -- entrada, salida, ajuste, transferencia
    producto_id INTEGER REFERENCES productos(id),
    variante_id INTEGER REFERENCES variantes_producto(id),
    almacen_origen_id INTEGER REFERENCES almacenes(id),
    almacen_destino_id INTEGER REFERENCES almacenes(id),
    cantidad INTEGER NOT NULL,
    existencia_anterior INTEGER,
    existencia_nueva INTEGER,
    costo_unitario DECIMAL(15,2),
    costo_total DECIMAL(15,2),
    referencia_tipo VARCHAR(50), -- compra, venta, ajuste_inventario
    referencia_id BIGINT, -- ID de la transacción origen
    concepto TEXT,
    lote_numero VARCHAR(100),
    fecha_caducidad DATE,
    seriales TEXT[], -- Array de números de serie
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id),
    metadata JSONB
);

-- 9. CLIENTES
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    tipo_cliente VARCHAR(20) DEFAULT 'persona', -- persona, empresa
    codigo_cliente VARCHAR(50) UNIQUE NOT NULL,
    rfc VARCHAR(20),
    nombre VARCHAR(200) NOT NULL,
    apellido_paterno VARCHAR(100),
    apellido_materno VARCHAR(100),
    razon_social VARCHAR(200),
    nombre_comercial VARCHAR(200),
    email VARCHAR(100),
    telefono VARCHAR(20),
    celular VARCHAR(20),
    direccion TEXT,
    colonia VARCHAR(100),
    ciudad VARCHAR(100),
    estado VARCHAR(100),
    pais VARCHAR(100) DEFAULT 'México',
    codigo_postal VARCHAR(10),
    limite_credito DECIMAL(15,2) DEFAULT 0,
    dias_credito INTEGER DEFAULT 0,
    saldo_actual DECIMAL(15,2) DEFAULT 0,
    categoria_cliente VARCHAR(50) DEFAULT 'normal',
    activo BOOLEAN DEFAULT true,
    fecha_registro DATE DEFAULT CURRENT_DATE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);

-- 10. MÉTODOS DE PAGO
CREATE TABLE metodos_pago (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- efectivo, tarjeta, transferencia, etc.
    acepta_cambio BOOLEAN DEFAULT true,
    requiere_referencia BOOLEAN DEFAULT false,
    comision_porcentaje DECIMAL(5,2) DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    orden INTEGER DEFAULT 0,
    configuracion JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar métodos de pago básicos
INSERT INTO metodos_pago (codigo, nombre, tipo, orden) VALUES
('EFECTIVO', 'Efectivo', 'efectivo', 1),
('TARJETA_DEBITO', 'Tarjeta de Débito', 'tarjeta', 2),
('TARJETA_CREDITO', 'Tarjeta de Crédito', 'tarjeta', 3),
('TRANSFERENCIA', 'Transferencia', 'transferencia', 4),
('CHEQUE', 'Cheque', 'cheque', 5);

-- 11. TRANSACCIONES DE VENTA (TICKETS)
CREATE TABLE ventas (
    id BIGSERIAL PRIMARY KEY,
    sucursal_id INTEGER REFERENCES sucursales(id),
    almacen_id INTEGER REFERENCES almacenes(id),
    folio VARCHAR(50) UNIQUE NOT NULL,
    tipo_venta VARCHAR(50) DEFAULT 'contado', -- contado, credito
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cliente_id INTEGER REFERENCES clientes(id),
    subtotal DECIMAL(15,2) NOT NULL DEFAULT 0,
    descuento_total DECIMAL(15,2) DEFAULT 0,
    impuestos_total DECIMAL(15,2) DEFAULT 0,
    total DECIMAL(15,2) NOT NULL DEFAULT 0,
    pago_recibido DECIMAL(15,2) DEFAULT 0,
    cambio DECIMAL(15,2) DEFAULT 0,
    estado VARCHAR(50) DEFAULT 'pendiente', -- pendiente, pagada, cancelada, devuelta
    cancelada BOOLEAN DEFAULT false,
    motivo_cancelacion TEXT,
    vendedor_id INTEGER REFERENCES usuarios(id),
    cajero_id INTEGER REFERENCES usuarios(id),
    caja_id INTEGER, -- Referencia a tabla de cajas (si existe)
    observaciones TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. DETALLE DE VENTAS
CREATE TABLE ventas_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id INTEGER REFERENCES productos(id),
    variante_id INTEGER REFERENCES variantes_producto(id),
    numero_linea INTEGER NOT NULL,
    descripcion VARCHAR(500),
    cantidad DECIMAL(10,3) NOT NULL,
    precio_unitario DECIMAL(15,2) NOT NULL,
    descuento_porcentaje DECIMAL(5,2) DEFAULT 0,
    descuento_monto DECIMAL(15,2) DEFAULT 0,
    subtotal DECIMAL(15,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    impuestos JSONB, -- Array de impuestos aplicados
    total_linea DECIMAL(15,2) NOT NULL,
    almacen_id INTEGER REFERENCES almacenes(id),
    lote_numero VARCHAR(100),
    seriales TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. PAGOS DE VENTAS
CREATE TABLE ventas_pagos (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT REFERENCES ventas(id) ON DELETE CASCADE,
    metodo_pago_id INTEGER REFERENCES metodos_pago(id),
    monto DECIMAL(15,2) NOT NULL,
    referencia VARCHAR(100),
    comision DECIMAL(15,2) DEFAULT 0,
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50) DEFAULT 'completado',
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);

-- 14. IMPUESTOS
CREATE TABLE impuestos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    tipo VARCHAR(50) DEFAULT 'iva', -- iva, ieps, isr, etc.
    activo BOOLEAN DEFAULT true,
    incluir_en_precio BOOLEAN DEFAULT false,
    pais VARCHAR(100),
    estado VARCHAR(100),
    aplica_desde DATE DEFAULT CURRENT_DATE,
    aplica_hasta DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar impuestos básicos
INSERT INTO impuestos (codigo, nombre, porcentaje, tipo) VALUES
('IVA16', 'IVA 16%', 16.00, 'iva'),
('IVA0', 'IVA 0%', 0.00, 'iva'),
('EXENTO', 'Exento', 0.00, 'exento');

-- 15. DESCUENTOS Y PROMOCIONES
CREATE TABLE descuentos_promociones (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- porcentaje, monto_fijo, bxgx, etc.
    valor DECIMAL(10,2) NOT NULL,
    aplicacion VARCHAR(50) DEFAULT 'producto', -- producto, venta_total
    condiciones JSONB, -- {monto_minimo: 1000, productos: [1,2,3]}
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP,
    activo BOOLEAN DEFAULT true,
    usos_maximos INTEGER,
    usos_actuales INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);

-- ============================================
-- MÓDULO DE COMPRAS (PARA FUTURO ERP)
-- ============================================

-- 16. PROVEEDORES
CREATE TABLE proveedores (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    rfc VARCHAR(20),
    razon_social VARCHAR(200) NOT NULL,
    nombre_comercial VARCHAR(200),
    contacto_nombre VARCHAR(200),
    contacto_email VARCHAR(100),
    contacto_telefono VARCHAR(20),
    direccion TEXT,
    ciudad VARCHAR(100),
    estado VARCHAR(100),
    pais VARCHAR(100) DEFAULT 'México',
    terminos_pago VARCHAR(100),
    dias_credito INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    calificacion INTEGER CHECK (calificacion >= 1 AND calificacion <= 5),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);

-- 17. ÓRDENES DE COMPRA
CREATE TABLE ordenes_compra (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id INTEGER REFERENCES proveedores(id),
    folio VARCHAR(50) UNIQUE NOT NULL,
    fecha_orden TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega_estimada DATE,
    estado VARCHAR(50) DEFAULT 'pendiente', -- pendiente, parcial, completada, cancelada
    subtotal DECIMAL(15,2) DEFAULT 0,
    impuestos_total DECIMAL(15,2) DEFAULT 0,
    total DECIMAL(15,2) DEFAULT 0,
    observaciones TEXT,
    creado_por INTEGER REFERENCES usuarios(id),
    aprobado_por INTEGER REFERENCES usuarios(id),
    fecha_aprobacion TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 18. DETALLE ÓRDENES DE COMPRA
CREATE TABLE ordenes_compra_detalle (
    id BIGSERIAL PRIMARY KEY,
    orden_compra_id BIGINT REFERENCES ordenes_compra(id) ON DELETE CASCADE,
    producto_id INTEGER REFERENCES productos(id),
    variante_id INTEGER REFERENCES variantes_producto(id),
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(15,2) NOT NULL,
    descuento_porcentaje DECIMAL(5,2) DEFAULT 0,
    total_linea DECIMAL(15,2) NOT NULL,
    cantidad_recibida INTEGER DEFAULT 0,
    almacen_destino_id INTEGER REFERENCES almacenes(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- MÓDULO DE CAJA / ARQUEO
-- ============================================

-- 19. CAJAS REGISTRADORAS
CREATE TABLE cajas (
    id SERIAL PRIMARY KEY,
    sucursal_id INTEGER REFERENCES sucursales(id),
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    almacen_asociado_id INTEGER REFERENCES almacenes(id),
    activa BOOLEAN DEFAULT true,
    ip_address INET,
    dispositivo_id VARCHAR(100),
    configuracion JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sucursal_id, codigo)
);

-- 20. APERTURAS DE CAJA
CREATE TABLE aperturas_caja (
    id BIGSERIAL PRIMARY KEY,
    caja_id INTEGER REFERENCES cajas(id),
    usuario_id INTEGER REFERENCES usuarios(id),
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP,
    saldo_inicial JSONB NOT NULL, -- {efectivo: 1000, monedas: [...]}
    saldo_final JSONB,
    ventas_efectivo DECIMAL(15,2) DEFAULT 0,
    ventas_tarjeta DECIMAL(15,2) DEFAULT 0,
    ventas_transferencia DECIMAL(15,2) DEFAULT 0,
    total_ventas DECIMAL(15,2) DEFAULT 0,
    diferencia DECIMAL(15,2) DEFAULT 0,
    observaciones TEXT,
    estado VARCHAR(50) DEFAULT 'abierta', -- abierta, cerrada
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 21. MOVIMIENTOS DE CAJA
CREATE TABLE movimientos_caja (
    id BIGSERIAL PRIMARY KEY,
    apertura_caja_id BIGINT REFERENCES aperturas_caja(id),
    tipo_movimiento VARCHAR(50) NOT NULL, -- ingreso, egreso, venta, pago
    concepto VARCHAR(200) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    metodo_pago_id INTEGER REFERENCES metodos_pago(id),
    referencia_id BIGINT, -- ID de venta, compra, etc.
    referencia_tipo VARCHAR(50),
    observaciones TEXT,
    creado_por INTEGER REFERENCES usuarios(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- MÓDULO DE CONTABILIDAD (BASE PARA ERP)
-- ============================================

-- 22. CUENTAS CONTABLES
CREATE TABLE cuentas_contables (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- activo, pasivo, capital, ingreso, gasto
    subtipo VARCHAR(100),
    parent_id INTEGER REFERENCES cuentas_contables(id),
    nivel INTEGER DEFAULT 1,
    naturaleza VARCHAR(10) DEFAULT 'deudora', -- deudora, acreedora
    moneda VARCHAR(3) DEFAULT 'MXN',
    activa BOOLEAN DEFAULT true,
    es_banco BOOLEAN DEFAULT false,
    permite_movimientos BOOLEAN DEFAULT true,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);

-- 23. ASIENTOS CONTABLES
CREATE TABLE asientos_contables (
    id BIGSERIAL PRIMARY KEY,
    folio VARCHAR(100) UNIQUE NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    concepto TEXT NOT NULL,
    tipo_asiento VARCHAR(50) DEFAULT 'manual', -- manual, automatico
    referencia_tipo VARCHAR(50), -- venta, compra, etc.
    referencia_id BIGINT,
    total_debe DECIMAL(15,2) NOT NULL,
    total_haber DECIMAL(15,2) NOT NULL,
    estado VARCHAR(50) DEFAULT 'borrador', -- borrador, publicado
    creado_por INTEGER REFERENCES usuarios(id),
    publicado_por INTEGER REFERENCES usuarios(id),
    fecha_publicacion TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 24. DETALLE ASIENTOS CONTABLES
CREATE TABLE asientos_contables_detalle (
    id BIGSERIAL PRIMARY KEY,
    asiento_id BIGINT REFERENCES asientos_contables(id) ON DELETE CASCADE,
    cuenta_id INTEGER REFERENCES cuentas_contables(id),
    debe DECIMAL(15,2) DEFAULT 0,
    haber DECIMAL(15,2) DEFAULT 0,
    concepto TEXT,
    referencia_tipo VARCHAR(50),
    referencia_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- MÓDULO DE REPORTES Y ANÁLISIS
-- ============================================

-- 25. REPORTES PROGRAMADOS
CREATE TABLE reportes_programados (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    tipo_reporte VARCHAR(100) NOT NULL, -- ventas, inventario, etc.
    formato VARCHAR(50) DEFAULT 'pdf', -- pdf, excel, html
    parametros JSONB,
    programacion_cron VARCHAR(100), -- Expresión cron
    ultima_ejecucion TIMESTAMP,
    proxima_ejecucion TIMESTAMP,
    activo BOOLEAN DEFAULT true,
    destino_email TEXT[], -- Array de emails
    guardar_en_servidor BOOLEAN DEFAULT true,
    creado_por INTEGER REFERENCES usuarios(id),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 26. DASHBOARDS
CREATE TABLE dashboards (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    configuracion JSONB NOT NULL, -- Widgets y layout
    tipo VARCHAR(50) DEFAULT 'personal', -- personal, compartido, sistema
    usuario_id INTEGER REFERENCES usuarios(id),
    compartido_con JSONB, -- Array de user/role IDs
    es_default BOOLEAN DEFAULT false,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- MÓDULO DE NOTIFICACIONES
-- ============================================

-- 27. NOTIFICACIONES
CREATE TABLE notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuarios(id),
    tipo VARCHAR(50) NOT NULL, -- alerta, info, warning, success
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT false,
    accion_url TEXT,
    accion_texto VARCHAR(100),
    prioridad INTEGER DEFAULT 0,
    expira_en TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 28. PLANTILLAS DE NOTIFICACIÓN
CREATE TABLE plantillas_notificacion (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    asunto VARCHAR(500),
    cuerpo TEXT NOT NULL,
    tipo VARCHAR(50) DEFAULT 'email', -- email, sms, push, in_app
    variables JSONB, -- Variables disponibles para la plantilla
    activa BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ============================================

-- Índices para búsquedas frecuentes
CREATE INDEX idx_productos_codigo ON productos(codigo_interno);
CREATE INDEX idx_productos_codigo_barras ON productos(codigo_barras);
CREATE INDEX idx_productos_categoria ON productos(categoria_id);
CREATE INDEX idx_productos_activo ON productos(activo);

CREATE INDEX idx_ventas_fecha ON ventas(fecha_hora);
CREATE INDEX idx_ventas_cliente ON ventas(cliente_id);
CREATE INDEX idx_ventas_estado ON ventas(estado);
CREATE INDEX idx_ventas_folio ON ventas(folio);
CREATE INDEX idx_ventas_sucursal ON ventas(sucursal_id);

CREATE INDEX idx_clientes_codigo ON clientes(codigo_cliente);
CREATE INDEX idx_clientes_nombre ON clientes(nombre);
CREATE INDEX idx_clientes_email ON clientes(email);

CREATE INDEX idx_inventario_producto ON inventario(producto_id);
CREATE INDEX idx_inventario_almacen ON inventario(almacen_id);
CREATE INDEX idx_inventario_existencia ON inventario(existencia);

CREATE INDEX idx_movimientos_inventario_producto ON movimientos_inventario(producto_id);
CREATE INDEX idx_movimientos_inventario_fecha ON movimientos_inventario(created_at);
CREATE INDEX idx_movimientos_inventario_tipo ON movimientos_inventario(tipo_movimiento);

CREATE INDEX idx_variantes_sku ON variantes_producto(sku);
CREATE INDEX idx_variantes_producto ON variantes_producto(producto_id);

-- Índices compuestos para mejor performance
CREATE INDEX idx_ventas_detalle_venta ON ventas_detalle(venta_id, numero_linea);
CREATE INDEX idx_inventario_producto_variante ON inventario(producto_id, variante_id);
CREATE INDEX idx_productos_busqueda ON productos USING gin(to_tsvector('spanish', nombre || ' ' || descripcion));

-- ============================================
-- FUNCIONES Y PROCEDIMIENTOS ÚTILES
-- ============================================

-- Función para generar folios automáticos
CREATE OR REPLACE FUNCTION generar_folio_venta(sucursal_prefix VARCHAR)
RETURNS VARCHAR AS $$
DECLARE
    folio_num BIGINT;
    fecha_actual VARCHAR;
    folio_completo VARCHAR;
BEGIN
    fecha_actual := TO_CHAR(CURRENT_DATE, 'YYYYMMDD');
    
    -- Obtener el próximo número de secuencia para el día
    SELECT COALESCE(MAX(SUBSTRING(folio FROM '\d+$')::BIGINT), 0) + 1
    INTO folio_num
    FROM ventas
    WHERE folio LIKE sucursal_prefix || fecha_actual || '%';
    
    -- Construir el folio
    folio_completo := sucursal_prefix || fecha_actual || LPAD(folio_num::TEXT, 4, '0');
    
    RETURN folio_completo;
END;
$$ LANGUAGE plpgsql;

-- Función para actualizar inventario automáticamente
CREATE OR REPLACE FUNCTION actualizar_inventario(
    p_producto_id INTEGER,
    p_variante_id INTEGER,
    p_almacen_id INTEGER,
    p_cantidad INTEGER,
    p_tipo_movimiento VARCHAR
) RETURNS INTEGER AS $$
DECLARE
    v_existencia_actual INTEGER;
    v_nueva_existencia INTEGER;
    v_inventario_id INTEGER;
BEGIN
    -- Buscar o crear registro de inventario
    SELECT id, existencia 
    INTO v_inventario_id, v_existencia_actual
    FROM inventario
    WHERE producto_id = p_producto_id 
      AND variante_id = p_variante_id 
      AND almacen_id = p_almacen_id;
    
    -- Calcular nueva existencia
    IF p_tipo_movimiento = 'entrada' THEN
        v_nueva_existencia := COALESCE(v_existencia_actual, 0) + p_cantidad;
    ELSIF p_tipo_movimiento = 'salida' THEN
        v_nueva_existencia := COALESCE(v_existencia_actual, 0) - p_cantidad;
    ELSE
        RAISE EXCEPTION 'Tipo de movimiento no válido: %', p_tipo_movimiento;
    END IF;
    
    -- Actualizar o insertar inventario
    IF v_inventario_id IS NOT NULL THEN
        UPDATE inventario 
        SET existencia = v_nueva_existencia,
            existencia_disponible = v_nueva_existencia - existencia_reservada,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = v_inventario_id;
    ELSE
        INSERT INTO inventario (
            producto_id, variante_id, almacen_id, 
            existencia, existencia_disponible
        ) VALUES (
            p_producto_id, p_variante_id, p_almacen_id,
            v_nueva_existencia, v_nueva_existencia
        )
        RETURNING id INTO v_inventario_id;
    END IF;
    
    RETURN v_inventario_id;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar inventario al crear venta
CREATE OR REPLACE FUNCTION trigger_actualizar_inventario_venta()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo procesar si la venta no está cancelada
    IF NEW.estado = 'pagada' AND NEW.cancelada = false THEN
        -- Actualizar inventario para cada línea de la venta
        PERFORM actualizar_inventario(
            producto_id, variante_id, almacen_id,
            cantidad, 'salida'
        )
        FROM ventas_detalle
        WHERE venta_id = NEW.id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar saldo de cliente
CREATE OR REPLACE FUNCTION trigger_actualizar_saldo_cliente()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.estado = 'pagada' AND OLD.estado != 'pagada' THEN
        UPDATE clientes 
        SET saldo_actual = saldo_actual + NEW.total
        WHERE id = NEW.cliente_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- VISTAS PARA REPORTES Y DASHBOARDS
-- ============================================

-- Vista: Resumen de ventas diarias
CREATE OR REPLACE VIEW vista_ventas_diarias AS
SELECT 
    DATE(v.fecha_hora) as fecha,
    s.nombre as sucursal,
    COUNT(*) as total_ventas,
    SUM(v.total) as total_ingresos,
    AVG(v.total) as ticket_promedio,
    COUNT(DISTINCT v.cliente_id) as clientes_unicos,
    SUM(vd.cantidad) as unidades_vendidas
FROM ventas v
JOIN sucursales s ON v.sucursal_id = s.id
JOIN ventas_detalle vd ON v.id = vd.venta_id
WHERE v.cancelada = false
GROUP BY DATE(v.fecha_hora), s.nombre
ORDER BY fecha DESC;

-- Vista: Top productos vendidos
CREATE OR REPLACE VIEW vista_top_productos AS
SELECT 
    p.codigo_interno,
    p.nombre,
    c.nombre as categoria,
    SUM(vd.cantidad) as unidades_vendidas,
    SUM(vd.total_linea) as ingresos_totales,
    COUNT(DISTINCT v.id) as veces_vendido
FROM ventas_detalle vd
JOIN productos p ON vd.producto_id = p.id
JOIN ventas v ON vd.venta_id = v.id
LEFT JOIN categorias_productos c ON p.categoria_id = c.id
WHERE v.cancelada = false
GROUP BY p.id, p.codigo_interno, p.nombre, c.nombre
ORDER BY unidades_vendidas DESC;

-- Vista: Inventario bajo mínimo
CREATE OR REPLACE VIEW vista_inventario_bajo_minimo AS
SELECT 
    p.codigo_interno,
    p.nombre as producto,
    v.sku as variante,
    a.codigo as almacen,
    a.nombre as nombre_almacen,
    i.existencia,
    i.stock_minimo,
    CASE 
        WHEN i.existencia <= i.stock_minimo THEN 'CRÍTICO'
        WHEN i.existencia <= i.stock_minimo * 1.5 THEN 'BAJO'
        ELSE 'OK'
    END as estado
FROM inventario i
JOIN productos p ON i.producto_id = p.id
LEFT JOIN variantes_producto v ON i.variante_id = v.id
JOIN almacenes a ON i.almacen_id = a.id
WHERE i.existencia <= i.stock_minimo * 1.5
AND p.activo = true
ORDER BY i.existencia / NULLIF(i.stock_minimo, 0);

-- Vista: Movimientos de caja del día
CREATE OR REPLACE VIEW vista_movimientos_caja_hoy AS
SELECT 
    c.nombre as caja,
    u.username as usuario,
    ac.fecha_apertura,
    ac.saldo_inicial,
    ac.ventas_efectivo,
    ac.ventas_tarjeta,
    ac.total_ventas,
    ac.estado
FROM aperturas_caja ac
JOIN cajas c ON ac.caja_id = c.id
JOIN usuarios u ON ac.usuario_id = u.id
WHERE DATE(ac.fecha_apertura) = CURRENT_DATE
ORDER BY ac.fecha_apertura DESC;

-- ============================================
-- PROCEDIMIENTOS DE MANTENIMIENTO
-- ============================================

-- Procedimiento: Cierre de caja diario
CREATE OR REPLACE PROCEDURE cerrar_caja_diaria(p_caja_id INTEGER, p_usuario_id INTEGER)
LANGUAGE plpgsql
AS $$
DECLARE
    v_apertura_id BIGINT;
    v_total_efectivo DECIMAL;
    v_total_tarjeta DECIMAL;
    v_total_otros DECIMAL;
BEGIN
    -- Encontrar apertura activa
    SELECT id INTO v_apertura_id
    FROM aperturas_caja
    WHERE caja_id = p_caja_id 
      AND estado = 'abierta'
      AND usuario_id = p_usuario_id
    ORDER BY fecha_apertura DESC
    LIMIT 1;
    
    IF v_apertura_id IS NULL THEN
        RAISE EXCEPTION 'No hay apertura de caja activa para este usuario';
    END IF;
    
    -- Calcular totales
    SELECT 
        COALESCE(SUM(CASE WHEN mp.tipo = 'efectivo' THEN vp.monto ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN mp.tipo = 'tarjeta' THEN vp.monto ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN mp.tipo NOT IN ('efectivo', 'tarjeta') THEN vp.monto ELSE 0 END), 0)
    INTO v_total_efectivo, v_total_tarjeta, v_total_otros
    FROM ventas_pagos vp
    JOIN metodos_pago mp ON vp.metodo_pago_id = mp.id
    JOIN ventas v ON vp.venta_id = v.id
    WHERE v.cajero_id = p_usuario_id
      AND DATE(v.fecha_hora) = CURRENT_DATE
      AND v.cancelada = false;
    
    -- Actualizar apertura de caja
    UPDATE aperturas_caja
    SET 
        fecha_cierre = CURRENT_TIMESTAMP,
        ventas_efectivo = v_total_efectivo,
        ventas_tarjeta = v_total_tarjeta,
        total_ventas = v_total_efectivo + v_total_tarjeta + v_total_otros,
        estado = 'cerrada',
        updated_at = CURRENT_TIMESTAMP
    WHERE id = v_apertura_id;
    
    -- Registrar actividad
    INSERT INTO actividad_usuario (usuario_id, tipo_actividad, modulo, descripcion)
    VALUES (p_usuario_id, 'CIERRE_CAJA', 'caja', 'Cierre de caja diario realizado');
    
    RAISE NOTICE 'Caja cerrada exitosamente. Totales: Efectivo: %, Tarjeta: %', 
        v_total_efectivo, v_total_tarjeta;
END;
$$;

-- Procedimiento: Ajuste de inventario
CREATE OR REPLACE PROCEDURE ajustar_inventario(
    p_producto_id INTEGER,
    p_variante_id INTEGER,
    p_almacen_id INTEGER,
    p_nueva_existencia INTEGER,
    p_concepto TEXT,
    p_usuario_id INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_existencia_actual INTEGER;
    v_diferencia INTEGER;
BEGIN
    -- Obtener existencia actual
    SELECT existencia INTO v_existencia_actual
    FROM inventario
    WHERE producto_id = p_producto_id 
      AND variante_id = p_variante_id 
      AND almacen_id = p_almacen_id;
    
    v_existencia_actual := COALESCE(v_existencia_actual, 0);
    v_diferencia := p_nueva_existencia - v_existencia_actual;
    
    -- Actualizar inventario
    PERFORM actualizar_inventario(
        p_producto_id, p_variante_id, p_almacen_id,
        ABS(v_diferencia),
        CASE WHEN v_diferencia > 0 THEN 'entrada' ELSE 'salida' END
    );
    
    -- Registrar movimiento de inventario
    INSERT INTO movimientos_inventario (
        tipo_movimiento, producto_id, variante_id,
        almacen_origen_id, cantidad, existencia_anterior,
        existencia_nueva, concepto, created_by
    ) VALUES (
        'ajuste', p_producto_id, p_variante_id,
        p_almacen_id, ABS(v_diferencia), v_existencia_actual,
        p_nueva_existencia, p_concepto, p_usuario_id
    );
    
    -- Registrar actividad
    INSERT INTO actividad_usuario (usuario_id, tipo_actividad, modulo, descripcion)
    VALUES (p_usuario_id, 'AJUSTE_INVENTARIO', 'inventario', 
            'Ajuste de inventario: ' || p_concepto);
    
    RAISE NOTICE 'Inventario ajustado. Diferencia: %, Nueva existencia: %', 
        v_diferencia, p_nueva_existencia;
END;
$$;

-- ============================================
-- DATOS INICIALES PARA PRUEBAS
-- ============================================

-- Insertar sucursal principal
INSERT INTO sucursales (codigo, nombre, direccion, ciudad, es_principal) 
VALUES ('SUC001', 'Sucursal Principal', 'Av. Principal 123', 'Ciudad de México', true);

-- Insertar almacén principal
INSERT INTO almacenes (sucursal_id, codigo, nombre, tipo, es_default)
VALUES (1, 'ALM001', 'Almacén Principal', 'principal', true);

-- Insertar categorías de ejemplo
INSERT INTO categorias_productos (codigo, nombre) VALUES
('CAT001', 'Electrónica'),
('CAT002', 'Ropa'),
('CAT003', 'Alimentos'),
('CAT004', 'Hogar');

-- Insertar productos de ejemplo
INSERT INTO productos (codigo_interno, nombre, descripcion, categoria_id, precio_base) VALUES
('PROD001', 'Laptop HP', 'Laptop HP 15.6 pulgadas', 1, 15000.00),
('PROD002', 'Camiseta Algodón', 'Camiseta 100% algodón', 2, 250.00),
('PROD003', 'Arroz 1kg', 'Arroz integral 1kg', 3, 30.00);

-- Insertar cliente de ejemplo
INSERT INTO clientes (codigo_cliente, nombre, email, telefono, tipo_cliente) 
VALUES ('CLI001', 'Cliente General', 'cliente@ejemplo.com', '5551234567', 'persona');

-- ============================================
-- ESTRUCTURA MODULAR COMPLETA
-- ============================================

/*
ESTRUCTURA POR MÓDULOS:

1. BASE
   - modulos_sistema
   - usuarios (ya existente)
   - configuracion_sistema (ya existente)

2. POS
   - sucursales
   - almacenes
   - cajas
   - aperturas_caja
   - movimientos_caja

3. INVENTARIO
   - categorias_productos
   - productos
   - variantes_producto
   - precios_producto
   - inventario
   - movimientos_inventario

4. VENTAS
   - ventas
   - ventas_detalle
   - ventas_pagos
   - clientes
   - metodos_pago

5. COMPRAS (Futuro)
   - proveedores
   - ordenes_compra
   - ordenes_compra_detalle

6. CONTABILIDAD (Futuro)
   - cuentas_contables
   - asientos_contables
   - asientos_contables_detalle

7. REPORTES
   - reportes_programados
   - dashboards

8. NOTIFICACIONES
   - notificaciones
   - plantillas_notificacion

9. IMPUESTOS Y DESCUENTOS
   - impuestos
   - descuentos_promociones
*/

-- ============================================
-- PERMISOS PARA LOS NUEVOS MÓDULOS
-- ============================================

-- Agregar permisos para los nuevos módulos (se integra con tu sistema de permisos)
INSERT INTO permisos (codigo, nombre, descripcion, modulo, categoria) VALUES
-- Módulo POS
('pos.acceso', 'Acceso al POS', 'Acceso al módulo de punto de venta', 'pos', 'acceso'),
('pos.ventas.crear', 'Crear ventas', 'Crear nuevas ventas en POS', 'pos', 'escritura'),
('pos.ventas.cancelar', 'Cancelar ventas', 'Cancelar ventas existentes', 'pos', 'escritura'),
('pos.caja.administrar', 'Administrar caja', 'Realizar apertura/cierre de caja', 'pos', 'privilegios'),

-- Módulo Inventario
('inventario.ver', 'Ver inventario', 'Ver niveles de inventario', 'inventario', 'lectura'),
('inventario.ajustar', 'Ajustar inventario', 'Realizar ajustes de inventario', 'inventario', 'escritura'),
('inventario.productos.crear', 'Crear productos', 'Crear nuevos productos', 'inventario', 'escritura'),
('inventario.productos.editar', 'Editar productos', 'Editar productos existentes', 'inventario', 'escritura'),

-- Módulo Clientes
('clientes.ver', 'Ver clientes', 'Ver lista de clientes', 'clientes', 'lectura'),
('clientes.crear', 'Crear clientes', 'Crear nuevos clientes', 'clientes', 'escritura'),
('clientes.editar', 'Editar clientes', 'Editar clientes existentes', 'clientes', 'escritura'),

-- Módulo Reportes
('reportes.ver', 'Ver reportes', 'Acceder a reportes del sistema', 'reportes', 'lectura'),
('reportes.exportar', 'Exportar reportes', 'Exportar reportes a diferentes formatos', 'reportes', 'escritura');
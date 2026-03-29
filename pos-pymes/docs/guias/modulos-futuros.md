# Modulos futuros - Referencia de diseño

Tablas y estructuras planificadas para futuros modulos del ERP.
Extraido de todo_erp.sql como referencia. NO ejecutar directamente,
adaptar cuando se implemente cada modulo.

---

## Mapa de modulos

```
1. BASE (implementado)
   - tenants, usuarios, roles, menus, permisos, sesiones

2. POS - Punto de Venta
   - sucursales, almacenes, cajas, aperturas_caja, movimientos_caja

3. INVENTARIO
   - categorias_productos, productos, variantes_producto,
     precios_producto, inventario, movimientos_inventario

4. VENTAS
   - ventas, ventas_detalle, ventas_pagos, clientes, metodos_pago

5. COMPRAS
   - proveedores, ordenes_compra, ordenes_compra_detalle

6. CONTABILIDAD
   - cuentas_contables, asientos_contables, asientos_contables_detalle

7. IMPUESTOS Y DESCUENTOS
   - impuestos, descuentos_promociones

8. REPORTES
   - reportes_programados, dashboards

9. NOTIFICACIONES
   - notificaciones, plantillas_notificacion
```

---

## Modulo POS

### sucursales
```sql
CREATE TABLE sucursales (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(100),
    ciudad VARCHAR(100),
    pais VARCHAR(100) DEFAULT 'Guatemala',
    moneda_base VARCHAR(3) DEFAULT 'Q',
    timezone VARCHAR(50) DEFAULT 'America/Guatemala',
    activa BOOLEAN DEFAULT true,
    es_principal BOOLEAN DEFAULT false,
    configuracion_pos JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id),
    updated_by INTEGER REFERENCES usuarios(id),
    tenant_id INTEGER REFERENCES tenants(id)
);
```

### almacenes
```sql
CREATE TABLE almacenes (
    id SERIAL PRIMARY KEY,
    sucursal_id INTEGER REFERENCES sucursales(id),
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) DEFAULT 'principal', -- principal, temporal, devoluciones
    direccion TEXT,
    capacidad_maxima INTEGER,
    activo BOOLEAN DEFAULT true,
    es_default BOOLEAN DEFAULT false,
    responsable_id INTEGER REFERENCES usuarios(id),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sucursal_id, codigo)
);
```

### cajas
```sql
CREATE TABLE cajas (
    id SERIAL PRIMARY KEY,
    sucursal_id INTEGER REFERENCES sucursales(id),
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    almacen_asociado_id INTEGER REFERENCES almacenes(id),
    activa BOOLEAN DEFAULT true,
    ip_address INET,
    configuracion JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sucursal_id, codigo),
    tenant_id INTEGER REFERENCES tenants(id)
);
```

### aperturas_caja
```sql
CREATE TABLE aperturas_caja (
    id BIGSERIAL PRIMARY KEY,
    caja_id INTEGER REFERENCES cajas(id),
    usuario_id INTEGER REFERENCES usuarios(id),
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP,
    saldo_inicial JSONB NOT NULL,
    saldo_final JSONB,
    ventas_efectivo DECIMAL(15,2) DEFAULT 0,
    ventas_tarjeta DECIMAL(15,2) DEFAULT 0,
    ventas_transferencia DECIMAL(15,2) DEFAULT 0,
    total_ventas DECIMAL(15,2) DEFAULT 0,
    diferencia DECIMAL(15,2) DEFAULT 0,
    estado VARCHAR(50) DEFAULT 'abierta', -- abierta, cerrada
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id INTEGER REFERENCES tenants(id)
);
```

---

## Modulo INVENTARIO

### categorias_productos
```sql
CREATE TABLE categorias_productos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    parent_id INTEGER REFERENCES categorias_productos(id) ON DELETE CASCADE,
    nivel INTEGER DEFAULT 1,
    imagen_url TEXT,
    activa BOOLEAN DEFAULT true,
    orden INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id INTEGER REFERENCES tenants(id)
);
```

### productos
```sql
CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    codigo_barras VARCHAR(100) UNIQUE,
    codigo_interno VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    categoria_id INTEGER REFERENCES categorias_productos(id),
    tipo_producto VARCHAR(50) DEFAULT 'producto', -- producto, servicio, combo
    unidad_medida VARCHAR(50) DEFAULT 'unidad',
    imagen_principal TEXT,
    activo BOOLEAN DEFAULT true,
    vendible BOOLEAN DEFAULT true,
    comprable BOOLEAN DEFAULT true,
    inventariable BOOLEAN DEFAULT true,
    precio_base DECIMAL(15,2) NOT NULL DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);
```

### variantes_producto
```sql
CREATE TABLE variantes_producto (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES productos(id) ON DELETE CASCADE,
    sku VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(200),
    atributos JSONB NOT NULL, -- {talla: 'M', color: 'Rojo'}
    precio_base DECIMAL(15,2) NOT NULL DEFAULT 0,
    costo_promedio DECIMAL(15,2) DEFAULT 0,
    activa BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id INTEGER REFERENCES tenants(id)
);
```

### inventario
```sql
CREATE TABLE inventario (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES productos(id) ON DELETE CASCADE,
    variante_id INTEGER REFERENCES variantes_producto(id) ON DELETE CASCADE,
    almacen_id INTEGER REFERENCES almacenes(id) ON DELETE CASCADE,
    existencia INTEGER NOT NULL DEFAULT 0,
    existencia_disponible INTEGER NOT NULL DEFAULT 0,
    existencia_reservada INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER DEFAULT 0,
    stock_maximo INTEGER DEFAULT 0,
    costo_promedio DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(producto_id, variante_id, almacen_id)
);
```

### movimientos_inventario
```sql
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
    concepto TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);
```

---

## Modulo VENTAS

### clientes
```sql
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    tipo_cliente VARCHAR(20) DEFAULT 'persona', -- persona, empresa
    codigo_cliente VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion TEXT,
    ciudad VARCHAR(100),
    pais VARCHAR(100) DEFAULT 'Guatemala',
    limite_credito DECIMAL(15,2) DEFAULT 0,
    dias_credito INTEGER DEFAULT 0,
    saldo_actual DECIMAL(15,2) DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);
```

### metodos_pago
```sql
CREATE TABLE metodos_pago (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- efectivo, tarjeta, transferencia
    acepta_cambio BOOLEAN DEFAULT true,
    requiere_referencia BOOLEAN DEFAULT false,
    comision_porcentaje DECIMAL(5,2) DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    orden INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### ventas
```sql
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
    estado VARCHAR(50) DEFAULT 'pendiente', -- pendiente, pagada, cancelada
    vendedor_id INTEGER REFERENCES usuarios(id),
    cajero_id INTEGER REFERENCES usuarios(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id INTEGER REFERENCES tenants(id)
);
```

### ventas_detalle
```sql
CREATE TABLE ventas_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id INTEGER REFERENCES productos(id),
    variante_id INTEGER REFERENCES variantes_producto(id),
    cantidad DECIMAL(10,3) NOT NULL,
    precio_unitario DECIMAL(15,2) NOT NULL,
    descuento_porcentaje DECIMAL(5,2) DEFAULT 0,
    total_linea DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Modulo COMPRAS

### proveedores
```sql
CREATE TABLE proveedores (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    razon_social VARCHAR(200) NOT NULL,
    contacto_nombre VARCHAR(200),
    contacto_email VARCHAR(100),
    contacto_telefono VARCHAR(20),
    direccion TEXT,
    terminos_pago VARCHAR(100),
    dias_credito INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES usuarios(id)
);
```

### ordenes_compra
```sql
CREATE TABLE ordenes_compra (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id INTEGER REFERENCES proveedores(id),
    folio VARCHAR(50) UNIQUE NOT NULL,
    fecha_orden TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega_estimada DATE,
    estado VARCHAR(50) DEFAULT 'pendiente', -- pendiente, parcial, completada, cancelada
    subtotal DECIMAL(15,2) DEFAULT 0,
    total DECIMAL(15,2) DEFAULT 0,
    creado_por INTEGER REFERENCES usuarios(id),
    aprobado_por INTEGER REFERENCES usuarios(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id INTEGER REFERENCES tenants(id)
);
```

---

## Modulo IMPUESTOS

### impuestos
```sql
CREATE TABLE impuestos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    tipo VARCHAR(50) DEFAULT 'iva', -- iva, ieps, isr
    activo BOOLEAN DEFAULT true,
    incluir_en_precio BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Datos iniciales
INSERT INTO impuestos (codigo, nombre, porcentaje, tipo) VALUES
('IVA12', 'IVA 12%', 12.00, 'iva'),
('IVA0', 'IVA 0%', 0.00, 'iva'),
('EXENTO', 'Exento', 0.00, 'exento');
```

---

## Funciones utiles de PostgreSQL

### Generar folios automaticos
```sql
CREATE OR REPLACE FUNCTION generar_folio_venta(sucursal_prefix VARCHAR)
RETURNS VARCHAR AS $$
DECLARE
    folio_num BIGINT;
    fecha_actual VARCHAR;
BEGIN
    fecha_actual := TO_CHAR(CURRENT_DATE, 'YYYYMMDD');
    SELECT COALESCE(MAX(SUBSTRING(folio FROM '\d+$')::BIGINT), 0) + 1
    INTO folio_num
    FROM ventas
    WHERE folio LIKE sucursal_prefix || fecha_actual || '%';
    RETURN sucursal_prefix || fecha_actual || LPAD(folio_num::TEXT, 4, '0');
END;
$$ LANGUAGE plpgsql;
```

### Actualizar inventario automaticamente
```sql
CREATE OR REPLACE FUNCTION actualizar_inventario(
    p_producto_id INTEGER,
    p_variante_id INTEGER,
    p_almacen_id INTEGER,
    p_cantidad INTEGER,
    p_tipo_movimiento VARCHAR -- 'entrada' o 'salida'
) RETURNS INTEGER AS $$
-- Ver implementacion completa en todo_erp.sql original
$$ LANGUAGE plpgsql;
```

---

## Permisos sugeridos para modulos futuros

```sql
-- POS
('pos.acceso', 'Acceso al POS', 'Acceso al punto de venta', 'pos', 'acceso'),
('pos.ventas.crear', 'Crear ventas', 'Crear nuevas ventas', 'pos', 'escritura'),
('pos.ventas.cancelar', 'Cancelar ventas', 'Cancelar ventas', 'pos', 'escritura'),
('pos.caja.administrar', 'Administrar caja', 'Apertura/cierre de caja', 'pos', 'privilegios'),

-- Inventario
('inventario.ver', 'Ver inventario', 'Ver niveles de inventario', 'inventario', 'lectura'),
('inventario.ajustar', 'Ajustar inventario', 'Realizar ajustes', 'inventario', 'escritura'),
('inventario.productos.crear', 'Crear productos', 'Crear productos', 'inventario', 'escritura'),

-- Clientes
('clientes.ver', 'Ver clientes', 'Ver lista de clientes', 'clientes', 'lectura'),
('clientes.crear', 'Crear clientes', 'Crear clientes', 'clientes', 'escritura'),

-- Reportes
('reportes.ver', 'Ver reportes', 'Acceder a reportes', 'reportes', 'lectura'),
('reportes.exportar', 'Exportar reportes', 'Exportar reportes', 'reportes', 'escritura')
```

---

## Nota importante

Estas tablas son **referencia de diseño**. Cuando implementes cada modulo:
1. Adaptalas a las convenciones del proyecto (BIGSERIAL vs SERIAL, etc.)
2. Crea la migracion en `database/migrations/`
3. Crea la entidad JPA que coincida
4. Segui la guia `docs/guias/como-agregar-modulo.md`

-- Script de inicialización de la Base de Datos SQLite

-- Tabla de ingresos
CREATE TABLE IF NOT EXISTS ingresos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,         -- Formato ISO-8601 (YYYY-MM-DD)
    descripcion TEXT NOT NULL,
    valor REAL NOT NULL CHECK (valor >= 0),
    tipo TEXT NOT NULL           -- 'Salario', 'Bonificación', 'Venta', 'Freelance', 'Otros'
);

-- Tabla de gastos
CREATE TABLE IF NOT EXISTS gastos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,         -- Formato ISO-8601 (YYYY-MM-DD)
    descripcion TEXT NOT NULL,
    categoria TEXT NOT NULL,     -- 'Alimentación', 'Transporte', 'Servicios', 'Salud', 'Educación', 'Tecnología', 'Entretenimiento', 'Hogar', 'Otros'
    valor REAL NOT NULL CHECK (valor >= 0),
    observacion TEXT
);

-- Tabla de gastos fijos mensuales
CREATE TABLE IF NOT EXISTS gastos_fijos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    valor REAL NOT NULL CHECK (valor >= 0),
    dia_cobro INTEGER NOT NULL CHECK (dia_cobro BETWEEN 1 AND 31),
    estado TEXT NOT NULL DEFAULT 'Activo' -- 'Activo' o 'Inactivo'
);

-- Tabla de presupuestos
CREATE TABLE IF NOT EXISTS presupuestos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    categoria TEXT NOT NULL UNIQUE,
    valor_presupuestado REAL NOT NULL CHECK (valor_presupuestado >= 0),
    fecha_creacion TEXT NOT NULL
);

-- Índices para mejorar el rendimiento de consultas por fecha y filtros
CREATE INDEX IF NOT EXISTS idx_gastos_fecha ON gastos(fecha);
CREATE INDEX IF NOT EXISTS idx_ingresos_fecha ON ingresos(fecha);
CREATE INDEX IF NOT EXISTS idx_gastos_categoria ON gastos(categoria);

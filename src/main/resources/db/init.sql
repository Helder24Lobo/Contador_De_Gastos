-- Script de inicialización de la Base de Datos SQLite

-- Tabla de incomes
CREATE TABLE IF NOT EXISTS incomes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,         -- Formato ISO-8601 (YYYY-MM-DD)
    descripcion TEXT NOT NULL,
    valor REAL NOT NULL CHECK (valor >= 0),
    tipo TEXT NOT NULL           -- 'Salario', 'Bonificación', 'Venta', 'Freelance', 'Otros'
);

-- Tabla de expenditures
CREATE TABLE IF NOT EXISTS expenditures (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,         -- Formato ISO-8601 (YYYY-MM-DD)
    descripcion TEXT NOT NULL,
    categoria TEXT NOT NULL,     -- 'Arriendo', 'Servicios', 'Mercado', 'Cuota celular', 'Parqueadero', 'Gym', 'Aceite moto', 'Corte de cabello', 'Plan', 'Gasolina', 'Spotify', 'Internet', 'Otros'
    valor REAL NOT NULL CHECK (valor >= 0),
    observacion TEXT
);

-- Tabla de expenditures fijos mensuales
CREATE TABLE IF NOT EXISTS gastos_fijos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    valor REAL NOT NULL CHECK (valor >= 0),
    dia_cobro INTEGER NOT NULL CHECK (dia_cobro BETWEEN 1 AND 31),
    estado TEXT NOT NULL DEFAULT 'Activo' -- 'Activo' o 'Inactivo'
);

-- Tabla de specifications
CREATE TABLE IF NOT EXISTS specifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    categoria TEXT NOT NULL UNIQUE,
    valor_presupuestado REAL NOT NULL CHECK (valor_presupuestado >= 0),
    fecha_creacion TEXT NOT NULL
);

-- Índices para mejorar el rendimiento de consultas por fecha y filtros
CREATE INDEX IF NOT EXISTS idx_gastos_fecha ON expenditures(fecha);
CREATE INDEX IF NOT EXISTS idx_ingresos_fecha ON incomes(fecha);
CREATE INDEX IF NOT EXISTS idx_gastos_categoria ON expenditures(categoria);

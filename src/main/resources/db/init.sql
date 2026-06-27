-- Script de inicialización de la Base de Datos SQLite

-- Tabla de incomes
CREATE TABLE IF NOT EXISTS incomes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,         -- Formato ISO-8601 (YYYY-MM-DD)
    descripcion TEXT NOT NULL,
    valor REAL NOT NULL CHECK (valor >= 0),
    tipo TEXT NOT NULL           -- 'Salario', 'Bonificación', 'Venta', 'Freelance', 'Otros'
);

-- Tabla de savings
CREATE TABLE IF NOT EXISTS savings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    target_value REAL NOT NULL CHECK (target_value >= 0),
    dateCurrent TEXT NOT NULL,  -- Formato ISO-8601 (YYYY-MM-DD)
    status TEXT NOT NULL DEFAULT 'Activo'
);

-- Tabla de movimientos de ahorro
CREATE TABLE IF NOT EXISTS savings_movements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    saving_id INTEGER NOT NULL,
    amount REAL NOT NULL CHECK (amount > 0),
    date TEXT NOT NULL,  -- Formato ISO-8601 (YYYY-MM-DD)
    observation TEXT,
    FOREIGN KEY (saving_id) REFERENCES savings(id) ON DELETE CASCADE
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

export const DB_NAME = 'tikete_v2.db';
export const DB_VERSION = 2;

export const CREATE_TABLES = `

  CREATE TABLE IF NOT EXISTS empresa (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre      TEXT    NOT NULL,
    slogan      TEXT,
    logo        TEXT,
    direccion   TEXT,
    ciudad      TEXT,
    telefono    TEXT,
    celular     TEXT,
    correo      TEXT,
    sitio_web   TEXT,
    ruc         TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS categorias (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS productos (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre       TEXT    NOT NULL,
    descripcion  TEXT,
    precio       REAL    NOT NULL DEFAULT 0,
    categoria_id INTEGER REFERENCES categorias(id),
    imagen       TEXT,
    activo       INTEGER NOT NULL DEFAULT 1,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS clientes (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre     TEXT NOT NULL,
    telefono   TEXT,
    correo     TEXT,
    direccion  TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS facturas (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    numero          TEXT    NOT NULL UNIQUE,
    cliente_id      INTEGER REFERENCES clientes(id),
    cliente_nombre  TEXT,
    empresa_id      INTEGER,
    fecha           TEXT    NOT NULL,
    vencimiento     TEXT,
    subtotal        REAL    NOT NULL DEFAULT 0,
    descuento       REAL    NOT NULL DEFAULT 0,
    impuesto        REAL    NOT NULL DEFAULT 0,
    total           REAL    NOT NULL DEFAULT 0,
    estado          TEXT    NOT NULL DEFAULT 'pendiente',
    notas           TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS factura_items (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    factura_id   INTEGER NOT NULL REFERENCES facturas(id) ON DELETE CASCADE,
    producto_id  INTEGER REFERENCES productos(id),
    nombre       TEXT    NOT NULL,
    descripcion  TEXT,
    cantidad     REAL    NOT NULL DEFAULT 1,
    precio       REAL    NOT NULL DEFAULT 0,
    subtotal     REAL    NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS tickets (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    numero     TEXT    NOT NULL UNIQUE,
    empresa_id INTEGER,
    fecha      TEXT    NOT NULL,
    total      REAL    NOT NULL DEFAULT 0,
    notas      TEXT,
    created_at TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS ticket_items (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_id   INTEGER NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    producto_id INTEGER REFERENCES productos(id),
    nombre      TEXT    NOT NULL,
    descripcion TEXT,
    cantidad    REAL    NOT NULL DEFAULT 1,
    precio      REAL    NOT NULL DEFAULT 0,
    subtotal    REAL    NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS configuracion (
    id               INTEGER PRIMARY KEY DEFAULT 1,
    moneda           TEXT    NOT NULL DEFAULT 'USD',
    simbolo_moneda   TEXT    NOT NULL DEFAULT '$',
    impuesto         REAL    NOT NULL DEFAULT 0,
    impuesto_nombre  TEXT    NOT NULL DEFAULT 'IVA',
    prefijo_factura  TEXT    NOT NULL DEFAULT 'FAC-',
    prefijo_ticket   TEXT    NOT NULL DEFAULT 'TKT-',
    color_primario   TEXT    NOT NULL DEFAULT '#6C63FF',
    color_secundario TEXT    NOT NULL DEFAULT '#FF6584',
    correo_smtp      TEXT,
    correo_usuario   TEXT,
    correo_password  TEXT
  );

  INSERT OR IGNORE INTO configuracion (id) VALUES (1);
`;

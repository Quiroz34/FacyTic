// ─── Empresa / Negocio ───────────────────────────────────────────────
export interface Empresa {
  id: number;
  nombre: string;
  slogan?: string;
  logo?: string;          // ruta local del archivo
  direccion?: string;
  ciudad?: string;
  telefono?: string;
  celular?: string;
  correo?: string;
  sitioWeb?: string;
  ruc?: string;           // RUC / NIT / ID fiscal
  createdAt: string;
  updatedAt: string;
}

// ─── Categoría de producto ────────────────────────────────────────────
export interface Categoria {
  id: number;
  nombre: string;
}

// ─── Producto / Servicio ──────────────────────────────────────────────
export interface Producto {
  id: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  categoriaId?: number;
  imagen?: string;
  activo: number;         // 1 = activo, 0 = inactivo
  createdAt: string;
}

// ─── Cliente ──────────────────────────────────────────────────────────
export interface Cliente {
  id: number;
  nombre: string;
  telefono?: string;
  correo?: string;
  direccion?: string;
  createdAt: string;
}

// ─── Item de factura/ticket ───────────────────────────────────────────
export interface Item {
  id?: number;
  productoId?: number;
  nombre: string;
  descripcion?: string;
  cantidad: number;
  precio: number;
  subtotal: number;
}

// ─── Factura ──────────────────────────────────────────────────────────
export type EstadoFactura = 'pendiente' | 'pagada' | 'cancelada';

export interface Factura {
  id: number;
  numero: string;
  clienteId?: number;
  clienteNombre?: string;
  empresaId: number;
  fecha: string;
  vencimiento?: string;
  subtotal: number;
  descuento: number;
  impuesto: number;
  total: number;
  estado: EstadoFactura;
  clienteDocumento?: string;
  clienteUbicacion?: string;
  notas?: string;
  items: Item[];
  createdAt: string;
}

// ─── Ticket ───────────────────────────────────────────────────────────
export interface Ticket {
  id: number;
  numero: string;
  empresaId: number;
  fecha: string;
  total: number;
  notas?: string;
  items: Item[];
  createdAt: string;
}

// ─── Configuración ────────────────────────────────────────────────────
export interface Configuracion {
  moneda: string;         // '$', 'Q', 'S/', etc.
  simboloMoneda: string;
  impuesto: number;       // porcentaje (ej. 12)
  impuestoNombre: string; // 'IVA', 'IGV', etc.
  prefijoFactura: string; // 'FAC-', 'INV-', etc.
  prefijoTicket: string;  // 'TKT-', etc.
  colorPrimario: string;
  colorSecundario: string;
  correoSmtp?: string;
  correoUsuario?: string;
  correoPassword?: string;
}

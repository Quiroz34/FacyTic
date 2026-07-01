import { getDatabase } from '../index';
import { Factura, Item } from '../../types';

const mapItems = async (facturaId: number): Promise<Item[]> => {
  const db = getDatabase();
  const result = await db.execute('SELECT * FROM factura_items WHERE factura_id=?;', [facturaId]);
  return (result.rows ?? []).map((r: any) => ({
    id: r.id,
    productoId: r.producto_id,
    nombre: r.nombre,
    descripcion: r.descripcion,
    cantidad: r.cantidad,
    precio: r.precio,
    subtotal: r.subtotal,
  }));
};

const mapRow = async (row: any): Promise<Factura> => ({
  id: row.id,
  numero: row.numero,
  clienteId: row.cliente_id,
  clienteNombre: row.cliente_nombre,
  empresaId: row.empresa_id,
  fecha: row.fecha,
  vencimiento: row.vencimiento,
  subtotal: row.subtotal,
  descuento: row.descuento,
  impuesto: row.impuesto,
  total: row.total,
  estado: row.estado,
  notas: row.notas,
  items: await mapItems(row.id),
  createdAt: row.created_at,
});

export const facturaRepository = {
  getAll: async (): Promise<Factura[]> => {
    const db = getDatabase();
    const result = await db.execute('SELECT * FROM facturas ORDER BY created_at DESC;');
    const rows = result.rows ?? [];
    return Promise.all(rows.map(mapRow));
  },

  getById: async (id: number): Promise<Factura | null> => {
    const db = getDatabase();
    const result = await db.execute('SELECT * FROM facturas WHERE id=?;', [id]);
    if (!result.rows || result.rows.length === 0) return null;
    return mapRow(result.rows[0]);
  },

  getNextNumero: async (prefijo: string): Promise<string> => {
    const db = getDatabase();
    const result = await db.execute('SELECT COUNT(*) as total FROM facturas;');
    const total = ((result.rows?.[0] as any)?.total ?? 0) + 1;
    return `${prefijo}${String(total).padStart(6, '0')}`;
  },

  create: async (f: Omit<Factura, 'id' | 'createdAt'>): Promise<number> => {
    const db = getDatabase();
    const res = await db.execute(
      `INSERT INTO facturas (numero,cliente_id,cliente_nombre,empresa_id,fecha,vencimiento,
       subtotal,descuento,impuesto,total,estado,notas)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?);`,
      [f.numero, f.clienteId ?? null, f.clienteNombre ?? null, f.empresaId,
       f.fecha, f.vencimiento ?? null, f.subtotal, f.descuento, f.impuesto,
       f.total, f.estado, f.notas ?? null],
    );
    const facturaId = res.insertId as number;
    for (const item of f.items) {
      await db.execute(
        'INSERT INTO factura_items (factura_id,producto_id,nombre,descripcion,cantidad,precio,subtotal) VALUES (?,?,?,?,?,?,?);',
        [facturaId, item.productoId ?? null, item.nombre, item.descripcion ?? null,
         item.cantidad, item.precio, item.subtotal],
      );
    }
    return facturaId;
  },

  updateEstado: async (id: number, estado: Factura['estado']): Promise<void> => {
    const db = getDatabase();
    await db.execute('UPDATE facturas SET estado=? WHERE id=?;', [estado, id]);
  },

  delete: async (id: number): Promise<void> => {
    const db = getDatabase();
    await db.execute('DELETE FROM facturas WHERE id=?;', [id]);
  },
};

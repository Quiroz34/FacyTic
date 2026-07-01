import { getDatabase } from '../index';
import { Ticket, Item } from '../../types';

const mapItems = async (ticketId: number): Promise<Item[]> => {
  const db = getDatabase();
  const result = await db.execute('SELECT * FROM ticket_items WHERE ticket_id=?;', [ticketId]);
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

const mapRow = async (row: any): Promise<Ticket> => ({
  id: row.id,
  numero: row.numero,
  empresaId: row.empresa_id,
  fecha: row.fecha,
  total: row.total,
  notas: row.notas,
  items: await mapItems(row.id),
  createdAt: row.created_at,
});

export const ticketRepository = {
  getAll: async (): Promise<Ticket[]> => {
    const db = getDatabase();
    const result = await db.execute('SELECT * FROM tickets ORDER BY created_at DESC;');
    const rows = result.rows ?? [];
    return Promise.all(rows.map(mapRow));
  },

  getById: async (id: number): Promise<Ticket | null> => {
    const db = getDatabase();
    const result = await db.execute('SELECT * FROM tickets WHERE id=?;', [id]);
    if (!result.rows || result.rows.length === 0) return null;
    return mapRow(result.rows[0]);
  },

  getNextNumero: async (prefijo: string): Promise<string> => {
    const db = getDatabase();
    const result = await db.execute('SELECT COUNT(*) as total FROM tickets;');
    const total = ((result.rows?.[0] as any)?.total ?? 0) + 1;
    return `${prefijo}${String(total).padStart(6, '0')}`;
  },

  create: async (t: Omit<Ticket, 'id' | 'createdAt'>): Promise<number> => {
    const db = getDatabase();
    const res = await db.execute(
      'INSERT INTO tickets (numero,empresa_id,fecha,total,notas) VALUES (?,?,?,?,?);',
      [t.numero, t.empresaId, t.fecha, t.total, t.notas ?? null],
    );
    const ticketId = res.insertId as number;
    for (const item of t.items) {
      await db.execute(
        'INSERT INTO ticket_items (ticket_id,producto_id,nombre,descripcion,cantidad,precio,subtotal) VALUES (?,?,?,?,?,?,?);',
        [ticketId, item.productoId ?? null, item.nombre, item.descripcion ?? null,
         item.cantidad, item.precio, item.subtotal],
      );
    }
    return ticketId;
  },

  delete: async (id: number): Promise<void> => {
    const db = getDatabase();
    await db.execute('DELETE FROM tickets WHERE id=?;', [id]);
  },
};

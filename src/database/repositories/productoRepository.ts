import { getDatabase } from '../index';
import { Producto } from '../../types';

const mapRow = (row: any): Producto => ({
  id: row.id,
  nombre: row.nombre,
  descripcion: row.descripcion,
  precio: row.precio,
  categoriaId: row.categoria_id,
  imagen: row.imagen,
  activo: row.activo,
  createdAt: row.created_at,
});

export const productoRepository = {
  getAll: async (soloActivos = true): Promise<Producto[]> => {
    const db = getDatabase();
    const query = soloActivos
      ? 'SELECT * FROM productos WHERE activo=1 ORDER BY nombre;'
      : 'SELECT * FROM productos ORDER BY nombre;';
    const result = await db.execute(query);
    return (result.rows ?? []).map(mapRow);
  },

  getById: async (id: number): Promise<Producto | null> => {
    const db = getDatabase();
    const result = await db.execute('SELECT * FROM productos WHERE id=?;', [id]);
    if (!result.rows || result.rows.length === 0) return null;
    return mapRow(result.rows[0]);
  },

  create: async (p: Omit<Producto, 'id' | 'createdAt'>): Promise<number> => {
    const db = getDatabase();
    const result = await db.execute(
      'INSERT INTO productos (nombre,descripcion,precio,categoria_id,imagen,activo) VALUES (?,?,?,?,?,?);',
      [p.nombre, p.descripcion ?? null, p.precio, p.categoriaId ?? null, p.imagen ?? null, p.activo],
    );
    return result.insertId as number;
  },

  update: async (id: number, p: Partial<Omit<Producto, 'id' | 'createdAt'>>): Promise<void> => {
    const db = getDatabase();
    await db.execute(
      `UPDATE productos SET nombre=COALESCE(?,nombre), descripcion=COALESCE(?,descripcion),
       precio=COALESCE(?,precio), categoria_id=COALESCE(?,categoria_id),
       imagen=COALESCE(?,imagen), activo=COALESCE(?,activo) WHERE id=?;`,
      [p.nombre ?? null, p.descripcion ?? null, p.precio ?? null,
       p.categoriaId ?? null, p.imagen ?? null, p.activo ?? null, id],
    );
  },

  delete: async (id: number): Promise<void> => {
    const db = getDatabase();
    await db.execute('UPDATE productos SET activo=0 WHERE id=?;', [id]);
  },
};

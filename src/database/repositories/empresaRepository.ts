import { getDatabase } from '../index';
import { Empresa } from '../../types';

export const empresaRepository = {
  get: async (): Promise<Empresa | null> => {
    const db = getDatabase();
    const result = await db.execute('SELECT * FROM empresa LIMIT 1;');
    if (!result.rows || result.rows.length === 0) return null;
    const row = result.rows[0] as any;
    return {
      id: row.id,
      nombre: row.nombre,
      slogan: row.slogan,
      logo: row.logo,
      direccion: row.direccion,
      ciudad: row.ciudad,
      telefono: row.telefono,
      celular: row.celular,
      correo: row.correo,
      sitioWeb: row.sitio_web,
      ruc: row.ruc,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  },

  save: async (empresa: Omit<Empresa, 'id' | 'createdAt' | 'updatedAt'>): Promise<void> => {
    const db = getDatabase();
    const existing = await empresaRepository.get();
    if (existing) {
      await db.execute(
        `UPDATE empresa SET nombre=?, slogan=?, logo=?, direccion=?, ciudad=?,
         telefono=?, celular=?, correo=?, sitio_web=?, ruc=?,
         updated_at=datetime('now') WHERE id=?;`,
        [empresa.nombre, empresa.slogan ?? null, empresa.logo ?? null,
         empresa.direccion ?? null, empresa.ciudad ?? null,
         empresa.telefono ?? null, empresa.celular ?? null,
         empresa.correo ?? null, empresa.sitioWeb ?? null,
         empresa.ruc ?? null, existing.id],
      );
    } else {
      await db.execute(
        `INSERT INTO empresa (nombre,slogan,logo,direccion,ciudad,telefono,celular,correo,sitio_web,ruc)
         VALUES (?,?,?,?,?,?,?,?,?,?);`,
        [empresa.nombre, empresa.slogan ?? null, empresa.logo ?? null,
         empresa.direccion ?? null, empresa.ciudad ?? null,
         empresa.telefono ?? null, empresa.celular ?? null,
         empresa.correo ?? null, empresa.sitioWeb ?? null,
         empresa.ruc ?? null],
      );
    }
  },
};

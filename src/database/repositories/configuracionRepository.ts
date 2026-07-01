import { getDatabase } from '../index';
import { Configuracion } from '../../types';

const mapRow = (row: any): Configuracion => ({
  moneda: row.moneda,
  simboloMoneda: row.simbolo_moneda,
  impuesto: row.impuesto,
  impuestoNombre: row.impuesto_nombre,
  prefijoFactura: row.prefijo_factura,
  prefijoTicket: row.prefijo_ticket,
  colorPrimario: row.color_primario,
  colorSecundario: row.color_secundario,
  correoSmtp: row.correo_smtp,
  correoUsuario: row.correo_usuario,
  correoPassword: row.correo_password,
});

export const configuracionRepository = {
  get: async (): Promise<Configuracion> => {
    const db = getDatabase();
    const result = await db.execute('SELECT * FROM configuracion WHERE id=1;');
    return mapRow((result.rows ?? [])[0] ?? {});
  },

  update: async (c: Partial<Configuracion>): Promise<void> => {
    const db = getDatabase();
    await db.execute(
      `UPDATE configuracion SET
        moneda=COALESCE(?,moneda),
        simbolo_moneda=COALESCE(?,simbolo_moneda),
        impuesto=COALESCE(?,impuesto),
        impuesto_nombre=COALESCE(?,impuesto_nombre),
        prefijo_factura=COALESCE(?,prefijo_factura),
        prefijo_ticket=COALESCE(?,prefijo_ticket),
        color_primario=COALESCE(?,color_primario),
        color_secundario=COALESCE(?,color_secundario),
        correo_smtp=COALESCE(?,correo_smtp),
        correo_usuario=COALESCE(?,correo_usuario),
        correo_password=COALESCE(?,correo_password)
       WHERE id=1;`,
      [c.moneda ?? null, c.simboloMoneda ?? null, c.impuesto ?? null,
       c.impuestoNombre ?? null, c.prefijoFactura ?? null, c.prefijoTicket ?? null,
       c.colorPrimario ?? null, c.colorSecundario ?? null,
       c.correoSmtp ?? null, c.correoUsuario ?? null, c.correoPassword ?? null],
    );
  },
};

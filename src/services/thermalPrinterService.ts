import ThermalPrinterModule from 'react-native-thermal-receipt-printer-image-qr';
import { Ticket, Factura, Empresa, Configuracion } from '../types';

// ─── Tipos ────────────────────────────────────────────────────────────────────

export interface BluetoothDevice {
  macAddress: string;
  deviceName: string;
}

export type AnchoImpresora = 58 | 80;

// Caracteres por línea según ancho de papel
const CHARS_POR_LINEA: Record<AnchoImpresora, number> = {
  58: 32,
  80: 48,
};

// ─── Helpers de formato ESC/POS ───────────────────────────────────────────────

const separador = (ancho: AnchoImpresora, char = '=') =>
  `[C]${char.repeat(CHARS_POR_LINEA[ancho] - 2)}\n`;

const linea = (ancho: AnchoImpresora) => separador(ancho, '-');

/** Genera una fila con texto izquierdo y derecho alineados en el ancho total */
const filaLR = (izq: string, der: string, ancho: AnchoImpresora): string => {
  const total = CHARS_POR_LINEA[ancho];
  const espacios = Math.max(1, total - izq.length - der.length);
  return `[L]${izq}${' '.repeat(espacios)}${der}\n`;
};

// ─── Formateadores de contenido ───────────────────────────────────────────────

const formatTicketESCPOS = (
  ticket: Ticket,
  empresa: Empresa | null,
  config: Configuracion | null,
  ancho: AnchoImpresora,
): string => {
  const sym = config?.simboloMoneda ?? '$';
  let c = '';

  // Encabezado
  c += `[C]<b><font size='big'>${empresa?.nombre ?? 'MI NEGOCIO'}</font></b>\n`;
  if (empresa?.slogan)   c += `[C]${empresa.slogan}\n`;
  if (empresa?.direccion) c += `[C]${empresa.direccion}\n`;
  if (empresa?.telefono)  c += `[C]Tel: ${empresa.telefono}\n`;
  if (empresa?.ruc)       c += `[C]RUC: ${empresa.ruc}\n`;

  c += separador(ancho);
  c += filaLR('TICKET:', ticket.numero, ancho);
  c += filaLR('FECHA: ', ticket.fecha,  ancho);
  c += linea(ancho);

  // Items
  for (const item of ticket.items) {
    const nombreCorto = item.nombre.slice(0, CHARS_POR_LINEA[ancho] - 2);
    c += `[L]${nombreCorto}\n`;
    c += filaLR(
      `  ${item.cantidad}x ${sym}${item.precio.toFixed(2)}`,
      `${sym}${item.subtotal.toFixed(2)}`,
      ancho,
    );
  }

  // Total
  c += separador(ancho);
  c += filaLR('<b>TOTAL</b>', `<b>${sym}${ticket.total.toFixed(2)}</b>`, ancho);
  c += separador(ancho);

  // Pie
  if (ticket.notas) c += `[C]${ticket.notas}\n`;
  c += `[C]¡Gracias por su compra!\n`;
  c += '\n\n\n'; // espacio para corte

  return c;
};

const formatFacturaESCPOS = (
  factura: Factura,
  empresa: Empresa | null,
  config: Configuracion | null,
  ancho: AnchoImpresora,
): string => {
  const sym = config?.simboloMoneda ?? '$';
  let c = '';

  // Encabezado
  c += `[C]<b><font size='big'>${empresa?.nombre ?? 'MI NEGOCIO'}</font></b>\n`;
  if (empresa?.slogan)    c += `[C]${empresa.slogan}\n`;
  if (empresa?.direccion) c += `[C]${empresa.direccion}\n`;
  if (empresa?.telefono)  c += `[C]Tel: ${empresa.telefono}\n`;
  if (empresa?.ruc)       c += `[C]RUC: ${empresa.ruc}\n`;

  c += separador(ancho);
  c += `[C]<b>FACTURA</b>\n`;
  c += filaLR('No.:', factura.numero, ancho);
  c += filaLR('Fecha:', factura.fecha, ancho);
  c += filaLR('Estado:', factura.estado.toUpperCase(), ancho);
  c += linea(ancho);
  c += filaLR('Cliente:', factura.clienteNombre ?? 'Consumidor Final', ancho);
  c += linea(ancho);

  // Items
  for (const item of factura.items) {
    const nombreCorto = item.nombre.slice(0, CHARS_POR_LINEA[ancho] - 2);
    c += `[L]${nombreCorto}\n`;
    c += filaLR(
      `  ${item.cantidad}x ${sym}${item.precio.toFixed(2)}`,
      `${sym}${item.subtotal.toFixed(2)}`,
      ancho,
    );
  }

  // Totales
  c += separador(ancho);
  c += filaLR('Subtotal:', `${sym}${factura.subtotal.toFixed(2)}`, ancho);
  if (factura.impuesto > 0) {
    const nombreImp = config?.impuestoNombre ?? 'IVA';
    const pctImp = config?.impuesto ?? 0;
    c += filaLR(`${nombreImp} (${pctImp}%):`, `${sym}${factura.impuesto.toFixed(2)}`, ancho);
  }
  c += separador(ancho);
  c += filaLR('<b>TOTAL</b>', `<b>${sym}${factura.total.toFixed(2)}</b>`, ancho);
  c += separador(ancho);

  if (factura.notas) c += `[C]${factura.notas}\n`;
  c += '\n\n\n';

  return c;
};

// ─── API pública ──────────────────────────────────────────────────────────────

export const thermalPrinterService = {

  /** Lista impresoras Bluetooth emparejadas */
  obtenerDispositivos: async (): Promise<BluetoothDevice[]> => {
    const lista = await ThermalPrinterModule.getDeviceList();
    return (lista ?? []) as BluetoothDevice[];
  },

  /** Imprime un ticket */
  imprimirTicket: async (
    macAddress: string,
    ticket: Ticket,
    empresa: Empresa | null,
    config: Configuracion | null,
    ancho: AnchoImpresora = 58,
  ): Promise<void> => {
    const payload = formatTicketESCPOS(ticket, empresa, config, ancho);
    await ThermalPrinterModule.printBluetooth({
      payload,
      printerNbrCharactersPerLine: CHARS_POR_LINEA[ancho],
      printerDpi: 203,
      printerWidthMM: ancho,
      macAddress,
    });
  },

  /** Imprime una factura */
  imprimirFactura: async (
    macAddress: string,
    factura: Factura,
    empresa: Empresa | null,
    config: Configuracion | null,
    ancho: AnchoImpresora = 58,
  ): Promise<void> => {
    const payload = formatFacturaESCPOS(factura, empresa, config, ancho);
    await ThermalPrinterModule.printBluetooth({
      payload,
      printerNbrCharactersPerLine: CHARS_POR_LINEA[ancho],
      printerDpi: 203,
      printerWidthMM: ancho,
      macAddress,
    });
  },
};

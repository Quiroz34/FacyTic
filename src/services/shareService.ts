import { Linking } from 'react-native';
import Share from 'react-native-share';
import { pdfService } from './pdfService';
import { Factura, Ticket, Empresa, Configuracion } from '../types';

// ─── Formateadores de texto (se mantienen para compatibilidad o tickets) ─────────────────

const linea = (char = '─', n = 36) => char.repeat(n);

export const formatFacturaTexto = (
  factura: Factura,
  empresa: Empresa | null,
  config: Configuracion | null,
): string => {
  // ... (mantenemos el código anterior por si acaso)
  const sym = config?.simboloMoneda ?? '$';
  const items = factura.items
    .map(i => {
      const qty  = `${i.cantidad}x ${i.nombre}`;
      const amt  = `${sym}${i.subtotal.toFixed(2)}`;
      const pad  = Math.max(1, 36 - qty.length - amt.length);
      return `${qty}${' '.repeat(pad)}${amt}`;
    })
    .join('\n');

  return `
${linea('═')}
  ${empresa?.nombre?.toUpperCase() ?? 'MI NEGOCIO'}
${empresa?.slogan ? `  ${empresa.slogan}` : ''}
${empresa?.ruc ? `  RUC/NIT: ${empresa.ruc}` : ''}
${empresa?.direccion ? `  ${empresa.direccion}` : ''}
${empresa?.telefono ? `  Tel: ${empresa.telefono}` : ''}
${linea('─')}
  FACTURA ${factura.numero}
  Fecha:   ${factura.fecha}
  Cliente: ${factura.clienteNombre ?? 'Consumidor Final'}
  Estado:  ${factura.estado.toUpperCase()}
${linea('─')}
${items}
${linea('─')}
  Subtotal:             ${sym}${factura.subtotal.toFixed(2)}
${factura.impuesto > 0 ? `  ${config?.impuestoNombre ?? 'IVA'} (${config?.impuesto ?? 0}%):  ${sym}${factura.impuesto.toFixed(2)}\n` : ''}  TOTAL:                ${sym}${factura.total.toFixed(2)}
${linea('═')}
`.trim();
};

export const formatTicketTexto = (
  ticket: Ticket,
  empresa: Empresa | null,
  config: Configuracion | null,
): string => {
  const sym = config?.simboloMoneda ?? '$';
  const items = ticket.items
    .map(i => {
      const qty = `${i.cantidad}x ${i.nombre}`;
      const amt = `${sym}${i.subtotal.toFixed(2)}`;
      const pad = Math.max(1, 32 - qty.length - amt.length);
      return `${qty}${' '.repeat(pad)}${amt}`;
    })
    .join('\n');

  return `
${linea('═', 32)}
  ${empresa?.nombre?.toUpperCase() ?? 'MI NEGOCIO'}
${empresa?.direccion ? `  ${empresa.direccion}` : ''}
${empresa?.telefono ? `  Tel: ${empresa.telefono}` : ''}
${linea('─', 32)}
  TICKET  ${ticket.numero}
  Fecha:  ${ticket.fecha}
${linea('─', 32)}
${items}
${linea('─', 32)}
  TOTAL:        ${sym}${ticket.total.toFixed(2)}
${linea('═', 32)}
  ¡Gracias por su compra!
`.trim();
};

// ─── API pública ──────────────────────────────────────────────────────────────

export const shareService = {
  // ── Factura (PDF) ─────────────────────────────────────────────────────────

  facturaWhatsApp: async (
    factura: Factura,
    empresa: Empresa | null,
    config: Configuracion | null,
  ) => {
    try {
      const filePath = await pdfService.generateFacturaFile(factura, empresa, config);
      await Share.shareSingle({
        url: `file://${filePath}`,
        type: 'application/pdf',
        social: Share.Social.WHATSAPP as any,
      });
    } catch {
      const filePath = await pdfService.generateFacturaFile(factura, empresa, config);
      await Share.open({
        url: `file://${filePath}`,
        type: 'application/pdf',
        failOnCancel: false,
      });
    }
  },

  facturaCorreo: async (
    factura: Factura,
    empresa: Empresa | null,
    config: Configuracion | null,
    para = '',
  ) => {
    try {
      const filePath = await pdfService.generateFacturaFile(factura, empresa, config);
      await Share.open({
        url: `file://${filePath}`,
        type: 'application/pdf',
        subject: `Factura ${factura.numero} - ${empresa?.nombre ?? 'Mi Negocio'}`,
        failOnCancel: false,
      });
    } catch {
      const asunto = `Factura ${factura.numero} - ${empresa?.nombre ?? 'Mi Negocio'}`;
      const cuerpo = formatFacturaTexto(factura, empresa, config);
      await Linking.openURL(`mailto:${para}?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`);
    }
  },

  facturaCompartir: async (
    factura: Factura,
    empresa: Empresa | null,
    config: Configuracion | null,
  ) => {
    try {
      const filePath = await pdfService.generateFacturaFile(factura, empresa, config);
      await Share.open({
        title: `Factura ${factura.numero}`,
        url: `file://${filePath}`,
        type: 'application/pdf',
        failOnCancel: false,
      });
    } catch (e) {
      console.warn('Error sharing factura PDF:', e);
    }
  },

  // ── Ticket (PDF) ─────────────────────────────────────────────────────────

  ticketWhatsApp: async (
    ticket: Ticket,
    empresa: Empresa | null,
    config: Configuracion | null,
  ) => {
    try {
      const filePath = await pdfService.generateTicketFile(ticket, empresa, config);
      await Share.shareSingle({
        url: `file://${filePath}`,
        type: 'application/pdf',
        social: Share.Social.WHATSAPP as any,
      });
    } catch {
      const filePath = await pdfService.generateTicketFile(ticket, empresa, config);
      await Share.open({
        url: `file://${filePath}`,
        type: 'application/pdf',
        failOnCancel: false,
      });
    }
  },

  ticketCorreo: async (
    ticket: Ticket,
    empresa: Empresa | null,
    config: Configuracion | null,
    para = '',
  ) => {
    try {
      const filePath = await pdfService.generateTicketFile(ticket, empresa, config);
      await Share.open({
        url: `file://${filePath}`,
        type: 'application/pdf',
        subject: `Ticket ${ticket.numero} - ${empresa?.nombre ?? 'Mi Negocio'}`,
        failOnCancel: false,
      });
    } catch {
      const asunto = `Ticket ${ticket.numero} - ${empresa?.nombre ?? 'Mi Negocio'}`;
      const cuerpo = formatTicketTexto(ticket, empresa, config);
      const url = `mailto:${para}?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`;
      await Linking.openURL(url);
    }
  },

  ticketCompartir: async (
    ticket: Ticket,
    empresa: Empresa | null,
    config: Configuracion | null,
  ) => {
    try {
      const filePath = await pdfService.generateTicketFile(ticket, empresa, config);
      await Share.open({
        title: `Ticket ${ticket.numero}`,
        url: `file://${filePath}`,
        type: 'application/pdf',
        failOnCancel: false,
      });
    } catch {
      const texto = formatTicketTexto(ticket, empresa, config);
      await Share.open({
        title: `Ticket ${ticket.numero}`,
        message: texto,
        failOnCancel: false,
      });
    }
  },
};

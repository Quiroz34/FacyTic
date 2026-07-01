import RNPrint from 'react-native-print';
const RNHTMLtoPDF = require('react-native-html-to-pdf');
import RNFS from 'react-native-fs';
import { Factura, Ticket, Empresa, Configuracion } from '../types';

const getBase64Logo = async (logoPath: string | undefined): Promise<string> => {
  if (!logoPath) return '';
  try {
    const exists = await RNFS.exists(logoPath);
    if (!exists) return '';
    const base64 = await RNFS.readFile(logoPath, 'base64');
    return `data:image/png;base64,${base64}`;
  } catch (e) {
    console.warn('Error reading logo for PDF:', e);
    return '';
  }
};

const getFacturaHTML = (factura: Factura, empresa: Empresa | null, config: Configuracion | null, logoBase64: string) => {
  const primaryColor = config?.colorPrimario || '#6C63FF';
  const symbol = config?.simboloMoneda || '$';
  
  return `
    <html>
      <head>
        <meta charset="utf-8">
        <style>
          @page { margin: 15mm; }
          body { 
            font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; 
            color: #1a1a1a; margin: 0; padding: 0; line-height: 1.4;
            background-color: #fff;
          }
          .main-container { position: relative; }
          
          /* Header: Luxury look with accent bar */
          .header-accent {
            height: 8px;
            background: linear-gradient(90deg, ${primaryColor}, #4ecdc4);
            margin-bottom: 30px;
          }
          
          .header-content { display: table; width: 100%; margin-bottom: 40px; }
          .col-logo { display: table-cell; width: 50%; vertical-align: middle; }
          .col-doc-info { display: table-cell; width: 50%; vertical-align: middle; text-align: right; }
          
          .logo-img { max-height: 90px; max-width: 220px; filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1)); }
          
          .doc-type-label { 
            font-size: 32px; font-weight: 900; color: ${primaryColor}; 
            margin: 0; letter-spacing: -1px; text-transform: uppercase;
          }
          .doc-id { font-size: 18px; font-weight: 600; color: #444; margin: 2px 0; }
          
          /* Info Blocks */
          .info-grid { display: table; width: 100%; margin-bottom: 40px; border-spacing: 20px 0; margin-left: -20px; }
          .info-col { display: table-cell; width: 33%; vertical-align: top; }
          .main-container { padding: 40px; }
          
          /* Header: Super Premium Gradient and Layout */
          .top-bar {
            height: 12px;
            background: linear-gradient(90deg, ${primaryColor}, #00d2ff);
          }
          
          .header-box { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 50px; }
          .logo-area { flex: 1; }
          .logo-img { max-height: 100px; max-width: 250px; margin-bottom: 15px; }
          
          .meta-area { text-align: right; flex: 1; }
          .doc-title { 
            font-size: 42px; font-weight: 900; color: ${primaryColor}; 
            margin: 0; line-height: 1; letter-spacing: -2px;
          }
          .doc-sub { font-size: 20px; font-weight: 600; color: #555; margin-top: 5px; }
          
          /* Grids for Information */
          .data-grid { display: flex; gap: 40px; margin-bottom: 50px; }
          .data-col { flex: 1; }
          .label { 
            font-size: 11px; font-weight: 800; color: #aaa; 
            text-transform: uppercase; letter-spacing: 1.5px;
            margin-bottom: 10px; border-bottom: 2px solid #f0f0f0;
            padding-bottom: 5px;
          }
          .content { font-size: 14px; color: #333; line-height: 1.5; }
          .content b { color: #000; font-size: 16px; }
          
          /* Premium Table */
          table { width: 100%; border-collapse: collapse; margin-bottom: 50px; }
          th { 
            text-align: left; padding: 15px; font-size: 12px; font-weight: 700;
            color: #666; border-bottom: 2px solid ${primaryColor};
            background-color: #fafafa;
          }
          td { padding: 20px 15px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
          .td-desc { font-weight: 600; width: 50%; }
          .td-qty { text-align: center; width: 10%; color: #666; }
          .td-price { text-align: right; width: 20%; color: #666; }
          .td-total { text-align: right; font-weight: 700; width: 20%; color: #000; }
          
          /* Professional Totals Area */
          .summary-area { display: flex; justify-content: space-between; }
          .notes-box { flex: 1.5; padding-right: 60px; }
          .totals-box { flex: 1; }
          
          .row-total { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f9f9f9; }
          .row-total span:first-child { color: #888; font-weight: 600; }
          .row-total span:last-child { color: #333; font-weight: 700; }
          
          .grand-box { 
            margin-top: 20px; background: #f8f8fb; padding: 25px; 
            border-radius: 12px; border: 1px solid #e1e4ed;
            display: flex; justify-content: space-between; align-items: center;
          }
          .grand-label { font-size: 20px; font-weight: 900; color: #000; }
          .grand-val { font-size: 32px; font-weight: 900; color: ${primaryColor}; }
          
          .footer-premium { 
            margin-top: 100px; text-align: center; border-top: 1px solid #eee; 
            padding-top: 30px; font-size: 12px; color: #bbb;
          }
        </style>
      </head>
      <body>
        <div class="top-bar"></div>
        <div class="main-container">
          <div class="header-box">
            <div class="logo-area">
              ${logoBase64 ? `<img src="${logoBase64}" class="logo-img" />` : ''}
              <div class="content" style="margin-top: 10px;">
                <b>${empresa?.nombre || 'MI NEGOCIO'}</b><br>
                ${empresa?.ruc ? `RFC/NIT: ${empresa.ruc}<br>` : ''}
                ${empresa?.direccion || ''}<br>
                ${empresa?.telefono ? `Tel: ${empresa.telefono}` : ''}
              </div>
            </div>
            <div class="meta-area">
              <div class="doc-title">FACTURA</div>
              <div class="doc-sub">Nº ${factura.numero}</div>
              <div class="content" style="margin-top: 15px;">
                Fecha: <b>${factura.fecha}</b><br>
                Estado: <span style="font-weight: 800; color: ${factura.estado === 'pagada' ? '#27ae60' : '#f39c12'}">${factura.estado.toUpperCase()}</span>
              </div>
            </div>
          </div>

          <div class="data-grid">
            <div class="data-col">
              <div class="label">Cliente</div>
              <div class="content">
                <b>${factura.clienteNombre || 'Consumidor Final'}</b><br>
                ${factura.clienteDocumento ? `ID: ${factura.clienteDocumento}<br>` : ''}
                ${factura.clienteUbicacion || ''}
              </div>
            </div>
            <div class="data-col">
              <div class="label">Información de Pago</div>
              <div class="content">
                Método: Efectivo/Tarjeta<br>
                Vencimiento: ${factura.fecha}
              </div>
            </div>
          </div>

          <table>
            <thead>
              <tr>
                <th>DESCRIPCIÓN</th>
                <th style="text-align:center">CANT.</th>
                <th style="text-align:right">PRECIO UNIT.</th>
                <th style="text-align:right">SUBTOTAL</th>
              </tr>
            </thead>
            <tbody>
              ${factura.items.map(item => `
                <tr>
                  <td class="td-desc">${item.nombre}</td>
                  <td class="td-qty">${item.cantidad}</td>
                  <td class="td-price">${symbol}${item.precio.toFixed(2)}</td>
                  <td class="td-total">${symbol}${item.subtotal.toFixed(2)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>

          <div class="summary-area">
            <div class="notes-box">
              <div class="label">Notas y Términos</div>
              <div class="content" style="color:#666; font-style: italic;">
                ${factura.notas || 'Muchas gracias por su preferencia. Esta factura es un comprobante válido de su transacción.'}
              </div>
            </div>
            <div class="totals-box">
              <div class="row-total">
                <span>Subtotal</span>
                <span>${symbol}${factura.subtotal.toFixed(2)}</span>
              </div>
              <div class="row-total">
                <span>${config?.impuestoNombre || 'IVA'} (${config?.impuesto || 0}%)</span>
                <span>${symbol}${factura.impuesto.toFixed(2)}</span>
              </div>
              <div class="grand-box">
                <span class="grand-label">TOTAL</span>
                <span class="grand-val">${symbol}${factura.total.toFixed(2)}</span>
              </div>
            </div>
          </div>

          <div class="footer-premium">
            <b>Tikete & Factura</b> - Transformando su negocio digitalmente<br>
            ¡Gracias por su compra! Reclame su garantía con este documento.
          </div>
        </div>
      </body>
    </html>
  `;
};

const getTicketHTML = (ticket: Ticket, empresa: Empresa | null, config: Configuracion | null, logoBase64: string) => {
  const symbol = config?.simboloMoneda || '$';
  
  return `
    <html>
      <head>
        <meta charset="utf-8">
        <style>
          @page { margin: 5mm; }
          body { 
            font-family: 'Courier New', Courier, monospace; 
            color: #000; margin: 0; padding: 10px; 
            width: 80mm; font-size: 13px; line-height: 1.2;
          }
          .text-center { text-align: center; }
          .text-right { text-align: right; }
          .logo-small { max-height: 50px; margin-bottom: 10px; display: block; margin-left: auto; margin-right: auto; }
          .header-name { font-size: 18px; font-weight: bold; margin-bottom: 2px; }
          .divider { border-top: 1px dashed #000; margin: 10px 0; }
          .item-row { display: table; width: 100%; border-bottom: 1px dotted #ccc; padding: 4px 0; }
          .item-desc { display: table-cell; width: 70%; }
          .item-price { display: table-cell; width: 30%; text-align: right; font-weight: bold; }
          .total-box { margin-top: 15px; font-size: 18px; font-weight: 900; }
          .footer-msg { margin-top: 20px; font-size: 11px; }
        </style>
      </head>
      <body>
        <div class="text-center">
          ${logoBase64 ? `<img src="${logoBase64}" class="logo-small" />` : ''}
          <div class="header-name">${empresa?.nombre || 'MI NEGOCIO'}</div>
          ${empresa?.ruc ? `<div>RFC/NIT: ${empresa.ruc}</div>` : ''}
          <div>${empresa?.direccion || ''}</div>
          <div>${empresa?.telefono || ''}</div>
        </div>

        <div class="divider"></div>
        <div class="text-center"><b>TICKET DE VENTA</b></div>
        <div>Nº: ${ticket.numero}</div>
        <div>Fecha: ${ticket.fecha}</div>
        <div class="divider"></div>

        ${ticket.items.map(item => `
          <div class="item-row">
            <div class="item-desc">
              ${item.cantidad}x ${item.nombre}
            </div>
            <div class="item-price">${symbol}${item.subtotal.toFixed(2)}</div>
          </div>
        `).join('')}

        <div class="divider"></div>
        <div class="total-box text-right">
          TOTAL: ${symbol}${ticket.total.toFixed(2)}
        </div>
        
        <div class="footer-msg text-center">
          ¡GRACIAS POR SU PREFERENCIA!<br>
          ${ticket.notas ? `<br><i>${ticket.notas}</i>` : ''}
          <br><br>
          *** Tikete & Factura ***
        </div>
      </body>
    </html>
  `;
};

export const pdfService = {
  generateFacturaPDF: async (factura: Factura, empresa: Empresa | null, config: Configuracion | null) => {
    const logoBase64 = await getBase64Logo(empresa?.logo);
    const html = getFacturaHTML(factura, empresa, config, logoBase64);
    try {
      await RNPrint.print({ html });
    } catch (error) {
      console.error('Error generating PDF:', error);
      throw error;
    }
  },

  generateFacturaFile: async (factura: Factura, empresa: Empresa | null, config: Configuracion | null): Promise<string> => {
    const logoBase64 = await getBase64Logo(empresa?.logo);
    const html = getFacturaHTML(factura, empresa, config, logoBase64);
    try {
      const options = {
        html,
        fileName: `Factura_${factura.numero.replace(/\//g, '_')}`,
        directory: 'Documents',
      };
      const file = await RNHTMLtoPDF.convert(options);
      return file.filePath || '';
    } catch (error) {
      console.error('Error generating PDF file:', error);
      throw error;
    }
  },

  generateTicketPDF: async (ticket: Ticket, empresa: Empresa | null, config: Configuracion | null) => {
    const logoBase64 = await getBase64Logo(empresa?.logo);
    const html = getTicketHTML(ticket, empresa, config, logoBase64);
    try {
      await RNPrint.print({ html });
    } catch (error) {
      console.error('Error generating Ticket PDF:', error);
      throw error;
    }
  },

  generateTicketFile: async (ticket: Ticket, empresa: Empresa | null, config: Configuracion | null): Promise<string> => {
    const logoBase64 = await getBase64Logo(empresa?.logo);
    const html = getTicketHTML(ticket, empresa, config, logoBase64);
    try {
      const options = {
        html,
        fileName: `Ticket_${ticket.numero.replace(/\//g, '_')}`,
        directory: 'Documents',
      };
      const file = await RNHTMLtoPDF.convert(options);
      return file.filePath || '';
    } catch (error) {
      console.error('Error generating Ticket file:', error);
      throw error;
    }
  }
};

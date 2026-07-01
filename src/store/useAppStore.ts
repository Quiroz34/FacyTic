import { create } from 'zustand';
import { Empresa, Producto, Factura, Ticket, Configuracion } from '../types';
import { empresaRepository } from '../database/repositories/empresaRepository';
import { productoRepository } from '../database/repositories/productoRepository';
import { facturaRepository } from '../database/repositories/facturaRepository';
import { ticketRepository } from '../database/repositories/ticketRepository';
import { configuracionRepository } from '../database/repositories/configuracionRepository';

interface AppState {
  empresa: Empresa | null;
  productos: Producto[];
  facturas: Factura[];
  tickets: Ticket[];
  configuracion: Configuracion | null;
  loading: boolean;

  loadEmpresa: () => Promise<void>;
  saveEmpresa: (e: Omit<Empresa, 'id' | 'createdAt' | 'updatedAt'>) => Promise<void>;

  loadProductos: () => Promise<void>;
  createProducto: (p: Omit<Producto, 'id' | 'createdAt'>) => Promise<void>;
  updateProducto: (id: number, p: Partial<Omit<Producto, 'id' | 'createdAt'>>) => Promise<void>;
  deleteProducto: (id: number) => Promise<void>;

  loadFacturas: () => Promise<void>;
  createFactura: (f: Omit<Factura, 'id' | 'createdAt'>) => Promise<number>;
  updateEstadoFactura: (id: number, estado: Factura['estado']) => Promise<void>;
  deleteFactura: (id: number) => Promise<void>;

  loadTickets: () => Promise<void>;
  createTicket: (t: Omit<Ticket, 'id' | 'createdAt'>) => Promise<number>;
  deleteTicket: (id: number) => Promise<void>;

  loadConfiguracion: () => Promise<void>;
  updateConfiguracion: (c: Partial<Configuracion>) => Promise<void>;

  initAll: () => Promise<void>;
}

export const useAppStore = create<AppState>((set, get) => ({
  empresa: null,
  productos: [],
  facturas: [],
  tickets: [],
  configuracion: null,
  loading: false,

  loadEmpresa: async () => {
    try {
      const empresa = await empresaRepository.get();
      set({ empresa });
    } catch (e) {
      console.error('[Store] loadEmpresa:', e);
    }
  },

  saveEmpresa: async (e) => {
    await empresaRepository.save(e);
    await get().loadEmpresa();
  },

  loadProductos: async () => {
    try {
      const productos = await productoRepository.getAll();
      set({ productos });
    } catch (e) {
      console.error('[Store] loadProductos:', e);
    }
  },

  createProducto: async (p) => {
    await productoRepository.create(p);
    await get().loadProductos();
  },

  updateProducto: async (id, p) => {
    await productoRepository.update(id, p);
    await get().loadProductos();
  },

  deleteProducto: async (id) => {
    await productoRepository.delete(id);
    await get().loadProductos();
  },

  loadFacturas: async () => {
    try {
      const facturas = await facturaRepository.getAll();
      set({ facturas });
    } catch (e) {
      console.error('[Store] loadFacturas:', e);
    }
  },

  createFactura: async (f) => {
    const id = await facturaRepository.create(f);
    await get().loadFacturas();
    return id;
  },

  updateEstadoFactura: async (id, estado) => {
    await facturaRepository.updateEstado(id, estado);
    await get().loadFacturas();
  },

  deleteFactura: async (id) => {
    await facturaRepository.delete(id);
    await get().loadFacturas();
  },

  loadTickets: async () => {
    try {
      const tickets = await ticketRepository.getAll();
      set({ tickets });
    } catch (e) {
      console.error('[Store] loadTickets:', e);
    }
  },

  createTicket: async (t) => {
    const id = await ticketRepository.create(t);
    await get().loadTickets();
    return id;
  },

  deleteTicket: async (id) => {
    await ticketRepository.delete(id);
    await get().loadTickets();
  },

  loadConfiguracion: async () => {
    try {
      const configuracion = await configuracionRepository.get();
      set({ configuracion });
    } catch (e) {
      console.error('[Store] loadConfiguracion:', e);
    }
  },

  updateConfiguracion: async (c) => {
    await configuracionRepository.update(c);
    await get().loadConfiguracion();
  },

  initAll: async () => {
    set({ loading: true });
    try {
      await Promise.all([
        get().loadEmpresa(),
        get().loadProductos(),
        get().loadFacturas(),
        get().loadTickets(),
        get().loadConfiguracion(),
      ]);
    } finally {
      set({ loading: false });
    }
  },
}));

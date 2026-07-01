import RNPrint from 'react-native-print';

export const printerService = {
  printHTML: async (html: string) => {
    try {
      await RNPrint.print({ html });
    } catch (error) {
      console.error('Error printing:', error);
      throw error;
    }
  },

  selectPrinter: async () => {
    try {
      // Abre el diálogo del sistema para seleccionar impresora
      const printer = await RNPrint.selectPrinter({ x: '0', y: '0' });
      return printer;
    } catch (error) {
      console.error('Error selecting printer:', error);
    }
  }
};

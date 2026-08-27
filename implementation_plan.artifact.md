# Plan de Finalización: "App Total 100% Funcional"

Este plan transformará la aplicación de un prototipo a un producto final profesional, corrigiendo los botones inactivos y agregando las piezas que faltan para el usuario final.

## User Review Required

> [!IMPORTANT]
> Agregaremos un sistema de **Gráficos Reales** en el Dashboard, un sistema de **Compartir por WhatsApp/Email** real y un **Control de Inventario** avanzado.

## Proposed Changes

### 1. Dashboard Inteligente
*   **Gráfico de Ventas**: Implementar un gráfico de barras que muestre las ventas de los últimos 7 días.
*   **Botones de Acción**: Hacer que los botones "Vender", "Stock", "PDF" y "Enviar" funcionen como accesos directos reales.
*   **Resumen de Clientes**: Mostrar el número real de clientes registrados.

### 2. Control de Inventario Avanzado
*   **Vista de Stock**: Una nueva sección para ver rápidamente qué productos se están agotando y reponerlos.
*   **Movimientos**: Registrar cuándo se agregó stock manualmente.

### 3. Conectividad y Compartición (Real)
*   **Generador de Archivos**: Guardar el PDF en una carpeta temporal.
*   **Compartir Nativo**: Usar el menú de compartir de Android para enviar el ticket a cualquier app (WhatsApp, Gmail, etc.).
*   **Firma Digital**: Opción para agregar una firma o sello digital en la configuración.

### 4. Herramientas de Administrador
*   **Exportar a CSV/Excel**: Permitir al usuario descargar su historial de ventas en un archivo de Excel para su contabilidad.
*   **Borrado Total**: Opción de limpiar la base de datos para empezar de cero (con confirmación).

### 5. Pulido UX (Toque Final)
*   **Feedback Visual**: Añadir sonidos suaves al cobrar y animaciones de "Éxito".
*   **Validaciones**: Evitar que se vendan productos sin stock (opcional, configurable).

---

## Plan de Ejecución
1.  **Dashboard**: Implementar el gráfico de ventas y activar los accesos rápidos.
2.  **Módulo de Compartir**: Configurar `FileProvider` para que Android permita enviar los PDFs generados.
3.  **Inventario**: Añadir la gestión de stock bajo.
4.  **Exportación**: Crear la utilidad para generar archivos CSV de las ventas.

## Verification Plan
1.  **Venta Completa**: Realizar una venta -> Ver cómo sube el gráfico -> Compartir el ticket por WhatsApp -> Verificar que el stock bajó.
2.  **Exportación**: Descargar el CSV y abrirlo en el visor de archivos.

# Implementación completada: Créditos, Stock Avanzado y Descuentos

Este documento conserva el alcance histórico. Todas las fases descritas quedaron implementadas en la base de datos versión 7, incluida la migración conservadora desde la versión 5.

## User Review Required

> [!IMPORTANT]
> La base de datos está en la **Versión 7** y cuenta con migraciones verificadas 5 → 6 → 7.
>
> [!WARNING]
> La opción de **"Vender al Fiado" (Crédito)** aumentará automáticamente el saldo deudor del cliente seleccionado.

## Cambios implementados

### 1. Sistema de Créditos (Cuentas por Cobrar)
*   **Modelo `Cliente`**: Añadir campo `deuda: Double`.
*   **Punto de Venta**: Añadir método de pago "CRÉDITO". Al usarlo, la venta se marca como "Pendiente" y se suma al saldo del cliente.
*   **Gestión de Deuda**: En la pantalla de Clientes, poder ver quién debe y registrar abonos.

### 2. Control de Inventario con Historial (Kárdex)
*   **Nueva Entidad `InventarioMovimiento`**: Registrará fecha, producto, cantidad (+ o -) y motivo (Venta, Reposición, Ajuste).
*   **Dashboard**: Añadir un botón para ver el historial de movimientos de un producto.

### 3. Top de Productos más Vendidos
*   **Dashboard**: Implementar una lista visual de los 5 productos con más unidades vendidas. Esto dará una visión clara de qué es lo más popular.

### 4. Descuentos en Ventas
*   **Punto de Venta**: Añadir un campo de "Descuento" (Monto fijo). El total se recalculará restando este valor antes de aplicar impuestos.
*   **PDF**: Mostrar el descuento aplicado en el ticket final.

### 5. Firma Digital y Logo (Mejora)
*   Asegurar que la firma capturada se guarde como imagen y se incluya en el PDF generado.

---

## Ejecución completada
1.  **Fase 1: Estructura de Datos**: completada.
2.  **Fase 2: Lógica de Crédito**: completada.
3.  **Fase 3: UI de Venta**: completada.
4.  **Fase 4: Dashboard Pro**: completada.
5.  **Fase 5: Documentos**: completada.

## Verificación automatizada
1.  **Venta a Crédito**: Cliente A tiene deuda $0 -> Venta crédito $50 -> Cliente A ahora debe $50.
2.  **Kárdex**: Vender 2 unidades -> El historial debe mostrar "-2 unidades (Venta)".
3.  **Descuento**: Producto $100 -> Descuento $10 -> Total $90.

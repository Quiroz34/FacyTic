# Tikete y Factura

Aplicación Android de punto de venta con catálogo, inventario, clientes, crédito, abonos, gastos, caja y comprobantes PDF. Los datos se guardan localmente con Room.

## Abrir desde PowerShell

```powershell
cd "C:\proyectos\Tikete y factura"
.\dev.ps1
```

Por defecto, el script revisa la compilación del código y ejecuta las pruebas unitarias **sin generar APK**. Usa el Java incluido con Android Studio.

Cuando decidas abrir la aplicación, ejecuta ` .\dev.ps1 -Run`. Esto sí compila e instala la versión de desarrollo; inicia un emulador si hace falta. También puedes conectar un celular con depuración USB. Deja un solo dispositivo conectado. Para elegir emulador: ` .\dev.ps1 -Run -Avd Pixel_10`.

Para generar solamente el APK: ` .\dev.ps1 -Apk`. Queda en `app\build\outputs\apk\debug\app-debug.apk`.

## Flujos disponibles

- Ventas con cantidades decimales, búsqueda, lectura de código desde una foto de cámara, descuentos, impuesto configurable, efectivo/cambio, tarjeta, transferencia y crédito.
- Guardado transaccional de venta, partidas, inventario y deuda. Folios consecutivos y comprobante obtenido del registro guardado.
- Productos con precio, costo, código de barras, proveedor, unidad de venta (pieza, kg o L) y stock mínimo; kilos y litros admiten fracciones como 0.5; entradas y ajustes con motivo e historial.
- Clientes editables, estados de cuenta, abonos por comprobante o por antigüedad y recibos PDF.
- Cancelación/devolución completa con reposición de inventario y registro del reembolso. El reembolso en efectivo requiere caja abierta; los cierres anteriores se conservan.
- Apertura, cierre y diferencia de caja; cobros por método. Las operaciones sin caja abierta no se incluyen en un cierre.
- Reportes de hoy, últimos siete días, mes y total histórico. Se separan ventas vigentes, cobros, deuda, costos, compras de mercancía y gastos operativos. Las compras no se descuentan otra vez como gasto operativo al calcular el resultado con el costo vendido. El resultado es una estimación y depende de costos completos.
- Comprobantes A4, 58 y 80 mm con logo, firma opcional, descuento, impuesto, pago y saldo. Compartir PDF y exportar CSV con columnas escapadas.
- Impresión directa por Bluetooth clásico SPP para impresoras ESC/POS, selección entre dispositivos vinculados, permisos de Android 12+, prueba, 58/80 mm y conexión para modelos antiguos. Se envía el documento como imagen para conservar acentos, logo y firma. No se hacen reintentos automáticos que puedan duplicar un ticket.
- Pantalla de carga con «Desarrollada por QZ Web Solutions», icono de recibo y paleta con contraste mejorado.
- Respaldo completo JSON y restauración transaccional desde Ajustes. Antes de restaurar se conserva una copia local exportable.

## Datos existentes

Las migraciones de base de datos **5 → 6 → 7 → 8** conservan productos, clientes, documentos, partidas, configuración y gastos. La versión 8 añade la unidad de venta; los artículos existentes se mantienen como piezas. Se quitó la migración destructiva. La versión 5 es la instalación histórica más antigua recuperable del repositorio; no existe un esquema verificable de versiones 1–4 y por eso la app no arriesga datos intentando adivinarlo.

Las fechas antiguas no contienen año y aparecen únicamente en el período «Todo». La versión anterior guardaba los abonos como gastos negativos: se conservan, pero se excluyen de gastos y resultados. Los saldos se concilian con los comprobantes de crédito por antigüedad; no se inventan recibos ni fechas. Las devoluciones de ventas antiguas sin identificadores de producto o con abonos sin método conocido requieren conciliación manual. Los comprobantes nuevos conservan una copia de la identidad del negocio al emitirse.

El lector usa la cámara instalada para tomar una foto y reconocer el código; también admite escribir el código. La impresión Bluetooth requiere una impresora compatible con Bluetooth clásico SPP y raster ESC/POS GS v 0. Las impresoras BLE o de protocolo propietario necesitan identificar su modelo e implementar su protocolo. No se afirma compatibilidad con una impresora física sin realizar el ticket de prueba. La impresión mediante los servicios de Android también está disponible.

Referencias del controlador: [conexiones Bluetooth en Android](https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices), [permisos de Bluetooth](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions) y [comando raster ESC/POS](https://download4.epson.biz/sec_pubs/pos/reference_en/escpos/gs_lv_0.html).

## Verificación

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest --console=plain
```

Las pruebas unitarias cubren redondeo, importes inválidos, contactos, duplicados, caja, fechas, CSV y bytes ESC/POS. Las pruebas de integración cubren ventas concurrentes, rollback, créditos, caja, devoluciones, respaldo/restauración y migraciones desde las versiones 5 y 6. Hay una prueba de interfaz que recorre altas, edición, duplicados, inventario, crédito, abonos, devolución, gastos, caja y acceso a la configuración Bluetooth. Las pruebas en Android se ejecutan con ` .\gradlew.bat :app:connectedDebugAndroidTest`; este comando genera e instala APK de prueba.

La compilación `release` sin firma se genera con ` .\gradlew.bat :app:assembleRelease`. Para publicar en Play Store o distribuir una actualización se necesita la llave privada del propietario; no debe guardarse en el repositorio. La compatibilidad Bluetooth se valida finalmente con el botón de prueba y una impresora ESC/POS física de 58 u 80 mm.

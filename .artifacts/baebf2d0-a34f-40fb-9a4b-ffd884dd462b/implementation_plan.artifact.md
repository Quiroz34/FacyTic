# Plan de Migración a App Android Nativa (Kotlin + Compose)

Este plan detalla la reconstrucción completa de la aplicación "Tikete & Factura" de React Native a código nativo de Android. El objetivo es eliminar los problemas de rendimiento, errores de conexión con el servidor de desarrollo y garantizar una experiencia fluida y estable.

## User Review Required

> [!IMPORTANT]
> La migración implica eliminar la estructura de React Native (`node_modules`, `App.tsx`, `package.json`, etc.) y reemplazarla por un proyecto nativo estándar. Se mantendrán todos los datos de diseño y lógica de negocio.

## Proposed Changes

### 1. Configuración del Proyecto
*   **Lenguaje**: Kotlin 2.1.
*   **UI Framework**: Jetpack Compose (Material 3).
*   **Arquitectura**: MVVM con Clean Architecture.
*   **Inyección de Dependencias**: Hilt.

### 2. Capa de Datos (Room Persistence)
Reemplazaremos el SQLite manual por **Room** para mayor seguridad y velocidad.
*   **Entidades**: `Empresa`, `Producto`, `Cliente`, `Factura`, `Ticket`, `Configuracion`.
*   **DAOs**: Repositorios optimizados para cada entidad con soporte para `Flow`.

### 3. Interfaz de Usuario (Jetpack Compose)
Recrearemos el diseño "Luxury Dark" actual:
*   **Tema**: Colores personalizados (`#7B6EFA` para primario, `#080810` para fondo).
*   **Navegación**: Navigation Compose con 5 pestañas principales.
*   **Pantallas**:
    *   `HomeScreen`: Resumen de ventas y acciones rápidas.
    *   `TicketsScreen` / `FacturasScreen`: Listados con filtrado.
    *   `NuevoTicket` / `NuevaFactura`: Formularios dinámicos con selección de productos.
    *   `DetalleScreen`: Vista previa estilizada y opciones de compartir/imprimir.

### 4. Servicios Nativos
*   **PDF Service**: Generación de facturas y tickets usando `PrintDocumentAdapter` nativo.
*   **Sharing**: Integración con el menú de compartir de Android para WhatsApp y Correo.

---

## Estructura de Archivos (Nueva)
```text
app/src/main/java/com/tiketefactura/
├── data/
│   ├── local/ (Room DB, DAOs, Entities)
│   └── repository/ (Implementaciones)
├── domain/
│   ├── model/ (Data classes)
│   └── repository/ (Interfaces)
├── ui/
│   ├── components/ (Botones, Inputs, Cards)
│   ├── navigation/ (NavHost)
│   ├── screens/ (Home, Tickets, etc.)
│   └── theme/ (Color, Type, Theme)
└── util/ (PDF, Printing)
```

## Plan de Verificación

### Pruebas Automatizadas
*   Pruebas unitarias para los cálculos de impuestos y totales.
*   Pruebas de Room para asegurar la integridad de la base de datos.

### Manual Verification
*   Verificar que la app inicie en menos de 2 segundos.
*   Confirmar que el guardado de una factura sea instantáneo.
*   Generar un PDF de prueba y verificar que se visualice correctamente.

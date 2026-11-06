# Publicación en Google Play y RevenueCat

La aplicación está preparada con `compileSdk 36`, `targetSdk 36`, `versionCode 3`,
`versionName 1.2.0` y el paquete `com.tiketefactura.v2`.

## 1. Google Play Console

1. Crear o abrir la aplicación con el paquete exacto `com.tiketefactura.v2`.
2. Activar Play App Signing y conservar de forma segura la llave de carga.
3. En **Monetización > Productos > Suscripciones**, crear:
   - Producto `tikete_premium_monthly`, plan base auto-renovable mensual, precio México: **$85 MXN**. No crear una prueba gratuita en Google Play: la aplicación ya concede los 5 días de prueba y luego bloquea el acceso.
   - Producto `tikete_premium_annual`, plan base auto-renovable anual, precio México: **$900 MXN**.
4. Activar ambos productos y completar los precios para los demás países. Google Play
   mostrará el precio localizado; RevenueCat no fija el importe.
5. Crear una pista de prueba interna y agregar las cuentas de prueba de licencia.

No cambies los identificadores después de publicar: Google Play no permite reutilizar o
renombrar productos con libertad.

## 2. RevenueCat

1. Crear el proyecto y una app Android para `com.tiketefactura.v2`.
2. Conectar las credenciales de Google Play indicadas por RevenueCat.
3. Importar `tikete_premium_monthly` y `tikete_premium_annual`.
4. Crear el entitlement exacto `premium` y asociarle ambos productos.
5. Crear un offering `default`, agregar los paquetes `$rc_monthly` y `$rc_annual`, y
   marcarlo como Current.
6. Diseñar y publicar el paywall en español. Debe mostrar renovación automática,
   periodo, precio, restauración, términos y privacidad.
7. Copiar únicamente la **Public SDK Key de Google Play** (`goog_...`). Nunca usar una
   clave secreta dentro de la app.

Agregar las propiedades en `%USERPROFILE%\.gradle\gradle.properties` (no en Git):

```properties
REVENUECAT_GOOGLE_API_KEY=goog_xxxxxxxxxxxxxxxxxxxxxxxx
UPLOAD_STORE_FILE=C:/ruta/segura/tikete-upload.jks
UPLOAD_STORE_PASSWORD=...
UPLOAD_KEY_ALIAS=tikete-upload
UPLOAD_KEY_PASSWORD=...
```

## 3. Generar y comprobar el AAB

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean :app:testDebugUnitTest :app:bundleRelease --console=plain
jarsigner -verify -verbose -certs app\build\outputs\bundle\release\app-release.aab
```

El último comando debe terminar indicando que el JAR fue verificado. No subir un AAB
que diga `jar is unsigned`.

### AAB para testers internos (20 días gratis)

La app tiene 5 días gratis en producción. Para generar un AAB exclusivo para la pista
interna con 20 días, usa un código de versión superior y no promociones este artefacto a
producción:

```powershell
.\gradlew.bat clean :app:bundleRelease -PFREE_TRIAL_DAYS=20 -PVERSION_CODE=4 -PVERSION_NAME=1.2.0-tester --console=plain
```

Antes de generar el AAB de producción, vuelve a crear el bundle sin `FREE_TRIAL_DAYS`
y con un `VERSION_CODE` superior a 4. Así la versión pública conserva los 5 días.

## 4. Prueba obligatoria antes de producción

- Instalar desde la pista interna; una instalación directa por Android Studio no prueba
  correctamente la facturación real de Google Play.
- Comprar el mensual con una cuenta de prueba, verificar el entitlement `premium` en
  RevenueCat y restaurar la compra tras reinstalar.
- Repetir con el anual y comprobar cancelación, expiración y periodo de gracia.
- Verificar que el precio mostrado por Google Play sea $85 MXN y $900 MXN.
- Confirmar que, tras vencer los 5 días de prueba de la aplicación sin entitlement
  `premium`, la aplicación no permite ejecutar ninguna función y muestra los planes.
- Completar ficha, clasificación de contenido, público objetivo, seguridad de datos,
  acceso a la aplicación, anuncios y URL pública de privacidad.

## Pendientes que requieren las cuentas del propietario

- Clave pública de RevenueCat.
- Llave de carga y contraseñas.
- Creación/activación de productos en Play Console.
- Conexión Google Play–RevenueCat.
- URL pública de la política de privacidad y datos del desarrollador.

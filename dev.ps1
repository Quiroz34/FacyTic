param([switch]$Apk, [switch]$Run, [string]$Avd)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$studioJava = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'
if (Test-Path -LiteralPath (Join-Path $studioJava 'bin\java.exe')) { $env:JAVA_HOME = $studioJava }
$sdkRoot = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$env:ANDROID_HOME = $sdkRoot
$adbPath = Join-Path $sdkRoot 'platform-tools\adb.exe'
$emulatorPath = Join-Path $sdkRoot 'emulator\emulator.exe'
if ($Apk -and $Run) { throw 'Elige -Apk o -Run.' }
if (!$Apk -and !$Run) {
    & .\gradlew.bat :app:compileDebugKotlin :app:testDebugUnitTest --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Falló la revisión. Revisa el error anterior.' }
    Write-Host 'Revisión terminada sin generar APK. Usa -Run para abrirla o -Apk para generar el instalador cuando lo decidas.'
    exit 0
}
if ($Apk) {
    & .\gradlew.bat :app:assembleDebug --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación. Revisa el error anterior.' }
    Write-Host "APK: $PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk"
    exit 0
}
if (!(Test-Path -LiteralPath $adbPath)) { throw 'No se encontró el SDK de Android. Configura ANDROID_HOME.' }
$devices = @(& $adbPath devices | Where-Object { $_ -match '^\S+\s+device$' } | ForEach-Object { ($_ -split '\s+')[0] })
if ($devices.Count -eq 0) {
    if (!(Test-Path -LiteralPath $emulatorPath)) { throw 'Abre un emulador o conecta un celular con depuración USB.' }
    $availableAvds = @(& $emulatorPath -list-avds)
    if (!$Avd) { $Avd = $availableAvds | Select-Object -First 1 }
    if (!$Avd -or $Avd -notin $availableAvds) { throw 'No hay un emulador válido. Crea uno en Android Studio o indica -Avd NOMBRE.' }
    # Running this script requests an interactive emulator for viewing the app.
    # Los snapshots rápidos de Android 16 pueden reanudar System UI con presión de
    # memoria y provocar un ANR ajeno a la aplicación. Un arranque en frío conserva
    # los datos del AVD y ofrece una sesión estable para probar la interfaz.
    Start-Process -FilePath $emulatorPath -ArgumentList @('-avd', $Avd, '-no-snapshot-load')
    $deadline = (Get-Date).AddMinutes(3)
    do {
        Start-Sleep -Seconds 2
        $devices = @(& $adbPath devices | Where-Object { $_ -match '^\S+\s+device$' } | ForEach-Object { ($_ -split '\s+')[0] })
    } until ($devices.Count -gt 0 -or (Get-Date) -gt $deadline)
    if ($devices.Count -eq 0) { throw 'El emulador no terminó de iniciar. Intenta de nuevo cuando esté listo.' }
}
if ($devices.Count -ne 1) { throw 'Hay varios dispositivos conectados. Deja uno solo para elegir dónde abrir la app.' }
$env:ANDROID_SERIAL = $devices[0]
$bootDeadline = (Get-Date).AddMinutes(3)
while ((& $adbPath -s $devices[0] shell getprop sys.boot_completed) -ne '1') {
    if ((Get-Date) -gt $bootDeadline) { throw 'El dispositivo no terminó de iniciar.' }
    Start-Sleep -Seconds 2
}
& .\gradlew.bat :app:installDebug --console=plain
if ($LASTEXITCODE -ne 0) { throw 'No se pudo instalar la app de desarrollo.' }
& $adbPath -s $devices[0] shell am start -n com.tiketefactura.v2/com.tiketefactura.MainActivity
if ($LASTEXITCODE -ne 0) { throw 'No se pudo abrir la aplicación.' }

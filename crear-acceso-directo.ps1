# ============================================================
#  crear-acceso-directo.ps1
#  Crea un acceso directo (.lnk) en el Escritorio que apunta
#  directamente al ejecutable nativo ContadorDeGastos.exe
#  generado por jpackage. No requiere Java visible ni consola.
# ============================================================

$ProjectDir   = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ExePath      = Join-Path $ProjectDir "dist\ContadorDeGastos\ContadorDeGastos.exe"
$DesktopPath  = [Environment]::GetFolderPath("Desktop")
$ShortcutPath = Join-Path $DesktopPath "Contador de Gastos.lnk"

# Verificar que el exe existe
if (-not (Test-Path $ExePath)) {
    $msg = "No se encontro el ejecutable en:`n$ExePath`n`n" +
           "Solucion: Ejecuta primero 'generar-instalador-exe.bat' para crear el ejecutable."
    [System.Windows.Forms.MessageBox]::Show($msg, "Contador de Gastos - Error",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Error) | Out-Null
    Write-Host "[ERROR] $msg" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

# Crear el acceso directo
$WScriptShell = New-Object -ComObject WScript.Shell
$Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)

# Target: el exe nativo directamente (sin wscript, sin bat, sin consola)
$Shortcut.TargetPath       = $ExePath
$Shortcut.Arguments        = ""
# WorkingDirectory CRITICO: debe ser la raiz del proyecto
# para que config.properties y contador_gastos.db se encuentren
$Shortcut.WorkingDirectory = $ProjectDir
$Shortcut.WindowStyle      = 1
$Shortcut.Description      = "Contador de Gastos - Gestor Financiero Personal"
$Shortcut.IconLocation     = "$ExePath,0"
$Shortcut.Save()

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Acceso directo creado exitosamente!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Ubicacion: $ShortcutPath" -ForegroundColor White
Write-Host "  Apunta a:  $ExePath" -ForegroundColor White
Write-Host ""
Write-Host "  Haz doble clic en el icono del Escritorio para iniciar." -ForegroundColor Yellow
Write-Host ""
Read-Host "Presiona Enter para cerrar"

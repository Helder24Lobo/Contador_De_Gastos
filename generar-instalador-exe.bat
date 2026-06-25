@echo off
:: ============================================================
::  Contador de Gastos - GENERAR EJECUTABLE con jpackage
::  Alternativa D: Crea una aplicación portable y/o instalador
::
::  MODO 1 (app-image): Carpeta portable ejecutable - NO requiere
::                      WiX Toolset. Siempre funciona.
::  MODO 2 (exe):       Instalador .exe - REQUIERE WiX Toolset v3
::                      Descarga: https://github.com/wixtoolset/wix3/releases
::
::  Resultado: dist\ContadorDeGastos\ con ContadorDeGastos.exe
:: ============================================================
setlocal EnableDelayedExpansion

title Generando Ejecutable - Contador de Gastos

:: --- Obtener directorio del script (raíz del proyecto) ---
set "PROJECT_DIR=%~dp0"
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

:: --- Rutas del JDK ---
set "JDK_HOME=C:\Program Files\Java\jdk-21.0.10"
set "JPACKAGE=%JDK_HOME%\bin\jpackage.exe"

:: --- Rutas del proyecto ---
set "MVN_CMD=%PROJECT_DIR%\apache-maven-3.9.6\bin\mvn.cmd"
set "JAR_FILE=%PROJECT_DIR%\target\Contador_De_Gastos-1.0-SNAPSHOT.jar"
set "JPACKAGE_INPUT=%PROJECT_DIR%\target\jpackage-input"
set "JPACKAGE_OUTPUT=%PROJECT_DIR%\dist"

echo ============================================================
echo   GENERANDO EJECUTABLE con jpackage
echo   Contador de Gastos - Gestor Financiero Personal
echo ============================================================
echo.

:: --- Verificar jpackage ---
if not exist "%JPACKAGE%" (
    echo [ERROR] No se encontro jpackage en: %JPACKAGE%
    pause
    exit /b 1
)

:: --- Detectar si WiX está disponible ---
set "JPACKAGE_TYPE=app-image"
set "WIX_AVAILABLE=NO"
where candle >nul 2>&1 && set "WIX_AVAILABLE=YES"
if exist "C:\Program Files (x86)\WiX Toolset v3.14\bin\candle.exe" set "WIX_AVAILABLE=YES"
if exist "C:\Program Files\WiX Toolset v3.14\bin\candle.exe" set "WIX_AVAILABLE=YES"

if "%WIX_AVAILABLE%"=="YES" (
    set "JPACKAGE_TYPE=exe"
    echo  Modo: INSTALADOR .EXE ^(WiX detectado^)
) else (
    echo  Modo: APP PORTABLE ^(app-image, sin WiX^)
    echo.
    echo  NOTA: Para generar un instalador .exe instala WiX Toolset v3:
    echo        https://github.com/wixtoolset/wix3/releases/tag/wix3141rtm
)
echo.

:: --- Compilar con Maven ---
echo [1/4] Compilando el proyecto...
cd /d "%PROJECT_DIR%"
call "%MVN_CMD%" clean package -q 2>&1
if errorlevel 1 (
    echo [ERROR] La compilacion fallo.
    call "%MVN_CMD%" clean package
    pause
    exit /b 1
)
echo       OK - JAR generado: %JAR_FILE%
echo.

:: --- Preparar input dir ---
echo [2/4] Preparando archivos...
if exist "%JPACKAGE_INPUT%" rmdir /s /q "%JPACKAGE_INPUT%"
mkdir "%JPACKAGE_INPUT%"
copy "%JAR_FILE%" "%JPACKAGE_INPUT%\" >nul

:: Copiar recursos que la app necesita junto al JAR
if exist "%PROJECT_DIR%\config.properties" (
    copy "%PROJECT_DIR%\config.properties" "%JPACKAGE_INPUT%\" >nul
)
echo       OK
echo.

:: --- Limpiar salida anterior ---
if exist "%JPACKAGE_OUTPUT%" rmdir /s /q "%JPACKAGE_OUTPUT%"
mkdir "%JPACKAGE_OUTPUT%"

:: --- Ejecutar jpackage ---
echo [3/4] Ejecutando jpackage ^(tipo: %JPACKAGE_TYPE%^)...
echo       Esto puede tardar 2-5 minutos...
echo.

if "%JPACKAGE_TYPE%"=="exe" (
    "%JPACKAGE%" ^
        --type exe ^
        --name "ContadorDeGastos" ^
        --app-version "1.0.0" ^
        --vendor "Personal Finance" ^
        --description "Gestor Financiero Personal" ^
        --input "%JPACKAGE_INPUT%" ^
        --dest "%JPACKAGE_OUTPUT%" ^
        --main-jar "Contador_De_Gastos-1.0-SNAPSHOT.jar" ^
        --main-class "com.personalfinance.contador.Launcher" ^
        --java-options "-Dfile.encoding=UTF-8" ^
        --win-menu ^
        --win-shortcut ^
        --win-dir-chooser ^
        --win-per-user-install ^
        --win-menu-group "Aplicaciones Financieras"
) else (
    "%JPACKAGE%" ^
        --type app-image ^
        --name "ContadorDeGastos" ^
        --app-version "1.0.0" ^
        --vendor "Personal Finance" ^
        --description "Gestor Financiero Personal" ^
        --input "%JPACKAGE_INPUT%" ^
        --dest "%JPACKAGE_OUTPUT%" ^
        --main-jar "Contador_De_Gastos-1.0-SNAPSHOT.jar" ^
        --main-class "com.personalfinance.contador.Launcher" ^
        --java-options "-Dfile.encoding=UTF-8"
)

if errorlevel 1 (
    echo.
    echo [ERROR] jpackage fallo. Revisa los mensajes arriba.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo [4/4] GENERACION COMPLETADA!
echo ============================================================
echo.

if "%JPACKAGE_TYPE%"=="exe" (
    echo  Instalador generado en: %JPACKAGE_OUTPUT%\
    dir "%JPACKAGE_OUTPUT%\*.exe" 2>nul
    echo.
    echo  Haz doble clic en el .exe para instalar la aplicacion.
) else (
    echo  Aplicacion portable generada en:
    echo    %JPACKAGE_OUTPUT%\ContadorDeGastos\
    echo.
    dir "%JPACKAGE_OUTPUT%\ContadorDeGastos\*.exe" 2>nul
    echo.
    echo  Para ejecutar: Haz doble clic en:
    echo    %JPACKAGE_OUTPUT%\ContadorDeGastos\ContadorDeGastos.exe
    echo.
    echo  NOTA: Esta carpeta es PORTABLE, puedes copiarla a cualquier
    echo        PC con Windows x64 y ejecutarla sin instalar Java.
)
echo.
pause

@echo off
:: ============================================================
::  Contador de Gastos - SOLO EJECUTAR (sin compilar)
::  Alternativa B: Ejecuta el JAR ya compilado directamente
::  Requisito: Haber ejecutado compilar-y-ejecutar.bat antes
::  Uso: Doble clic para iniciar la aplicación
:: ============================================================
setlocal EnableDelayedExpansion

title Contador de Gastos

:: --- Obtener directorio del script (raíz del proyecto) ---
set "PROJECT_DIR=%~dp0"
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

:: --- Buscar Java ---
set "JAVA_BIN=C:\Program Files\Java\jdk-21.0.10\bin"
if exist "%JAVA_BIN%\java.exe" (
    set "JAVA_EXE=%JAVA_BIN%\java.exe"
) else (
    where java >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] No se encontró Java instalado.
        echo Por favor instala JDK 21 desde: https://www.oracle.com/java/technologies/downloads/
        pause
        exit /b 1
    )
    set "JAVA_EXE=java"
)

:: --- Verificar que el JAR existe ---
set "JAR_FILE=%PROJECT_DIR%\target\Contador_De_Gastos-1.0-SNAPSHOT.jar"

if not exist "%JAR_FILE%" (
    echo [ERROR] No se encontró la aplicación compilada.
    echo.
    echo El archivo esperado es:
    echo   %JAR_FILE%
    echo.
    echo Solución: Ejecuta primero el archivo:
    echo   compilar-y-ejecutar.bat
    echo.
    pause
    exit /b 1
)

:: --- Cambiar al directorio del proyecto para rutas relativas ---
cd /d "%PROJECT_DIR%"

:: --- Lanzar la aplicación sin mostrar consola ---
start "" "%JAVA_EXE%" -jar "%JAR_FILE%"

exit /b 0

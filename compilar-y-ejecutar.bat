@echo off
:: ============================================================
::  Contador de Gastos - COMPILAR Y EJECUTAR
::  Alternativa A: Compila el proyecto y luego lo ejecuta
::  Autor: Generado automáticamente
::  Uso: Doble clic para compilar y ejecutar la aplicación
:: ============================================================
setlocal EnableDelayedExpansion

title Contador de Gastos - Compilando...

:: --- Obtener directorio del script (raíz del proyecto) ---
set "PROJECT_DIR=%~dp0"
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

:: --- Verificar Java ---
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

:: --- Verificar Maven incluido en el proyecto ---
set "MVN_CMD=%PROJECT_DIR%\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    echo [ERROR] No se encontró Maven en el proyecto.
    echo Ruta esperada: %MVN_CMD%
    pause
    exit /b 1
)

echo ============================================================
echo   CONTADOR DE GASTOS - Compilación y Ejecución
echo ============================================================
echo.
echo [1/3] Verificando entorno...
echo       Java: %JAVA_EXE%
echo       Maven: %MVN_CMD%
echo       Proyecto: %PROJECT_DIR%
echo.

:: --- Compilar con Maven ---
echo [2/3] Compilando el proyecto con Maven...
echo       Esto puede tardar unos minutos la primera vez...
echo.

cd /d "%PROJECT_DIR%"
call "%MVN_CMD%" clean package -q 2>&1

if errorlevel 1 (
    echo.
    echo [ERROR] La compilación falló. Ejecutando con detalle para ver errores:
    call "%MVN_CMD%" clean package
    echo.
    echo Por favor revisa los errores mostrados arriba.
    pause
    exit /b 1
)

echo       Compilación exitosa!
echo.

:: --- Ejecutar el JAR ---
echo [3/3] Iniciando la aplicación...
echo.

set "JAR_FILE=%PROJECT_DIR%\target\Contador_De_Gastos-1.0-SNAPSHOT.jar"
if not exist "%JAR_FILE%" (
    echo [ERROR] No se encontró el JAR compilado en:
    echo         %JAR_FILE%
    pause
    exit /b 1
)

start "" "%JAVA_EXE%" -jar "%JAR_FILE%"

echo   Aplicación iniciada correctamente.
echo   Esta ventana se cerrará en 5 segundos...
timeout /t 5 /nobreak >nul
exit /b 0

@echo off
echo ====================================
echo   Creando Instalador
echo ====================================
echo.
echo NOTA: Este script requiere Inno Setup instalado
echo Descargalo desde: https://jrsoftware.org/isdl.php
echo.
echo Compilando con Inno Setup...
echo.

"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" instalador.iss

if %errorlevel% equ 0 (
    echo.
    echo ====================================
    echo   Instalador creado exitosamente!
    echo ====================================
    echo.
    echo El instalador esta en:
    echo DetectorSismos_Setup.exe
    echo.
) else (
    echo.
    echo ====================================
    echo   Error al crear el instalador
    echo ====================================
    echo.
    echo Asegurate de tener Inno Setup instalado en:
    echo C:\Program Files (x86)\Inno Setup 6\
    echo.
)

pause

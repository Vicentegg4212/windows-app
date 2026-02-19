@echo off
echo ====================================
echo   Compilando Detector de Sismos
echo ====================================
echo.

dotnet publish -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true

echo.
echo ====================================
echo   Compilacion completada!
echo ====================================
echo.
echo El ejecutable esta en:
echo bin\Release\net8.0-windows\win-x64\publish\DetectorSismos.exe
echo.
pause

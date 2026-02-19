# SasmexCore.dll

Biblioteca de clases (.dll) del proyecto SASMEX. Contiene la lógica reutilizable sin dependencias de UI.

## Contenido

- **SasmexCore.Models**
  - `AlertaSasmex` – modelo de alerta RSS/CAP
  - `ConfiguracionApp` – configuración de la aplicación

- **SasmexCore.Services**
  - `SasmexService` – obtención de alertas desde `https://rss.sasmex.net/api/v1/alerts/latest/cap/` (reintentos, sin excepciones)
  - `SasmexResult` – resultado tipado (Success, Alertas, ErrorMessage)
  - `ConfigService` – carga/guardado de configuración en `%LocalAppData%/DetectorSismos/config.json`

## Uso

El ejecutable **DetectorSismos** referencia esta DLL. Al compilar, `SasmexCore.dll` se copia a la carpeta de salida del exe.

## Dependencias

- .NET 8.0
- Newtonsoft.Json 13.0.3

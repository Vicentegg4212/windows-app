# App Android SASMEX — Plan desde el mismo proyecto

Misma fuente de datos: **rss.sasmex.net** (SASMEX/CIRES). Opciones para la app Android:

---

## Opción recomendada: .NET MAUI

**Ventajas:** Reutilizas C# y la lógica (modelos, llamada a la API, parseo XML). Un solo lenguaje y una base de código compartida entre Windows y Android.

**Estructura sugerida:**

```
windows-app/
├── DetectorSismos.csproj          # App Windows (WPF) — ya existe
├── SasmexCore/                     # Biblioteca compartida (nueva)
│   ├── SasmexCore.csproj           # net8.0 (o netstandard2.0)
│   ├── Models/
│   │   └── AlertaSasmex.cs
│   └── Services/
│       └── SasmexService.cs
└── Sasmex.Android/                 # App Android (MAUI) — nueva
    ├── Sasmex.Android.csproj       # TargetFramework: net8.0-android
    ├── App.xaml, MainPage.xaml
    ├── MauiProgram.cs
    └── Platforms/Android/...
```

- **SasmexCore**: solo modelos + `SasmexService` (HttpClient, XML). Sin WPF, sin MAUI.
- **DetectorSismos (WPF)**: referencia a SasmexCore y usa sus tipos y el servicio.
- **Sasmex.Android (MAUI)**: referencia a SasmexCore; pantallas en XAML MAUI (listado de alertas, detalle, actualizar).

**Requisitos:** .NET 8 SDK, workload MAUI (`dotnet workload install maui`), Android SDK (Android Studio o command-line tools).

---

## Opción alternativa: Kotlin/Java nativo

**Ventajas:** App 100 % nativa Android, sin MAUI.

**Desventajas:** Lógica duplicada: hay que volver a implementar en Kotlin el cliente HTTP, el parseo del XML de rss.sasmex.net y los modelos equivalentes.

Misma API: `GET https://rss.sasmex.net/api/v1/alerts/latest/cap/` → XML Atom/RSS → extraer entradas (título, fecha, severidad, descripción) como en el bot y en la app Windows.

---

## Pasos siguientes (MAUI)

1. Crear el proyecto **SasmexCore** y mover/copiar `AlertaSasmex` y `SasmexService` (sin depender de WPF).
2. Hacer que **DetectorSismos** (WPF) use SasmexCore en lugar de tener el servicio en el propio proyecto.
3. Crear el proyecto **Sasmex.Android** (MAUI) que referencie SasmexCore.
4. En la app Android: una pantalla con lista de alertas, botón “Actualizar” y (opcional) detalle al tocar una alerta.

En este repo ya tienes la carpeta **SasmexCore** (biblioteca compartida) y el esqueleto del proyecto **Sasmex.Android** para que puedas abrirlo en Visual Studio o con `dotnet build` y seguir desde ahí.

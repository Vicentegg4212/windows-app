# Detector de Sismos en Tiempo Real

Aplicación de escritorio para Windows desarrollada en .NET 8 con WPF que detecta y monitorea sismos en tiempo real utilizando la API de USGS (United States Geological Survey).

## 🌟 Características

- **Detección en tiempo real**: Consulta la API de USGS para obtener sismos recientes
- **Múltiples períodos de consulta**: Última hora, día, semana o mes
- **Filtro de sismos significativos**: Opción para ver solo sismos importantes
- **Monitoreo automático**: Actualización automática cada 5 minutos
- **Notificaciones**: Alertas automáticas para sismos con magnitud >= 4.5
- **Alerta de tsunamis**: Indicador especial para sismos con riesgo de tsunami
- **Interfaz intuitiva**: DataGrid con información detallada de cada sismo
- **Datos en tiempo real**: Magnitud, ubicación, profundidad, coordenadas y más

## 📋 Requisitos

- Windows 10/11
- .NET 8.0 SDK o superior
- Conexión a Internet

## 🚀 Instalación y Ejecución

### Opción 1: Usar Visual Studio
1. Abre el proyecto con Visual Studio 2022
2. Restaura los paquetes NuGet
3. Presiona F5 para compilar y ejecutar

### Opción 2: Línea de comandos
```bash
# Compilar el proyecto
dotnet build

# Ejecutar la aplicación
dotnet run
```

### Opción 3: Crear ejecutable
```bash
# Publicar como ejecutable único
dotnet publish -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true
```

El ejecutable estará en: `bin/Release/net8.0-windows/win-x64/publish/DetectorSismos.exe`

## 🎯 Uso

1. **Seleccionar período**: Elige el rango de tiempo en el ComboBox (última hora, día, semana, mes)
2. **Actualizar datos**: Haz clic en el botón "🔄 Actualizar" para obtener los sismos más recientes
3. **Monitoreo automático**: Activa la casilla "Monitoreo Automático" para recibir actualizaciones cada 5 minutos
4. **Ver detalles**: La tabla muestra toda la información de cada sismo detectado

## 📊 Información mostrada

- **Fecha y Hora**: Momento exacto del sismo
- **Magnitud**: Escala de Richter
- **Lugar**: Ubicación descriptiva del epicentro
- **Latitud/Longitud**: Coordenadas geográficas precisas
- **Profundidad**: En kilómetros bajo la superficie
- **Tsunami**: Indicador de riesgo de tsunami

## 🔔 Sistema de Notificaciones

La aplicación muestra alertas emergentes para:
- Sismos con magnitud >= 4.5
- Sismos con alerta de tsunami
- Solo notifica cada sismo una vez

## 🛠️ Tecnologías utilizadas

- **.NET 8.0**: Framework principal
- **WPF (Windows Presentation Foundation)**: Interfaz de usuario
- **C#**: Lenguaje de programación
- **Newtonsoft.Json**: Deserialización JSON
- **HttpClient**: Comunicación con API REST
- **USGS Earthquake API**: Fuente de datos de sismos

## 📡 API de USGS

La aplicación utiliza la API pública de USGS:
- URL base: `https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/`
- Formato: GeoJSON
- Sin necesidad de autenticación
- Actualización continua

## 🎨 Estructura del Proyecto

```
DetectorSismos/
├── Models/
│   └── Sismo.cs              # Modelos de datos
├── Services/
│   └── SismoService.cs       # Servicio de API
├── MainWindow.xaml           # Interfaz de usuario
├── MainWindow.xaml.cs        # Lógica de la ventana
├── App.xaml                  # Configuración de la app
├── App.xaml.cs               # Punto de entrada
└── DetectorSismos.csproj     # Archivo de proyecto
```

## 📝 Notas

- La aplicación requiere conexión a Internet para funcionar
- Los datos provienen directamente de USGS y se actualizan constantemente
- El monitoreo automático puede consumir ancho de banda si se deja activo por períodos prolongados
- Las notificaciones solo aparecen para sismos nuevos con magnitud >= 4.5

## 🤝 Contribuciones

Este es un proyecto educativo. Siéntete libre de mejorarlo y adaptarlo a tus necesidades.

## 📄 Licencia

Proyecto de código abierto para uso educativo y personal.

## 🌐 Referencias

- [USGS Earthquake API Documentation](https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php)
- [.NET Documentation](https://docs.microsoft.com/dotnet/)
- [WPF Documentation](https://docs.microsoft.com/dotnet/desktop/wpf/)

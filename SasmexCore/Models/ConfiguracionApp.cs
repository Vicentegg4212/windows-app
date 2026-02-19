namespace SasmexCore.Models
{
    /// <summary>
    /// Configuración guardada por el asistente de instalación y usada por la aplicación.
    /// </summary>
    public class ConfiguracionApp
    {
        public int PeriodoInicial { get; set; } = 1;
        public int IntervaloMonitoreoMinutos { get; set; } = 5;
        public bool NotificacionesActivas { get; set; } = true;
        public double MagnitudMinimaNotificacion { get; set; } = 4.5;
        public bool MonitoreoAutomaticoPorDefecto { get; set; } = false;
        public bool SonidoEnAlertas { get; set; } = true;

        public double? WindowLeft { get; set; }
        public double? WindowTop { get; set; }
        public double? WindowWidth { get; set; }
        public double? WindowHeight { get; set; }
    }
}

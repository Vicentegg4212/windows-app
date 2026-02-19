namespace DetectorSismos.Models
{
    /// <summary>
    /// Configuración guardada por el asistente de instalación y usada por la aplicación.
    /// </summary>
    public class ConfiguracionApp
    {
        public int PeriodoInicial { get; set; } = 1; // 0=hora, 1=día, 2=semana, 3=mes...
        public int IntervaloMonitoreoMinutos { get; set; } = 5;
        public bool NotificacionesActivas { get; set; } = true;
        public double MagnitudMinimaNotificacion { get; set; } = 4.5;
        public bool MonitoreoAutomaticoPorDefecto { get; set; } = false;
    }
}

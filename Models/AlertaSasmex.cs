using System;

namespace DetectorSismos.Models
{
    /// <summary>
    /// Representa una alerta del Sistema de Alerta Sísmica Mexicano (SASMEX/CIRES).
    /// Fuente: https://rss.sasmex.net
    /// </summary>
    public class AlertaSasmex
    {
        public string Id { get; set; } = string.Empty;
        public DateTime FechaHora { get; set; }
        public string Evento { get; set; } = string.Empty;
        public string Severidad { get; set; } = "Moderada";
        public string Descripcion { get; set; } = string.Empty;
    }
}

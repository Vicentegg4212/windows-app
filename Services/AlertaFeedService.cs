using System;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Timers;
using System.Xml.Linq;
using System.Xml;
using System.Collections.Generic;

namespace DetectorSismos.Services
{
    public class AlertaFeedService
    {
        private string? _ultimoId;
        private readonly string _archivoUltimoId = "ultimo_alerta_id.txt";
        private Timer? _timer;
        public event Action<AlertaInfo>? NuevaAlertaDetectada;

        public AlertaFeedService()
        {
            CargarUltimoId();
        }

        public void IniciarConsultaPeriodica(string url, double intervaloMs = 30000)
        {
            _timer = new Timer(intervaloMs);
            _timer.Elapsed += async (s, e) => await ConsultarFeed(url);
            _timer.AutoReset = true;
            _timer.Start();
        }

        public async Task ConsultarFeed(string url)
        {
            try
            {
                using var client = new System.Net.Http.HttpClient();
                var xml = await client.GetStringAsync(url);
                var alerta = ParsearAlerta(xml);
                if (alerta != null && alerta.Id != _ultimoId)
                {
                    if (_ultimoId != null) // No es la primera vez
                        NuevaAlertaDetectada?.Invoke(alerta);
                    _ultimoId = alerta.Id;
                    GuardarUltimoId();
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error consultando feed: {ex.Message}");
            }
        }

        public AlertaInfo? ParsearAlerta(string xml)
        {
            try
            {
                var doc = XDocument.Parse(xml);
                XElement? entry = doc.Root?.Name.LocalName switch
                {
                    "feed" => doc.Root.Element("entry"), // Atom
                    "rss" => doc.Root.Element("channel")?.Element("item"), // RSS
                    _ => null
                };
                if (entry == null) return null;

                string id = entry.Element("id")?.Value
                    ?? entry.Element("guid")?.Value
                    ?? entry.Element("link")?.Value
                    ?? "";
                string title = entry.Element("title")?.Value ?? "";
                string date = entry.Element("updated")?.Value
                    ?? entry.Element("pubDate")?.Value
                    ?? ExtraerFechaDeTitulo(title);
                string desc = entry.Element("content")?.Value
                    ?? entry.Element("description")?.Value
                    ?? entry.Element("summary")?.Value
                    ?? entry.Element("content")?.Element("_")?.Value
                    ?? entry.Element("description")?.Element("_")?.Value
                    ?? "";

                // Buscar info anidada
                var alertInfo = entry.Element("content")?.Element("alert")?.Element("info");
                string headline = alertInfo?.Element("headline")?.Value ?? "";
                string alertDesc = alertInfo?.Element("description")?.Value ?? "";
                string severity = alertInfo?.Element("severity")?.Value ?? "";

                var severidad = CalcularSeveridad(desc, severity);

                return new AlertaInfo
                {
                    Id = id,
                    Titulo = title,
                    Fecha = date,
                    Descripcion = desc,
                    Headline = headline,
                    AlertaDescripcion = alertDesc,
                    Severidad = severidad
                };
            }
            catch
            {
                return null;
            }
        }

        private string CalcularSeveridad(string texto, string? severity)
        {
            texto = (texto ?? "").ToLower();
            severity = (severity ?? "").ToLower();
            if (texto.Contains("no ameritó") || texto.Contains("preventiv") || severity == "minor")
                return "Menor";
            if (texto.Contains("ameritó alerta") || texto.Contains("alerta pública") || severity == "severe" || severity == "extreme")
                return "Mayor";
            return "Moderada";
        }

        private string ExtraerFechaDeTitulo(string titulo)
        {
            if (string.IsNullOrEmpty(titulo)) return "";
            var match = System.Text.RegularExpressions.Regex.Match(titulo, @"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?");
            return match.Success ? match.Value : "";
        }

        private void GuardarUltimoId()
        {
            try { File.WriteAllText(_archivoUltimoId, _ultimoId ?? ""); } catch { }
        }
        private void CargarUltimoId()
        {
            try { _ultimoId = File.Exists(_archivoUltimoId) ? File.ReadAllText(_archivoUltimoId) : null; } catch { _ultimoId = null; }
        }
    }

    public class AlertaInfo
    {
        public string Id { get; set; } = "";
        public string Titulo { get; set; } = "";
        public string Fecha { get; set; } = "";
        public string Descripcion { get; set; } = "";
        public string Headline { get; set; } = "";
        public string AlertaDescripcion { get; set; } = "";
        public string Severidad { get; set; } = "";
    }
}

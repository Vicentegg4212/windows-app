using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using System.Xml.Linq;
using DetectorSismos.Models;

namespace DetectorSismos.Services
{
    /// <summary>
    /// Servicio que obtiene alertas sísmicas desde la API SASMEX/CIRES (la misma que usa el bot).
    /// URL: https://rss.sasmex.net/api/v1/alerts/latest/cap/
    /// </summary>
    public class SasmexService
    {
        private readonly HttpClient _httpClient;
        private const string ApiUrl = "https://rss.sasmex.net/api/v1/alerts/latest/cap/";
        private static readonly XNamespace Atom = "http://www.w3.org/2005/Atom";

        public SasmexService()
        {
            _httpClient = new HttpClient();
            _httpClient.Timeout = TimeSpan.FromSeconds(30);
            _httpClient.DefaultRequestHeaders.Add("User-Agent", "DetectorSismos-WPF/1.0");
            _httpClient.DefaultRequestHeaders.Add("Accept", "application/xml, text/xml, */*");
        }

        /// <summary>
        /// Obtiene las últimas alertas SASMEX (feed Atom o RSS).
        /// </summary>
        public async Task<List<AlertaSasmex>> ObtenerUltimasAlertasAsync()
        {
            var lista = new List<AlertaSasmex>();
            try
            {
                var response = await _httpClient.GetAsync(ApiUrl);
                response.EnsureSuccessStatusCode();
                string xml = await response.Content.ReadAsStringAsync();
                if (string.IsNullOrWhiteSpace(xml))
                    return lista;

                var doc = XDocument.Parse(xml);
                var root = doc.Root;
                if (root == null)
                    return lista;

                // Atom: feed/entry
                var entries = root.Elements(Atom + "entry").ToList();
                if (entries.Count == 0)
                {
                    // RSS: rss/channel/item
                    var channel = root.Element("channel") ?? root.Element(Atom + "channel");
                    if (channel != null)
                    {
                        entries = channel.Elements("item").ToList();
                        if (entries.Count == 0)
                            entries = channel.Elements(Atom + "item").ToList();
                    }
                }

                foreach (var entry in entries)
                {
                    var alerta = ParseEntry(entry, entries.IndexOf(entry));
                    if (alerta != null)
                        lista.Add(alerta);
                }

                // Ordenar por fecha descendente (más reciente primero)
                lista = lista.OrderByDescending(a => a.FechaHora).ToList();
            }
            catch (HttpRequestException ex)
            {
                System.Diagnostics.Debug.WriteLine($"SasmexService: {ex.Message}");
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"SasmexService parse: {ex.Message}");
            }

            return lista;
        }

        private static AlertaSasmex? ParseEntry(XElement entry, int index)
        {
            string GetValue(XElement? parent, string localName)
            {
                if (parent == null) return string.Empty;
                var el = parent.Element(Atom + localName) ?? parent.Element(localName);
                return el?.Value?.Trim() ?? string.Empty;
            }

            string GetContent(XElement? parent, string localName)
            {
                if (parent == null) return string.Empty;
                var el = parent.Element(Atom + localName) ?? parent.Element(localName);
                if (el == null) return string.Empty;
                // Puede ser valor directo o tener atributo type="html" con contenido
                var value = el.Value?.Trim();
                if (!string.IsNullOrEmpty(value)) return LimpiarDescripcion(value);
                var attr = el.Attribute("type");
                if (attr?.Value == "html" || attr?.Value == "xhtml")
                {
                    return LimpiarDescripcion(string.Concat(el.Nodes().Select(n => n.ToString())));
                }
                return string.Empty;
            }

            var id = GetValue(entry, "id");
            if (string.IsNullOrEmpty(id))
                id = GetValue(entry, "guid") ?? $"alerta-{index}";

            var title = GetValue(entry, "title");
            var updated = GetValue(entry, "updated");
            if (string.IsNullOrEmpty(updated))
                updated = GetValue(entry, "published") ?? GetValue(entry, "pubDate");
            var content = GetContent(entry, "content");
            if (string.IsNullOrEmpty(content))
                content = GetContent(entry, "description");
            if (string.IsNullOrEmpty(content))
                content = GetContent(entry, "summary");

            // Fecha: intentar parsear ISO o formato RSS
            DateTime fechaHora = DateTime.Now;
            if (!string.IsNullOrEmpty(updated))
            {
                if (DateTime.TryParse(updated, null, System.Globalization.DateTimeStyles.RoundtripKind, out var parsed))
                    fechaHora = parsed.ToLocalTime();
                else if (DateTime.TryParse(updated, out var parsed2))
                    fechaHora = parsed2;
            }

            // Severidad desde texto (igual que el bot)
            string severidad = "Severidad: Moderada";
            var descLower = (content + " " + title).ToLowerInvariant();
            if (descLower.Contains("minor") || descLower.Contains("no ameritó") || descLower.Contains("preventiv") ||
                descLower.Contains("sismo moderado") || descLower.Contains("menor"))
                severidad = "Severidad: Menor";
            else if (descLower.Contains("severe") || descLower.Contains("extreme") ||
                     descLower.Contains("ameritó alerta") || descLower.Contains("alerta pública") ||
                     descLower.Contains("mayor") || descLower.Contains("fuerte"))
                severidad = "Severidad: Mayor";

            return new AlertaSasmex
            {
                Id = id,
                FechaHora = fechaHora,
                Evento = string.IsNullOrEmpty(title) ? "Alerta Sísmica SASMEX" : title,
                Severidad = severidad,
                Descripcion = content
            };
        }

        private static string LimpiarDescripcion(string text)
        {
            if (string.IsNullOrWhiteSpace(text)) return string.Empty;
            var lineas = text.Split(new[] { "\r\n", "\r", "\n" }, StringSplitOptions.None)
                .Select(l => l.Trim())
                .Where(l =>
                {
                    if (string.IsNullOrEmpty(l)) return false;
                    if (l.StartsWith("ALERTA SÍSMICA SASMEX", StringComparison.OrdinalIgnoreCase)) return false;
                    if (l.StartsWith("Sistema de Alerta Sísmica Mexicano", StringComparison.OrdinalIgnoreCase)) return false;
                    if (System.Text.RegularExpressions.Regex.IsMatch(l, @"Consulta:\s*https?://cires\.org\.mx/reportes_sasmex", System.Text.RegularExpressions.RegexOptions.IgnoreCase)) return false;
                    return true;
                });
            return string.Join("\n", lineas).Trim();
        }
    }
}

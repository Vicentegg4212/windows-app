using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Linq;
using SasmexCore.Models;

namespace SasmexCore.Services
{
    /// <summary>
    /// Servicio robusto para obtener alertas SASMEX. Reintentos automáticos, sin excepciones al llamante.
    /// URL: https://rss.sasmex.net/api/v1/alerts/latest/cap/
    /// </summary>
    public class SasmexService
    {
        private const string ApiUrl = "https://rss.sasmex.net/api/v1/alerts/latest/cap/";
        private static readonly XNamespace Atom = "http://www.w3.org/2005/Atom";
        private const int MaxReintentos = 3;
        private const int DelayEntreReintentosMs = 2000;
        private const int TimeoutSegundos = 20;

        private readonly HttpClient _httpClient;

        public SasmexService(string? userAgent = null)
        {
            _httpClient = new HttpClient();
            _httpClient.Timeout = TimeSpan.FromSeconds(TimeoutSegundos);
            try
            {
                _httpClient.DefaultRequestHeaders.TryAddWithoutValidation("User-Agent", userAgent ?? "SasmexCore/2.0");
                _httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Accept", "application/xml, text/xml, */*");
                _httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Cache-Control", "no-cache");
            }
            catch { }
        }

        /// <summary>
        /// Obtiene las últimas alertas. Nunca lanza; devuelve SasmexResult con Success o ErrorMessage.
        /// </summary>
        public async Task<SasmexResult> ObtenerAlertasAsync(CancellationToken cancellationToken = default)
        {
            for (int intento = 1; intento <= MaxReintentos; intento++)
            {
                try
                {
                    using var cts = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
                    cts.CancelAfter(TimeSpan.FromSeconds(TimeoutSegundos));

                    var response = await _httpClient.GetAsync(ApiUrl, HttpCompletionOption.ResponseContentRead, cts.Token).ConfigureAwait(false);
                    response.EnsureSuccessStatusCode();

                    string xml = await response.Content.ReadAsStringAsync(cts.Token).ConfigureAwait(false);
                    if (string.IsNullOrWhiteSpace(xml))
                        return SasmexResult.Ok(new List<AlertaSasmex>());

                    var lista = ParsearXml(xml);
                    return SasmexResult.Ok(lista);
                }
                catch (TaskCanceledException) when (cancellationToken.IsCancellationRequested)
                {
                    return SasmexResult.Fail("Operación cancelada.");
                }
                catch (TaskCanceledException)
                {
                    if (intento == MaxReintentos)
                        return SasmexResult.Fail("Tiempo de espera agotado. El servidor no respondió a tiempo.");
                    await Task.Delay(DelayEntreReintentosMs * intento, cancellationToken).ConfigureAwait(false);
                }
                catch (OperationCanceledException)
                {
                    return SasmexResult.Fail("La solicitud fue cancelada o superó el tiempo de espera.");
                }
                catch (HttpRequestException ex)
                {
                    if (intento == MaxReintentos)
                        return SasmexResult.Fail($"No se pudo conectar a rss.sasmex.net. Verifica tu conexión a internet.\n\nDetalle: {ex.Message}");
                    await Task.Delay(DelayEntreReintentosMs * intento, cancellationToken).ConfigureAwait(false);
                }
                catch (XmlException ex)
                {
                    if (intento == MaxReintentos)
                        return SasmexResult.Fail($"La respuesta del servidor no es XML válido.\n\nDetalle: {ex.Message}");
                    await Task.Delay(DelayEntreReintentosMs, cancellationToken).ConfigureAwait(false);
                }
                catch (Exception ex)
                {
                    if (intento == MaxReintentos)
                        return SasmexResult.Fail($"Error inesperado: {ex.Message}");
                    await Task.Delay(DelayEntreReintentosMs * intento, cancellationToken).ConfigureAwait(false);
                }
            }

            return SasmexResult.Fail("No se pudo obtener datos después de varios intentos.");
        }

        private static List<AlertaSasmex> ParsearXml(string xml)
        {
            var lista = new List<AlertaSasmex>();
            try
            {
                var doc = XDocument.Parse(xml);
                var root = doc.Root;
                if (root == null) return lista;

                var entries = root.Elements(Atom + "entry").ToList();
                if (entries.Count == 0)
                {
                    var channel = root.Element("channel") ?? root.Element(Atom + "channel");
                    if (channel != null)
                    {
                        entries = channel.Elements("item").ToList();
                        if (entries.Count == 0)
                            entries = channel.Elements(Atom + "item").ToList();
                    }
                }

                for (int i = 0; i < entries.Count; i++)
                {
                    var alerta = ParseEntry(entries[i], i);
                    if (alerta != null)
                        lista.Add(alerta);
                }

                return lista.OrderByDescending(a => a.FechaHora).ToList();
            }
            catch
            {
                return lista;
            }
        }

        private static AlertaSasmex? ParseEntry(XElement entry, int index)
        {
            try
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
                    var value = el.Value?.Trim();
                    if (!string.IsNullOrEmpty(value)) return LimpiarDescripcion(value);
                    var attr = el.Attribute("type");
                    if (attr?.Value == "html" || attr?.Value == "xhtml")
                        return LimpiarDescripcion(string.Concat(el.Nodes().Select(n => n.ToString())));
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

                DateTime fechaHora = DateTime.Now;
                if (!string.IsNullOrEmpty(updated))
                {
                    if (DateTime.TryParse(updated, null, System.Globalization.DateTimeStyles.RoundtripKind, out var parsed))
                        fechaHora = parsed.ToLocalTime();
                    else if (DateTime.TryParse(updated, out var parsed2))
                        fechaHora = parsed2;
                }

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
                    Descripcion = content ?? string.Empty
                };
            }
            catch
            {
                return null;
            }
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

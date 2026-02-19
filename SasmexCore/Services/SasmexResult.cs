using System.Collections.Generic;
using SasmexCore.Models;

namespace SasmexCore.Services
{
    /// <summary>
    /// Resultado de la obtención de alertas SASMEX. Nunca lanza excepciones al llamante.
    /// </summary>
    public sealed class SasmexResult
    {
        public bool Success { get; init; }
        public List<AlertaSasmex> Alertas { get; init; } = new();
        public string ErrorMessage { get; init; } = string.Empty;

        public static SasmexResult Ok(List<AlertaSasmex>? alertas) =>
            new() { Success = true, Alertas = alertas ?? new List<AlertaSasmex>() };

        public static SasmexResult Fail(string errorMessage) =>
            new() { Success = false, ErrorMessage = string.IsNullOrEmpty(errorMessage) ? "Error desconocido." : errorMessage };
    }
}

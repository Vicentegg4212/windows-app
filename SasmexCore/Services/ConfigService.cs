using System;
using System.IO;
using Newtonsoft.Json;
using SasmexCore.Models;

namespace SasmexCore.Services
{
    /// <summary>
    /// Guarda y carga la configuración en %LocalAppData%/DetectorSismos/config.json.
    /// No muestra UI; Guardar devuelve false si falla.
    /// </summary>
    public static class ConfigService
    {
        private static string AppDataFolder =>
            Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "DetectorSismos");

        private static string ConfigPath => Path.Combine(AppDataFolder, "config.json");

        public static string AppDataPath => AppDataFolder;

        public static ConfiguracionApp? Cargar()
        {
            try
            {
                if (!File.Exists(ConfigPath))
                    return null;
                string json = File.ReadAllText(ConfigPath);
                return JsonConvert.DeserializeObject<ConfiguracionApp>(json);
            }
            catch
            {
                return null;
            }
        }

        /// <returns>true si se guardó correctamente; false si hubo error (el llamante puede mostrar mensaje).</returns>
        public static bool Guardar(ConfiguracionApp config)
        {
            try
            {
                if (!Directory.Exists(AppDataFolder))
                    Directory.CreateDirectory(AppDataFolder);
                string json = JsonConvert.SerializeObject(config, Formatting.Indented);
                File.WriteAllText(ConfigPath, json);
                return true;
            }
            catch
            {
                return false;
            }
        }
    }
}

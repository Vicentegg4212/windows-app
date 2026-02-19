using System;
using System.IO;
using Newtonsoft.Json;
using DetectorSismos.Models;

namespace DetectorSismos.Services
{
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

        public static void Guardar(ConfiguracionApp config)
        {
            try
            {
                if (!Directory.Exists(AppDataFolder))
                    Directory.CreateDirectory(AppDataFolder);
                string json = JsonConvert.SerializeObject(config, Formatting.Indented);
                File.WriteAllText(ConfigPath, json);
            }
            catch (Exception ex)
            {
                System.Windows.MessageBox.Show(
                    $"No se pudo guardar la configuración: {ex.Message}",
                    "Aviso",
                    System.Windows.MessageBoxButton.OK,
                    System.Windows.MessageBoxImage.Warning);
            }
        }
    }
}

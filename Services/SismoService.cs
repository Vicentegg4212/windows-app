using System;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json;
using DetectorSismos.Models;

namespace DetectorSismos.Services
{
    public class SismoService
    {
        private readonly HttpClient _httpClient;
        private const string API_BASE_URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/";

        public SismoService()
        {
            _httpClient = new HttpClient();
            _httpClient.Timeout = TimeSpan.FromSeconds(30);
        }

        /// <summary>
        /// Obtiene sismos de la última hora con magnitud 2.5+
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosUltimaHora()
        {
            return await ObtenerSismos("2.5_hour.geojson");
        }

        /// <summary>
        /// Obtiene sismos del último día con magnitud 2.5+
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosUltimoDia()
        {
            return await ObtenerSismos("2.5_day.geojson");
        }

        /// <summary>
        /// Obtiene sismos de la última semana con magnitud 2.5+
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosUltimaSemana()
        {
            return await ObtenerSismos("2.5_week.geojson");
        }

        /// <summary>
        /// Obtiene sismos del último mes con magnitud 2.5+
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosUltimoMes()
        {
            return await ObtenerSismos("2.5_month.geojson");
        }

        /// <summary>
        /// Obtiene sismos significativos de la última hora
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosSignificativosUltimaHora()
        {
            return await ObtenerSismos("significant_hour.geojson");
        }

        /// <summary>
        /// Obtiene sismos significativos del último día
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosSignificativosUltimoDia()
        {
            return await ObtenerSismos("significant_day.geojson");
        }

        /// <summary>
        /// Obtiene sismos significativos de la última semana
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosSignificativosUltimaSemana()
        {
            return await ObtenerSismos("significant_week.geojson");
        }

        /// <summary>
        /// Obtiene sismos significativos del último mes
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerSismosSignificativosUltimoMes()
        {
            return await ObtenerSismos("significant_month.geojson");
        }

        /// <summary>
        /// Obtiene todos los sismos de la última hora
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerTodosSismosUltimaHora()
        {
            return await ObtenerSismos("all_hour.geojson");
        }

        /// <summary>
        /// Obtiene todos los sismos del último día
        /// </summary>
        public async Task<RespuestaUSGS?> ObtenerTodosSismosUltimoDia()
        {
            return await ObtenerSismos("all_day.geojson");
        }

        private async Task<RespuestaUSGS?> ObtenerSismos(string endpoint)
        {
            try
            {
                string url = $"{API_BASE_URL}{endpoint}";
                HttpResponseMessage response = await _httpClient.GetAsync(url);
                response.EnsureSuccessStatusCode();

                string json = await response.Content.ReadAsStringAsync();
                RespuestaUSGS? resultado = JsonConvert.DeserializeObject<RespuestaUSGS>(json);

                return resultado;
            }
            catch (HttpRequestException ex)
            {
                Console.WriteLine($"Error al obtener sismos: {ex.Message}");
                return null;
            }
            catch (JsonException ex)
            {
                Console.WriteLine($"Error al deserializar respuesta: {ex.Message}");
                return null;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error inesperado: {ex.Message}");
                return null;
            }
        }
    }
}

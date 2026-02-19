using System;
using Newtonsoft.Json;

namespace DetectorSismos.Models
{
    public class Sismo
    {
        [JsonProperty("id")]
        public string Id { get; set; } = string.Empty;

        [JsonProperty("properties")]
        public SismoProperties Properties { get; set; } = new();

        [JsonProperty("geometry")]
        public Geometry Geometry { get; set; } = new();
    }

    public class SismoProperties
    {
        [JsonProperty("mag")]
        public double Magnitud { get; set; }

        [JsonProperty("place")]
        public string Lugar { get; set; } = string.Empty;

        [JsonProperty("time")]
        public long Time { get; set; }

        [JsonProperty("updated")]
        public long Updated { get; set; }

        [JsonProperty("url")]
        public string Url { get; set; } = string.Empty;

        [JsonProperty("detail")]
        public string Detail { get; set; } = string.Empty;

        [JsonProperty("status")]
        public string Status { get; set; } = string.Empty;

        [JsonProperty("tsunami")]
        public int Tsunami { get; set; }

        [JsonProperty("type")]
        public string Type { get; set; } = string.Empty;

        public DateTime FechaHora => DateTimeOffset.FromUnixTimeMilliseconds(Time).LocalDateTime;
    }

    public class Geometry
    {
        [JsonProperty("type")]
        public string Type { get; set; } = string.Empty;

        [JsonProperty("coordinates")]
        public double[] Coordinates { get; set; } = Array.Empty<double>();

        public double Longitud => Coordinates.Length > 0 ? Coordinates[0] : 0;
        public double Latitud => Coordinates.Length > 1 ? Coordinates[1] : 0;
        public double Profundidad => Coordinates.Length > 2 ? Coordinates[2] : 0;
    }

    public class RespuestaUSGS
    {
        [JsonProperty("type")]
        public string Type { get; set; } = string.Empty;

        [JsonProperty("metadata")]
        public Metadata Metadata { get; set; } = new();

        [JsonProperty("features")]
        public Sismo[] Features { get; set; } = Array.Empty<Sismo>();
    }

    public class Metadata
    {
        [JsonProperty("generated")]
        public long Generated { get; set; }

        [JsonProperty("url")]
        public string Url { get; set; } = string.Empty;

        [JsonProperty("title")]
        public string Title { get; set; } = string.Empty;

        [JsonProperty("status")]
        public int Status { get; set; }

        [JsonProperty("api")]
        public string Api { get; set; } = string.Empty;

        [JsonProperty("count")]
        public int Count { get; set; }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Threading;
using DetectorSismos.Models;
using DetectorSismos.Services;

namespace DetectorSismos
{
    public partial class MainWindow : Window
    {
        private readonly SasmexService _sasmexService;
        private readonly DispatcherTimer _timer;
        private readonly HashSet<string> _alertasNotificadas;
        private DateTime _ultimaActualizacion;
        private bool _notificacionesActivas = true;
        private bool _notificarSoloMayor = true;

        public MainWindow()
        {
            InitializeComponent();
            _sasmexService = new SasmexService();
            _alertasNotificadas = new HashSet<string>();

            var config = ConfigService.Cargar();
            if (config != null)
            {
                _timer = new DispatcherTimer();
                _timer.Interval = TimeSpan.FromMinutes(config.IntervaloMonitoreoMinutos);
                _notificacionesActivas = config.NotificacionesActivas;
                chkMonitoreo.IsChecked = config.MonitoreoAutomaticoPorDefecto;
            }
            else
            {
                _timer = new DispatcherTimer();
                _timer.Interval = TimeSpan.FromMinutes(5);
            }

            _timer.Tick += Timer_Tick;
            if (chkMonitoreo.IsChecked == true)
                _timer.Start();

            _ = CargarAlertas();
        }

        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.F5 && btnActualizar.IsEnabled)
            {
                e.Handled = true;
                _ = CargarAlertas();
            }
        }

        private async void btnActualizar_Click(object sender, RoutedEventArgs e)
        {
            await CargarAlertas();
        }

        private async System.Threading.Tasks.Task CargarAlertas()
        {
            try
            {
                txtEstado.Text = "Cargando alertas SASMEX...";
                btnActualizar.IsEnabled = false;
                btnActualizar.Content = "Cargando…";

                var alertas = await _sasmexService.ObtenerUltimasAlertasAsync();

                if (alertas != null && alertas.Count > 0)
                {
                    dgAlertas.ItemsSource = alertas;
                    txtContador.Text = $"Alertas: {alertas.Count}";

                    var ultima = alertas.First();
                    txtAlertaTitulo.Text = ultima.Evento;
                    txtAlertaFecha.Text = ultima.FechaHora.ToString("dd/MM/yyyy HH:mm:ss");
                    txtAlertaSeveridad.Text = ultima.Severidad;
                    txtAlertaDescripcion.Text = string.IsNullOrEmpty(ultima.Descripcion) ? "—" : ultima.Descripcion;

                    // Color de la barra según severidad
                    var sev = (ultima.Severidad ?? "").ToLowerInvariant();
                    alertCardBar.Background = sev.Contains("mayor") ? (Brush)Application.Current.Resources["DangerBrush"]
                        : sev.Contains("menor") ? (Brush)Application.Current.Resources["SuccessBrush"]
                        : (Brush)Application.Current.Resources["WarningBrush"];

                    if (chkMonitoreo.IsChecked == true)
                        VerificarAlertasNuevas(alertas);
                }
                else
                {
                    dgAlertas.ItemsSource = null;
                    txtContador.Text = "Alertas: 0";
                    txtAlertaTitulo.Text = "Sin alertas recientes";
                    txtAlertaFecha.Text = "—";
                    txtAlertaSeveridad.Text = "—";
                    txtAlertaDescripcion.Text = "No hay entradas en el feed o no se pudo conectar a rss.sasmex.net.";
                    alertCardBar.Background = (Brush)Application.Current.Resources["WarningBrush"];
                }

                _ultimaActualizacion = DateTime.Now;
                txtUltimaActualizacion.Text = $"Última actualización: {_ultimaActualizacion:dd/MM/yyyy HH:mm:ss}";
                txtEstado.Text = "Listo";
            }
            catch (Exception ex)
            {
                txtEstado.Text = "Error";
                MessageBox.Show($"Error al cargar alertas: {ex.Message}\n\nVerifica tu conexión y que rss.sasmex.net esté disponible.",
                    "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                btnActualizar.IsEnabled = true;
                btnActualizar.Content = "Actualizar";
            }
        }

        private void VerificarAlertasNuevas(List<AlertaSasmex> alertas)
        {
            if (!_notificacionesActivas) return;
            foreach (var alerta in alertas)
            {
                if (_alertasNotificadas.Contains(alerta.Id)) continue;
                bool esMayor = alerta.Severidad.Contains("Mayor", StringComparison.OrdinalIgnoreCase);
                if (_notificarSoloMayor && !esMayor) continue;

                _alertasNotificadas.Add(alerta.Id);
                string mensaje = $"{alerta.Evento}\n\n{alerta.Severidad}\n{alerta.FechaHora:dd/MM/yyyy HH:mm:ss}";
                if (!string.IsNullOrEmpty(alerta.Descripcion))
                    mensaje += $"\n\n{alerta.Descripcion}";
                MessageBox.Show(mensaje, "⚠️ Alerta SASMEX / CIRES",
                    MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        private void chkMonitoreo_Checked(object sender, RoutedEventArgs e)
        {
            _timer.Start();
            txtEstado.Text = "Monitoreo automático activado";
        }

        private void chkMonitoreo_Unchecked(object sender, RoutedEventArgs e)
        {
            _timer.Stop();
            txtEstado.Text = "Monitoreo automático desactivado";
        }

        private async void Timer_Tick(object? sender, EventArgs e)
        {
            await CargarAlertas();
        }

        protected override void OnClosed(EventArgs e)
        {
            _timer.Stop();
            base.OnClosed(e);
        }
    }
}

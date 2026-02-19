using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Threading;
using DetectorSismos.Models;
using DetectorSismos.Services;

namespace DetectorSismos
{
    public partial class MainWindow : Window
    {
        private readonly SismoService _sismoService;
        private readonly DispatcherTimer _timer;
        private HashSet<string> _sismosNotificados;
        private DateTime _ultimaActualizacion;

        public MainWindow()
        {
            InitializeComponent();
            _sismoService = new SismoService();
            _sismosNotificados = new HashSet<string>();

            // Configurar timer para monitoreo automático (cada 5 minutos)
            _timer = new DispatcherTimer();
            _timer.Interval = TimeSpan.FromMinutes(5);
            _timer.Tick += Timer_Tick;

            // Cargar sismos al iniciar
            _ = CargarSismos();
        }

        private async void btnActualizar_Click(object sender, RoutedEventArgs e)
        {
            await CargarSismos();
        }

        private async System.Threading.Tasks.Task CargarSismos()
        {
            try
            {
                txtEstado.Text = "Cargando sismos...";
                btnActualizar.IsEnabled = false;

                RespuestaUSGS? respuesta = null;

                // Obtener sismos según el período seleccionado
                switch (cbPeriodo.SelectedIndex)
                {
                    case 0: // Última Hora
                        respuesta = await _sismoService.ObtenerSismosUltimaHora();
                        break;
                    case 1: // Último Día
                        respuesta = await _sismoService.ObtenerSismosUltimoDia();
                        break;
                    case 2: // Última Semana
                        respuesta = await _sismoService.ObtenerSismosUltimaSemana();
                        break;
                    case 3: // Último Mes
                        respuesta = await _sismoService.ObtenerSismosUltimoMes();
                        break;
                    case 4: // Significativos - Última Hora
                        respuesta = await _sismoService.ObtenerSismosSignificativosUltimaHora();
                        break;
                    case 5: // Significativos - Último Día
                        respuesta = await _sismoService.ObtenerSismosSignificativosUltimoDia();
                        break;
                    case 6: // Significativos - Última Semana
                        respuesta = await _sismoService.ObtenerSismosSignificativosUltimaSemana();
                        break;
                    case 7: // Significativos - Último Mes
                        respuesta = await _sismoService.ObtenerSismosSignificativosUltimoMes();
                        break;
                    default:
                        respuesta = await _sismoService.ObtenerSismosUltimoDia();
                        break;
                }

                if (respuesta != null && respuesta.Features != null)
                {
                    // Ordenar por fecha (más recientes primero)
                    var sismosOrdenados = respuesta.Features
                        .OrderByDescending(s => s.Properties.Time)
                        .ToList();

                    // Actualizar DataGrid
                    dgSismos.ItemsSource = sismosOrdenados;
                    txtContador.Text = $"Sismos detectados: {sismosOrdenados.Count}";

                    // Verificar sismos nuevos para notificación
                    if (chkMonitoreo.IsChecked == true)
                    {
                        VerificarSismosNuevos(sismosOrdenados);
                    }

                    _ultimaActualizacion = DateTime.Now;
                    txtUltimaActualizacion.Text = $"Última actualización: {_ultimaActualizacion:dd/MM/yyyy HH:mm:ss}";
                    txtEstado.Text = "Sismos cargados correctamente";
                }
                else
                {
                    txtEstado.Text = "Error al cargar sismos";
                    MessageBox.Show("No se pudieron obtener los datos de sismos. Verifica tu conexión a internet.",
                        "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
            catch (Exception ex)
            {
                txtEstado.Text = "Error";
                MessageBox.Show($"Error al cargar sismos: {ex.Message}",
                    "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                btnActualizar.IsEnabled = true;
            }
        }

        private void VerificarSismosNuevos(List<Sismo> sismos)
        {
            foreach (var sismo in sismos)
            {
                // Solo notificar sismos con magnitud >= 4.5
                if (sismo.Properties.Magnitud >= 4.5 && !_sismosNotificados.Contains(sismo.Id))
                {
                    _sismosNotificados.Add(sismo.Id);

                    string mensaje = $"Magnitud: {sismo.Properties.Magnitud:N1}\n" +
                                   $"Lugar: {sismo.Properties.Lugar}\n" +
                                   $"Fecha: {sismo.Properties.FechaHora:dd/MM/yyyy HH:mm:ss}";

                    if (sismo.Properties.Tsunami == 1)
                    {
                        mensaje += "\n⚠️ ALERTA DE TSUNAMI";
                    }

                    MessageBox.Show(mensaje, "⚠️ Nuevo Sismo Detectado",
                        MessageBoxButton.OK, MessageBoxImage.Warning);
                }
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
            await CargarSismos();
        }

        protected override void OnClosed(EventArgs e)
        {
            _timer.Stop();
            base.OnClosed(e);
        }
    }
}

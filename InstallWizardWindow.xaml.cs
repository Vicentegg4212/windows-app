using System;
using System.IO;
using System.Net.Http;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using DetectorSismos.Models;
using DetectorSismos.Services;

namespace DetectorSismos
{
    public partial class InstallWizardWindow : Window
    {
        private int _pasoActual = 1;
        private const int TotalPasos = 4;
        private ConfiguracionApp _config = new ConfiguracionApp();

        public InstallWizardWindow()
        {
            InitializeComponent();
            MostrarPaso(1);
        }

        private void MostrarPaso(int paso)
        {
            _pasoActual = paso;
            panelContent.Children.Clear();

            // Actualizar indicadores visuales (paleta App.xaml)
            var colorActivo = (Brush)Application.Current.Resources["SuccessBrush"];
            var colorInactivo = (Brush)new BrushConverter().ConvertFrom("#FFCBD5E1")!;
            step1Circle.Fill = paso >= 1 ? colorActivo : colorInactivo;
            step2Circle.Fill = paso >= 2 ? colorActivo : colorInactivo;
            step3Circle.Fill = paso >= 3 ? colorActivo : colorInactivo;
            step4Circle.Fill = paso >= 4 ? colorActivo : colorInactivo;

            btnAtras.Visibility = paso > 1 ? Visibility.Visible : Visibility.Collapsed;
            btnSiguiente.Visibility = paso < TotalPasos ? Visibility.Visible : Visibility.Collapsed;
            btnFinalizar.Visibility = paso == TotalPasos ? Visibility.Visible : Visibility.Collapsed;

            switch (paso)
            {
                case 1: PasoBienvenida(); break;
                case 2: PasoRequisitos(); break;
                case 3: PasoConfiguracion(); break;
                case 4: PasoFinalizar(); break;
            }
        }

        private void PasoBienvenida()
        {
            panelContent.Children.Add(new TextBlock
            {
                Text = "Bienvenido al asistente de instalación",
                FontSize = 20,
                FontWeight = FontWeights.Bold,
                Margin = new Thickness(0, 0, 0, 12)
            });
            panelContent.Children.Add(new TextBlock
            {
                Text = "Este asistente te guiará para configurar el Detector de Sismos en Tiempo Real. " +
                       "Se verificará que tu equipo cumpla los requisitos y podrás elegir opciones iniciales.",
                TextWrapping = TextWrapping.Wrap,
                FontSize = 14,
                Margin = new Thickness(0, 0, 0, 20)
            });
            panelContent.Children.Add(new TextBlock
            {
                Text = "Características principales:",
                FontSize = 16,
                FontWeight = FontWeights.SemiBold,
                Margin = new Thickness(0, 0, 0, 8)
            });
            var lista = new StackPanel { Margin = new Thickness(15, 0, 0, 0) };
            foreach (var s in new[] {
                "• Detección de sismos en tiempo real (USGS)",
                "• Notificaciones para sismos significativos (magnitud ≥ 4.5)",
                "• Alertas de tsunami",
                "• Monitoreo automático opcional"
            })
                lista.Children.Add(new TextBlock { Text = s, TextWrapping = TextWrapping.Wrap, Margin = new Thickness(0, 4, 0, 0), FontSize = 13 });
            panelContent.Children.Add(lista);
        }

        private void PasoRequisitos()
        {
            panelContent.Children.Add(new TextBlock
            {
                Text = "Verificación de requisitos",
                FontSize = 20,
                FontWeight = FontWeights.Bold,
                Margin = new Thickness(0, 0, 0, 12)
            });
            panelContent.Children.Add(new TextBlock
            {
                Text = "Comprobando que tu sistema esté listo para ejecutar la aplicación...",
                TextWrapping = TextWrapping.Wrap,
                FontSize = 14,
                Margin = new Thickness(0, 0, 0, 16)
            });

            var sp = new StackPanel();
            ComprobarRequisito(sp, "Windows 10 o superior", true);
            ComprobarRequisito(sp, ".NET 8.0 en tiempo de ejecución", true);
            ComprobarRequisito(sp, "Conexión a Internet", VerificarInternet());
            ComprobarRequisito(sp, "Espacio en disco (aprox. 200 MB)", VerificarEspacioDisco());
            panelContent.Children.Add(sp);
        }

        private void ComprobarRequisito(StackPanel parent, string titulo, bool ok)
        {
            var panel = new StackPanel
            {
                Orientation = Orientation.Horizontal,
                Margin = new Thickness(0, 8, 0, 0)
            };
            panel.Children.Add(new TextBlock
            {
                Text = ok ? "✓" : "✗",
                FontSize = 16,
                Foreground = ok ? Brushes.Green : Brushes.Red,
                Margin = new Thickness(0, 0, 10, 0)
            });
            panel.Children.Add(new TextBlock
            {
                Text = titulo,
                FontSize = 14,
                VerticalAlignment = VerticalAlignment.Center
            });
            parent.Children.Add(panel);
        }

        private static bool VerificarInternet()
        {
            try
            {
                using var client = new HttpClient { Timeout = TimeSpan.FromSeconds(8) };
                client.DefaultRequestHeaders.Add("User-Agent", "DetectorSismos-WPF/1.0");
                var response = client.GetAsync("https://rss.sasmex.net/api/v1/alerts/latest/cap/").Result;
                return response.IsSuccessStatusCode;
            }
            catch { return false; }
        }

        private static bool VerificarEspacioDisco()
        {
            try
            {
                var drive = Path.GetPathRoot(Environment.SystemDirectory);
                if (string.IsNullOrEmpty(drive)) return true;
                var info = new DriveInfo(drive);
                return info.AvailableFreeSpace > 200 * 1024 * 1024; // 200 MB
            }
            catch { return true; }
        }

        private ComboBox? _cbPeriodo;
        private TextBox? _txtIntervalo;
        private CheckBox? _chkNotif;
        private TextBox? _txtMagnitud;
        private CheckBox? _chkMonitoreo;

        private void PasoConfiguracion()
        {
            panelContent.Children.Add(new TextBlock
            {
                Text = "Configuración inicial",
                FontSize = 20,
                FontWeight = FontWeights.Bold,
                Margin = new Thickness(0, 0, 0, 12)
            });
            panelContent.Children.Add(new TextBlock
            {
                Text = "Ajusta las opciones por defecto. Podrás cambiarlas después desde la aplicación.",
                TextWrapping = TextWrapping.Wrap,
                FontSize = 14,
                Margin = new Thickness(0, 0, 0, 20)
            });

            var grid = new Grid();
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(280) });
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
            grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
            grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
            grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
            grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });

            _cbPeriodo = new ComboBox
            {
                Width = 220,
                SelectedIndex = _config.PeriodoInicial,
                Margin = new Thickness(0, 4, 0, 12)
            };
            _cbPeriodo.Items.Add("Última Hora");
            _cbPeriodo.Items.Add("Último Día");
            _cbPeriodo.Items.Add("Última Semana");
            _cbPeriodo.Items.Add("Último Mes");
            _cbPeriodo.SelectedIndex = Math.Min(_config.PeriodoInicial, _cbPeriodo.Items.Count - 1);

            _txtIntervalo = new TextBox
            {
                Text = _config.IntervaloMonitoreoMinutos.ToString(),
                Width = 60,
                Margin = new Thickness(0, 4, 0, 12)
            };

            _chkNotif = new CheckBox
            {
                Content = "Activar notificaciones para sismos significativos",
                IsChecked = _config.NotificacionesActivas,
                Margin = new Thickness(0, 8, 0, 4)
            };
            _txtMagnitud = new TextBox
            {
                Text = _config.MagnitudMinimaNotificacion.ToString("N1"),
                Width = 50,
                Margin = new Thickness(0, 4, 0, 12)
            };

            _chkMonitoreo = new CheckBox
            {
                Content = "Activar monitoreo automático al iniciar",
                IsChecked = _config.MonitoreoAutomaticoPorDefecto,
                Margin = new Thickness(0, 8, 0, 0)
            };

            AgregarFila(grid, 0, "Período de datos por defecto:", _cbPeriodo);
            AgregarFila(grid, 1, "Intervalo de monitoreo automático (minutos):", _txtIntervalo);
            AgregarFila(grid, 2, "Magnitud mínima para notificar:", _txtMagnitud);
            grid.Children.Add(_chkNotif);
            Grid.SetRow(_chkNotif, 3); Grid.SetColumn(_chkNotif, 1);
            grid.Children.Add(_chkMonitoreo);
            Grid.SetRow(_chkMonitoreo, 4); Grid.SetColumn(_chkMonitoreo, 1);

            panelContent.Children.Add(grid);
        }

        private void AgregarFila(Grid grid, int row, string etiqueta, FrameworkElement control)
        {
            var lbl = new TextBlock
            {
                Text = etiqueta,
                VerticalAlignment = VerticalAlignment.Center,
                Margin = new Thickness(0, 0, 12, 0)
            };
            grid.Children.Add(lbl);
            Grid.SetRow(lbl, row);
            Grid.SetColumn(lbl, 0);
            grid.Children.Add(control);
            Grid.SetRow(control, row);
            Grid.SetColumn(control, 1);
        }

        private void PasoFinalizar()
        {
            // Guardar desde paso 3
            if (_cbPeriodo != null) _config.PeriodoInicial = _cbPeriodo.SelectedIndex;
            if (_txtIntervalo != null && int.TryParse(_txtIntervalo.Text, out var min)) _config.IntervaloMonitoreoMinutos = Math.Max(1, Math.Min(60, min));
            if (_chkNotif != null) _config.NotificacionesActivas = _chkNotif.IsChecked == true;
            if (_txtMagnitud != null && double.TryParse(_txtMagnitud.Text.Replace(",", "."), System.Globalization.NumberStyles.Any, System.Globalization.CultureInfo.InvariantCulture, out var mag)) _config.MagnitudMinimaNotificacion = Math.Max(1, Math.Min(10, mag));
            if (_chkMonitoreo != null) _config.MonitoreoAutomaticoPorDefecto = _chkMonitoreo.IsChecked == true;

            panelContent.Children.Add(new TextBlock
            {
                Text = "Instalación lista",
                FontSize = 20,
                FontWeight = FontWeights.Bold,
                Margin = new Thickness(0, 0, 0, 12)
            });
            panelContent.Children.Add(new TextBlock
            {
                Text = "La configuración se ha guardado. Haz clic en \"Finalizar\" para iniciar el Detector de Sismos.",
                TextWrapping = TextWrapping.Wrap,
                FontSize = 14,
                Margin = new Thickness(0, 0, 0, 16)
            });
            panelContent.Children.Add(new TextBlock
            {
                Text = "Resumen:",
                FontSize = 16,
                FontWeight = FontWeights.SemiBold,
                Margin = new Thickness(0, 8, 0, 8)
            });
            var resumen = new StackPanel { Margin = new Thickness(0, 0, 0, 0) };
            resumen.Children.Add(LineaResumen("Período inicial:", new[] { "Última Hora", "Último Día", "Última Semana", "Último Mes" }[_config.PeriodoInicial]));
            resumen.Children.Add(LineaResumen("Intervalo monitoreo:", $"{_config.IntervaloMonitoreoMinutos} min"));
            resumen.Children.Add(LineaResumen("Notificaciones:", _config.NotificacionesActivas ? $"Sí (magnitud ≥ {_config.MagnitudMinimaNotificacion:N1})" : "No"));
            resumen.Children.Add(LineaResumen("Monitoreo al iniciar:", _config.MonitoreoAutomaticoPorDefecto ? "Sí" : "No"));
            panelContent.Children.Add(resumen);
        }

        private static StackPanel LineaResumen(string label, string value)
        {
            var p = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(0, 4, 0, 0) };
            p.Children.Add(new TextBlock { Text = label, FontWeight = FontWeights.SemiBold, Width = 180 });
            p.Children.Add(new TextBlock { Text = value });
            return p;
        }

        private void btnAtras_Click(object sender, RoutedEventArgs e)
        {
            if (_pasoActual > 1)
                MostrarPaso(_pasoActual - 1);
        }

        private void btnSiguiente_Click(object sender, RoutedEventArgs e)
        {
            if (_pasoActual < TotalPasos)
                MostrarPaso(_pasoActual + 1);
        }

        private void btnFinalizar_Click(object sender, RoutedEventArgs e)
        {
            ConfigService.Guardar(_config);
            DialogResult = true;
            Close();
        }

        private void btnCancelar_Click(object sender, RoutedEventArgs e)
        {
            if (MessageBox.Show("¿Salir del asistente? La instalación no se completará.", "Confirmar",
                MessageBoxButton.YesNo, MessageBoxImage.Question) == MessageBoxResult.Yes)
            {
                DialogResult = false;
                Close();
            }
        }
    }
}

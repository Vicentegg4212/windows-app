using System;
using System.IO;
using System.Threading.Tasks;
using System.Windows;

namespace DetectorSismos
{
    public partial class App : Application
    {
        protected override async void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            var loading = new LoadingWindow();
            loading.Show();
            await Task.Delay(200).ConfigureAwait(true);

            try
            {
                loading.SetMensaje("Verificando carpetas del sistema...");
                await Task.Delay(350).ConfigureAwait(true);

                string appDataPath = Path.Combine(
                    Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "DetectorSismos"
                );
                string configFile = Path.Combine(appDataPath, "first_run.txt");

                if (!Directory.Exists(appDataPath))
                {
                    Directory.CreateDirectory(appDataPath);
                }

                loading.SetMensaje("Cargando configuración...");
                await Task.Delay(300).ConfigureAwait(true);

                bool esPrimeraVez = !File.Exists(configFile);

                loading.SetMensaje("Preparando interfaz...");
                await Task.Delay(400).ConfigureAwait(true);
                loading.SetMensaje("Iniciando SASMEX...");
                await Task.Delay(300).ConfigureAwait(true);

                loading.Close();
                loading = null;

                // Abrir la siguiente ventana en el hilo de la UI para evitar que se quede colgada
                bool primeraVez = esPrimeraVez;
                await Application.Current.Dispatcher.InvokeAsync(() =>
                {
                    if (primeraVez)
                    {
                        var installWizard = new InstallWizardWindow();
                        bool? result = installWizard.ShowDialog();

                        if (result == true)
                        {
                            try
                            {
                                File.WriteAllText(configFile, DateTime.Now.ToString());
                            }
                            catch { /* ya creada la carpeta */ }
                        }
                        else
                        {
                            Shutdown();
                            return;
                        }
                    }

                    var mainWindow = new MainWindow();
                    mainWindow.Show();
                });
            }
            catch (Exception ex)
            {
                try { loading?.Close(); } catch { }
                MessageBox.Show(
                    "No se pudo iniciar la aplicación.\n\nError: " + ex.Message,
                    "DetectorSismos",
                    MessageBoxButton.OK,
                    MessageBoxImage.Error);
                Shutdown();
            }
        }
    }
}

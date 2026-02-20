using System;
using System.IO;
using System.Windows;
using System.Windows.Threading;

namespace DetectorSismos
{
    public partial class App : Application
    {
        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            string appDataPath = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "DetectorSismos"
            );
            string configFile = Path.Combine(appDataPath, "first_run.txt");
            if (!Directory.Exists(appDataPath))
                Directory.CreateDirectory(appDataPath);
            bool esPrimeraVez = !File.Exists(configFile);

            var loading = new LoadingWindow();
            loading.Show();

            // Ejecutar la apertura de la siguiente ventana en el siguiente ciclo del dispatcher
            // (así la barra se pinta y no hay problemas de hilo)
            Dispatcher.BeginInvoke(DispatcherPriority.Loaded, new Action(() =>
            {
                try
                {
                    loading.Close();
                    loading = null;

                    if (esPrimeraVez)
                    {
                        try
                        {
                            var installWizard = new InstallWizardWindow();
                            if (installWizard.ShowDialog() == true)
                                try { File.WriteAllText(configFile, DateTime.Now.ToString()); } catch { }
                            else
                            {
                                Shutdown();
                                return;
                            }
                        }
                        catch (Exception exWizard)
                        {
                            // Si el asistente falla (ej. recurso XAML), ir directo a principal
                            MessageBox.Show("Configuración omitida: " + exWizard.Message, "Aviso", MessageBoxButton.OK, MessageBoxImage.Warning);
                        }
                    }

                    var mainWindow = new MainWindow();
                    mainWindow.Show();
                }
                catch (Exception ex)
                {
                    try { loading?.Close(); } catch { }
                    MessageBox.Show(
                        "No se pudo iniciar la aplicación.\n\nError: " + ex.Message + "\n\n" + ex.StackTrace,
                        "DetectorSismos",
                        MessageBoxButton.OK,
                        MessageBoxImage.Error);
                    Shutdown();
                }
            }));
        }
    }
}

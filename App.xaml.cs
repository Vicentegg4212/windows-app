using System;
using System.IO;
using System.Windows;
using System.Windows.Threading;

namespace DetectorSismos
{
    public partial class App : Application
    {
        public App()
        {
            AppDomain.CurrentDomain.UnhandledException += (_, args) =>
            {
                var ex = (Exception)args.ExceptionObject;
                EscribirError("UnhandledException", ex);
                try { MessageBox.Show(ex.Message + "\n\nVer: " + RutaLog(), "DetectorSismos - Error", MessageBoxButton.OK, MessageBoxImage.Error); } catch { }
            };

            DispatcherUnhandledException += (_, args) =>
            {
                EscribirError("DispatcherUnhandledException", args.Exception);
                MessageBox.Show(args.Exception.Message + "\n\nVer: " + RutaLog(), "DetectorSismos - Error", MessageBoxButton.OK, MessageBoxImage.Error);
                args.Handled = true;
                Shutdown();
            };
        }

        private static string RutaLog()
        {
            string dir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "DetectorSismos");
            if (!Directory.Exists(dir)) Directory.CreateDirectory(dir);
            return Path.Combine(dir, "error_log.txt");
        }

        private static void EscribirError(string tipo, Exception ex)
        {
            try
            {
                File.AppendAllText(RutaLog(), $"\r\n--- {DateTime.Now:yyyy-MM-dd HH:mm:ss} [{tipo}] ---\r\n{ex}\r\n");
            }
            catch { }
        }

        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            try
            {
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
                                EscribirError("InstallWizard", exWizard);
                                MessageBox.Show("Configuración omitida: " + exWizard.Message, "Aviso", MessageBoxButton.OK, MessageBoxImage.Warning);
                            }
                        }

                        var mainWindow = new MainWindow();
                        mainWindow.Show();
                    }
                    catch (Exception ex)
                    {
                        try { loading?.Close(); } catch { }
                        EscribirError("Startup", ex);
                        MessageBox.Show(
                            ex.Message + "\n\nDetalle en: " + RutaLog(),
                            "DetectorSismos - Error",
                            MessageBoxButton.OK,
                            MessageBoxImage.Error);
                        Shutdown();
                    }
                }));
            }
            catch (Exception ex)
            {
                EscribirError("OnStartup", ex);
                MessageBox.Show(
                    "Error al iniciar: " + ex.Message + "\n\n" + ex.StackTrace + "\n\nGuardado en: " + RutaLog(),
                    "DetectorSismos - Error",
                    MessageBoxButton.OK,
                    MessageBoxImage.Error);
                Shutdown();
            }
        }
    }
}

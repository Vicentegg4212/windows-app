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
            await Task.Delay(150);

            try
            {
                loading.SetMensaje("Verificando carpetas del sistema...");
                await Task.Delay(350);

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
                await Task.Delay(300);

                bool esPrimeraVez = !File.Exists(configFile);

                loading.SetMensaje("Preparando interfaz...");
                await Task.Delay(400);
                loading.SetMensaje("Iniciando SASMEX...");
                await Task.Delay(250);

                loading.Close();

                if (esPrimeraVez)
                {
                    var installWizard = new InstallWizardWindow();
                    bool? result = installWizard.ShowDialog();

                    if (result == true)
                    {
                        File.WriteAllText(configFile, DateTime.Now.ToString());
                    }
                    else
                    {
                        Shutdown();
                        return;
                    }
                }

                var mainWindow = new MainWindow();
                mainWindow.Show();
            }
            catch (Exception)
            {
                loading.Close();
                throw;
            }
        }
    }
}

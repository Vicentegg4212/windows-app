using System;
using System.IO;
using System.Windows;

namespace DetectorSismos
{
    public partial class App : Application
    {
        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            // Verificar si es la primera vez que se ejecuta
            string appDataPath = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "DetectorSismos"
            );
            string configFile = Path.Combine(appDataPath, "first_run.txt");

            // Si no existe la carpeta, crearla
            if (!Directory.Exists(appDataPath))
            {
                Directory.CreateDirectory(appDataPath);
            }

            // Si es la primera vez, mostrar ventana de bienvenida
            if (!File.Exists(configFile))
            {
                var welcomeWindow = new WelcomeWindow();
                bool? result = welcomeWindow.ShowDialog();

                if (result == true)
                {
                    // Marcar como ya ejecutado
                    File.WriteAllText(configFile, DateTime.Now.ToString());
                }
                else
                {
                    // Usuario canceló, cerrar aplicación
                    Shutdown();
                    return;
                }
            }

            // Mostrar ventana principal
            var mainWindow = new MainWindow();
            mainWindow.Show();
        }
    }
}

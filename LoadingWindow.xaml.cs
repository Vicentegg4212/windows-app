using System.Windows;

namespace DetectorSismos
{
    public partial class LoadingWindow : Window
    {
        public LoadingWindow()
        {
            InitializeComponent();
        }

        public void SetMensaje(string mensaje)
        {
            txtMensaje.Text = mensaje;
        }
    }
}

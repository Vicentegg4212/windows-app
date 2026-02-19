; Script de instalación para Detector de Sismos
; Para compilar este instalador necesitas Inno Setup: https://jrsoftware.org/isinfo.php

#define MyAppName "Detector de Sismos"
#define MyAppVersion "1.0"
#define MyAppPublisher "Detector Sismos Team"
#define MyAppExeName "DetectorSismos.exe"

[Setup]
; Información de la aplicación
AppId={{A8F9D123-4567-89AB-CDEF-123456789ABC}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\DetectorSismos
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
; Archivos de salida
OutputDir=.
OutputBaseFilename=DetectorSismos_Setup
; Icono y compresión
Compression=lzma
SolidCompression=yes
WizardStyle=modern
; Requisitos
ArchitecturesInstallIn64BitMode=x64
PrivilegesRequired=lowest

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear un acceso directo en el {cm:Desktop}"; GroupDescription: "Accesos directos adicionales:"; Flags: unchecked
Name: "quicklaunchicon"; Description: "Crear un acceso directo en la barra de inicio rápido"; GroupDescription: "Accesos directos adicionales:"; Flags: unchecked; OnlyBelowVersion: 6.1; Check: not IsAdminInstallMode

[Files]
Source: "bin\Release\net8.0-windows\win-x64\publish\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
procedure InitializeWizard();
var
  WelcomePage: TWizardPage;
begin
  WizardForm.WelcomeLabel1.Caption := 'Bienvenido al Asistente de Instalación de Detector de Sismos';
  WizardForm.WelcomeLabel2.Caption := 
    'Esta aplicación te permitirá monitorear sismos en tiempo real desde cualquier parte del mundo.' + #13#10 + #13#10 +
    'Características:' + #13#10 +
    '• Detección de sismos en tiempo real usando datos de USGS' + #13#10 +
    '• Monitoreo automático cada 5 minutos' + #13#10 +
    '• Notificaciones para sismos significativos' + #13#10 +
    '• Alertas de tsunami' + #13#10 +
    '• Información detallada: magnitud, ubicación, profundidad' + #13#10 + #13#10 +
    'Haz clic en Siguiente para continuar.';
end;

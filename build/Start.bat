@echo off
mode 200,60
powershell -command "& {Add-Type -AssemblyName System.Windows.Forms; $screen=[System.Windows.Forms.Screen]::PrimaryScreen.Bounds; rundll32 user32.dll,SetForegroundWindow ([Console]::WindowHandle); rundll32 user32.dll,SetWindowPos ([Console]::WindowHandle),0,0,0,$screen.Width,$screen.Height,0x0040 }"
java -jar FireWork.jar
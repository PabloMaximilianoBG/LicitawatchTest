# Levanta todo LicitaWatch en el orden correcto.
# CORRE ESTE SCRIPT COMO ADMINISTRADOR (clic derecho > Ejecutar con PowerShell,
# o abre PowerShell como admin y ejecuta: .\start-all.ps1)
#
# Necesita admin por dos cosas puntuales:
#  - Agregar exclusiones de Windows Defender en C:\jtmp y C:\dev\keycloak (evita que
#    el antivirus intercepte el socket AF_UNIX que usa el Selector de Java 21 en
#    Windows durante el arranque de Tomcat/Netty — sin esto, Keycloak y los
#    microservicios fallan con "Unable to establish loopback connection").
#  - Arrancar el servicio de RabbitMQ.

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$jtmp = "C:\jtmp"
if (-not (Test-Path $jtmp)) { New-Item -ItemType Directory -Path $jtmp | Out-Null }

Write-Host "0) Exclusiones de Defender + RabbitMQ..."
Add-MpPreference -ExclusionPath 'C:\jtmp'
Add-MpPreference -ExclusionPath 'C:\dev\keycloak'
Start-Service RabbitMQ
& "C:\Program Files\RabbitMQ Server\rabbitmq_server-*\sbin\rabbitmq-plugins.bat" enable rabbitmq_management | Out-Null

$java = "C:\Program Files\Java\jdk-21\bin\java.exe"
$jvmArgs = "-Djava.io.tmpdir=C:/jtmp"

function Start-Jar($name, $path) {
    $jar = Get-ChildItem "$path\target\*.jar" | Where-Object { $_.Name -notlike "*original*" } | Select-Object -First 1
    Write-Host "Starting $name ($($jar.Name))..."
    Start-Process -FilePath $java -ArgumentList "$jvmArgs -jar `"$($jar.FullName)`"" -WorkingDirectory $path -WindowStyle Normal
}

Write-Host "1) Keycloak..."
Start-Process -FilePath "C:\dev\keycloak\keycloak-26.7.3\bin\kc.bat" -ArgumentList "start-dev" -WorkingDirectory "C:\dev\keycloak\keycloak-26.7.3\bin" -WindowStyle Normal
Start-Sleep -Seconds 2

Write-Host "2) usuarios-ms, ingesta-ms, coincidencias-ms, notificaciones-ms..."
Start-Jar "usuarios-ms" "$root\backend\usuarios-ms"
Start-Jar "ingesta-ms" "$root\backend\ingesta-ms"
Start-Jar "coincidencias-ms" "$root\backend\coincidencias-ms"
Start-Jar "notificaciones-ms" "$root\backend\notificaciones-ms"
Start-Sleep -Seconds 5

Write-Host "3) api-gateway..."
Start-Jar "api-gateway" "$root\backend\api-gateway"

Write-Host ""
Write-Host "Listo. Cada servicio abrio su propia ventana. Cuando todos digan 'Started ... Application', corre:"
Write-Host "  cd frontend; npm run dev"
Write-Host "y abre http://localhost:5173"

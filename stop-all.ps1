<#
.SYNOPSIS
    LicitaWatch - detiene los procesos backend + Gateway + frontend
    buscando que proceso esta escuchando en cada puerto conocido (desde
    .env, mas el 5173 fijo del frontend) y matandolo. Funciona sin importar
    como se hayan iniciado (start-all.ps1, mvn spring-boot:run / npm run dev
    a mano, o desde el IDE).

.EXAMPLE
    .\stop-all.ps1
#>

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path

$envFile = Join-Path $raiz ".env"
if (-not (Test-Path $envFile)) {
    Write-Error "No se encontro .env en la raiz del proyecto ($envFile)."
    exit 1
}

Get-Content $envFile | Where-Object { $_ -match '^\s*[^#]' -and $_ -match '=' } | ForEach-Object {
    $partes = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($partes[0].Trim(), $partes[1].Trim(), "Process")
}

$servicios = @(
    @{ Nombre = "api-usuarios";       Puerto = [int]$env:PORT_USUARIOS }
    @{ Nombre = "api-licitaciones";   Puerto = [int]$env:PORT_LICITACIONES }
    @{ Nombre = "api-ms-ventas";      Puerto = [int]$env:PORT_VENTAS }
    @{ Nombre = "api-notificaciones"; Puerto = [int]$env:PORT_NOTIFICACIONES }
    @{ Nombre = "api-asistente";      Puerto = [int]$env:PORT_ASISTENTE }
    @{ Nombre = "gateway";            Puerto = [int]$env:PORT_GATEWAY }
    @{ Nombre = "frontend";           Puerto = 5173 }
)

foreach ($servicio in $servicios) {
    $conexiones = Get-NetTCPConnection -LocalPort $servicio.Puerto -State Listen -ErrorAction SilentlyContinue
    if (-not $conexiones) {
        Write-Host "$($servicio.Nombre) (puerto $($servicio.Puerto)): no esta corriendo."
        continue
    }

    $idsProceso = $conexiones.OwningProcess | Sort-Object -Unique
    foreach ($procId in $idsProceso) {
        try {
            Get-Process -Id $procId -ErrorAction Stop | Out-Null
            Write-Host "Deteniendo $($servicio.Nombre) (PID $procId, puerto $($servicio.Puerto))..."
            Stop-Process -Id $procId -Force
        } catch {
            Write-Warning "No se pudo detener el proceso $procId de $($servicio.Nombre): $_"
        }
    }
}

Write-Host "Listo."

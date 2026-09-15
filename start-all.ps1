<#
.SYNOPSIS
    LicitaWatch - levanta los 5 microservicios backend + el API Gateway +
    el frontend, en el orden correcto, cada uno con su log en
    logs\<nombre>.out.log / logs\<nombre>.err.log.

.DESCRIPTION
    Reemplaza a un "docker compose up" (este proyecto corre 100% nativo,
    sin Docker). Cada servicio Java se levanta con "mvn spring-boot:run" y
    el frontend con "npm run dev", todos en procesos en segundo plano. El
    script espera a que cada puerto responda antes de continuar con el
    siguiente, con timeout y aviso claro si algo no arranca.

    Requiere que ya hayas corrido db\init.sql una vez (ver README) y que el
    servicio de PostgreSQL este corriendo. Si frontend\node_modules no
    existe todavia, corre "npm install" ahi antes de levantarlo.

.EXAMPLE
    .\start-all.ps1
#>

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $raiz

$envFile = Join-Path $raiz ".env"
if (-not (Test-Path $envFile)) {
    Write-Error "No se encontro .env en la raiz del proyecto ($envFile). Copia .env.example y completa los valores reales antes de continuar."
    exit 1
}

Write-Host "Cargando variables de entorno desde .env..."
Get-Content $envFile | Where-Object { $_ -match '^\s*[^#]' -and $_ -match '=' } | ForEach-Object {
    $partes = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($partes[0].Trim(), $partes[1].Trim(), "Process")
}

$logsDir = Join-Path $raiz "logs"
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

$pgService = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue
if (-not $pgService -or $pgService.Status -ne "Running") {
    Write-Warning "No se detecto un servicio de PostgreSQL corriendo. Los microservicios con base de datos propia no van a poder conectarse."
}

function Iniciar-Servicio {
    param([string]$Nombre, [string]$DirectorioRelativo)

    Write-Host "Iniciando $Nombre..."
    $outLog = Join-Path $logsDir "$Nombre.out.log"
    $errLog = Join-Path $logsDir "$Nombre.err.log"
    Remove-Item $outLog, $errLog -ErrorAction SilentlyContinue

    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" `
        -WorkingDirectory (Join-Path $raiz $DirectorioRelativo) `
        -RedirectStandardOutput $outLog -RedirectStandardError $errLog `
        -WindowStyle Hidden | Out-Null
}

function Esperar-Puerto {
    param([string]$Nombre, [int]$Puerto, [int]$TimeoutSegundos = 130)

    Write-Host -NoNewline "  Esperando a que $Nombre responda en el puerto $Puerto"
    $inicio = Get-Date
    while (((Get-Date) - $inicio).TotalSeconds -lt $TimeoutSegundos) {
        try {
            $cliente = New-Object System.Net.Sockets.TcpClient
            $cliente.Connect("127.0.0.1", $Puerto)
            $cliente.Close()
            Write-Host " listo."
            return $true
        } catch {
            Start-Sleep -Seconds 2
            Write-Host -NoNewline "."
        }
    }
    Write-Host " TIMEOUT."
    Write-Warning "$Nombre no respondio en $TimeoutSegundos segundos. Revisa logs\$Nombre.err.log y logs\$Nombre.out.log"
    return $false
}

# Orden: Usuarios primero (todos dependen de su JWT/seed de roles), despues
# Licitaciones/MS-Ventas/Notificaciones (independientes entre si), despues
# Asistente (consulta a los anteriores en tiempo de request, no al arrancar),
# y el Gateway al final (es la puerta de entrada, tiene sentido que abra
# ultimo).

Iniciar-Servicio "api-usuarios" "backend\api-usuarios"
Esperar-Puerto "api-usuarios" ([int]$env:PORT_USUARIOS)

Iniciar-Servicio "api-licitaciones" "backend\api-licitaciones"
Esperar-Puerto "api-licitaciones" ([int]$env:PORT_LICITACIONES)

Iniciar-Servicio "api-ms-ventas" "backend\api-ms-ventas"
Esperar-Puerto "api-ms-ventas" ([int]$env:PORT_VENTAS)

Iniciar-Servicio "api-notificaciones" "backend\api-notificaciones"
Esperar-Puerto "api-notificaciones" ([int]$env:PORT_NOTIFICACIONES)

Iniciar-Servicio "api-asistente" "backend\api-asistente"
Esperar-Puerto "api-asistente" ([int]$env:PORT_ASISTENTE)

Iniciar-Servicio "gateway" "gateway"
Esperar-Puerto "gateway" ([int]$env:PORT_GATEWAY)

# Frontend al final: es lo unico que un usuario final abre en el navegador,
# tiene sentido que sea lo ultimo en levantar una vez que el Gateway (y por
# lo tanto todo el backend detras de el) ya responde.
$nodeModules = Join-Path $raiz "frontend\node_modules"
if (-not (Test-Path $nodeModules)) {
    Write-Host "Instalando dependencias del frontend (primera vez)..."
    Push-Location (Join-Path $raiz "frontend")
    npm.cmd install
    Pop-Location
}

Write-Host "Iniciando frontend..."
$frontendOutLog = Join-Path $logsDir "frontend.out.log"
$frontendErrLog = Join-Path $logsDir "frontend.err.log"
Remove-Item $frontendOutLog, $frontendErrLog -ErrorAction SilentlyContinue
# "npm" por si solo resuelve a npm.ps1 (script de PowerShell), que
# Start-Process no puede ejecutar directamente como si fuera un ejecutable
# nativo ("no es una aplicacion Win32 valida"). npm.cmd si es invocable.
Start-Process -FilePath "npm.cmd" -ArgumentList "run", "dev" `
    -WorkingDirectory (Join-Path $raiz "frontend") `
    -RedirectStandardOutput $frontendOutLog -RedirectStandardError $frontendErrLog `
    -WindowStyle Hidden | Out-Null
Esperar-Puerto "frontend" 5173

Write-Host ""
Write-Host "Todos los procesos fueron lanzados. Logs en: $logsDir"
Write-Host "Abre la app en: http://localhost:5173"
Write-Host "Prueba rapida de la API: curl http://localhost:$($env:PORT_GATEWAY)/api/auth/login -Method POST ..."
Write-Host "Para detener todo: .\stop-all.ps1"

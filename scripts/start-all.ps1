<#
  LicitaWatch v2 - arranque on-premise de los 19 microservicios + frontend (Windows, sin Docker).
  Orden por capas (PPT diap. 12): MS.<d>.bd -> MS.ventas.ambassador -> MS.<d>.bs -> MS.bff.<d> -> api-gateway -> frontend.
  Cada capa se inicia en paralelo y se espera a que responda /actuator/health antes de la siguiente.

  Uso (desde la raíz del proyecto):
    powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1            # compila si faltan jars y levanta todo
    powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1 -Build     # fuerza recompilación (mvn install)
    powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1 -SinFrontend

  Requisitos: PostgreSQL con las 5 bases creadas (scripts\crear-bases.ps1) y el archivo .env en la raíz.
  Logs: carpeta .run\ (un archivo por servicio).
#>
param(
    [switch]$Build,
    [switch]$SinFrontend
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if (-not (Test-Path (Join-Path $root '.env'))) {
    Write-Host 'Falta el archivo .env en la raiz. Copia .env.example como .env y completalo.' -ForegroundColor Red
    exit 1
}
New-Item -ItemType Directory -Force (Join-Path $root '.run') | Out-Null
# Directorio corto y SIN espacios para los sockets internos (AF_UNIX) de la JVM en Windows.
$tmp = Join-Path $env:SystemDrive 'licitawatch-tmp'
try { New-Item -ItemType Directory -Force $tmp -ErrorAction Stop | Out-Null }
catch { $tmp = Join-Path $root '.run\tmp'; New-Item -ItemType Directory -Force $tmp | Out-Null }

$capas = @(
    @(
        @{ modulo = 'usuarios\ms-usuarios-bd';             artefacto = 'ms-usuarios-bd';             puerto = 8311 },
        @{ modulo = 'licitaciones\ms-licitaciones-bd';     artefacto = 'ms-licitaciones-bd';         puerto = 8312 },
        @{ modulo = 'ventas\ms-ventas-bd';                 artefacto = 'ms-ventas-bd';               puerto = 8313 },
        @{ modulo = 'notificaciones\ms-notificaciones-bd'; artefacto = 'ms-notificaciones-bd';       puerto = 8314 },
        @{ modulo = 'chat\ms-chat-bd';                     artefacto = 'ms-chat-bd';                 puerto = 8316 },
        @{ modulo = 'ventas\ms-ventas-ambassador';         artefacto = 'ms-ventas-ambassador';       puerto = 8413 }
    ),
    @(
        @{ modulo = 'usuarios\ms-usuarios-bs';             artefacto = 'ms-usuarios-bs';             puerto = 8211 },
        @{ modulo = 'licitaciones\ms-licitaciones-bs';     artefacto = 'ms-licitaciones-bs';         puerto = 8212 },
        @{ modulo = 'ventas\ms-ventas-bs';                 artefacto = 'ms-ventas-bs';               puerto = 8213 },
        @{ modulo = 'notificaciones\ms-notificaciones-bs'; artefacto = 'ms-notificaciones-bs';       puerto = 8214 },
        @{ modulo = 'asistente\ms-asistente-bs';           artefacto = 'ms-asistente-bs';            puerto = 8215 },
        @{ modulo = 'chat\ms-chat-bs';                     artefacto = 'ms-chat-bs';                 puerto = 8216 }
    ),
    @(
        @{ modulo = 'usuarios\ms-usuarios-bff';             artefacto = 'ms-usuarios-bff';           puerto = 8111 },
        @{ modulo = 'licitaciones\ms-licitaciones-bff';     artefacto = 'ms-licitaciones-bff';       puerto = 8112 },
        @{ modulo = 'ventas\ms-ventas-bff';                 artefacto = 'ms-ventas-bff';             puerto = 8113 },
        @{ modulo = 'notificaciones\ms-notificaciones-bff'; artefacto = 'ms-notificaciones-bff';     puerto = 8114 },
        @{ modulo = 'asistente\ms-asistente-bff';           artefacto = 'ms-asistente-bff';          puerto = 8115 },
        @{ modulo = 'chat\ms-chat-bff';                     artefacto = 'ms-chat-bff';               puerto = 8116 }
    ),
    @(
        @{ modulo = 'api-gateway';                          artefacto = 'api-gateway';               puerto = 8080 }
    )
)

function Esperar-Salud([int]$puerto, [int]$segundos = 180) {
    $limite = (Get-Date).AddSeconds($segundos)
    while ((Get-Date) -lt $limite) {
        try {
            $r = Invoke-RestMethod -Uri "http://127.0.0.1:$puerto/actuator/health" -TimeoutSec 3
            if ($r.status -eq 'UP') { return $true }
        } catch { }
        Start-Sleep -Seconds 2
    }
    return $false
}

$todos = $capas | ForEach-Object { $_ }
$faltanJars = $todos | Where-Object { -not (Test-Path (Join-Path $root "$($_.modulo)\target\$($_.artefacto)-2.0.0.jar")) }
if ($Build -or $faltanJars) {
    Write-Host 'Compilando LicitaWatch (mvn -DskipTests install)...' -ForegroundColor Cyan
    & mvn -q -DskipTests install
    if ($LASTEXITCODE -ne 0) { Write-Host 'Error de compilacion' -ForegroundColor Red; exit 1 }
}

foreach ($capa in $capas) {
    $iniciados = @()
    foreach ($s in $capa) {
        if (Get-NetTCPConnection -LocalPort $s.puerto -State Listen -ErrorAction SilentlyContinue) {
            Write-Host ("{0,-24} ya esta corriendo en {1}" -f $s.artefacto, $s.puerto) -ForegroundColor Yellow
            continue
        }
        $dir = Join-Path $root $s.modulo
        $jar = "target\$($s.artefacto)-2.0.0.jar"
        $log = Join-Path $root ".run\$($s.artefacto).log"
        Write-Host ("Iniciando {0,-24} puerto {1}" -f $s.artefacto, $s.puerto)
        $p = Start-Process -FilePath 'java' -ArgumentList "-Djdk.net.unixdomain.tmpdir=$tmp", '-Dfile.encoding=UTF-8', '-Xms64m', '-Xmx320m',
            '-XX:TieredStopAtLevel=1', '-XX:+UseSerialGC', '-jar', $jar `
            -WorkingDirectory $dir -RedirectStandardOutput $log -RedirectStandardError "$log.err" -WindowStyle Hidden -PassThru
        $p.Id | Out-File (Join-Path $root ".run\$($s.artefacto).pid")
        $iniciados += $s
    }
    foreach ($s in $iniciados) {
        if (Esperar-Salud $s.puerto) { Write-Host ("  {0,-24} OK" -f $s.artefacto) -ForegroundColor Green }
        else { Write-Host ("  {0,-24} NO RESPONDE (revisa .run\{0}.log)" -f $s.artefacto) -ForegroundColor Red }
    }
}

if (-not $SinFrontend) {
    $front = Join-Path $root 'frontend'
    if (Test-Path $front) {
        if (-not (Test-Path (Join-Path $front 'node_modules'))) {
            Write-Host 'Instalando dependencias del frontend (npm install)...' -ForegroundColor Cyan
            Push-Location $front; & npm install; Pop-Location
        }
        if (-not (Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue)) {
            Write-Host 'Iniciando frontend en http://localhost:5173 ...'
            $p = Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', 'npm run dev' -WorkingDirectory $front `
                -RedirectStandardOutput (Join-Path $root '.run\frontend.log') -RedirectStandardError (Join-Path $root '.run\frontend.log.err') `
                -WindowStyle Hidden -PassThru
            $p.Id | Out-File (Join-Path $root '.run\frontend.pid')
        }
    }
}

Write-Host ''
Write-Host 'LicitaWatch en ejecucion:' -ForegroundColor Cyan
Write-Host '  Frontend    : http://localhost:5173'
Write-Host '  api-gateway : http://localhost:8080 (unica entrada)'
Write-Host '  Swagger     : http://localhost:<puerto>/swagger-ui.html de cada BFF/BS/BD (ver README)'
Write-Host '  Detener     : powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1'

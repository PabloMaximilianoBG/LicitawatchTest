<#
  LicitaWatch v2 - detiene los 19 microservicios y el frontend.
  Uso: powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1
#>
$puertos = 8080, 8111, 8112, 8113, 8114, 8115, 8116, 8211, 8212, 8213, 8214, 8215, 8216, 8311, 8312, 8313, 8314, 8316, 8413, 5173
foreach ($puerto in $puertos) {
    $conexiones = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conexiones) {
        try {
            Stop-Process -Id $c.OwningProcess -Force -Confirm:$false -ErrorAction Stop
            Write-Host "Detenido proceso $($c.OwningProcess) (puerto $puerto)"
        } catch { }
    }
}
$root = Split-Path -Parent $PSScriptRoot
Get-ChildItem (Join-Path $root '.run') -Filter '*.pid' -ErrorAction SilentlyContinue | Remove-Item -Force -Confirm:$false
Write-Host 'LicitaWatch detenido.'

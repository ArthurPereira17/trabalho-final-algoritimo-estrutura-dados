# run.ps1 - roda o backend (compila antes, se necessario).
# Uso:
#   .\run.ps1                          sobe o servidor em http://localhost:8080
#   .\run.ps1 --indexar "C:\textos"    indexa o diretorio e sobe o servidor
# Funciona de qualquer pasta (usa o diretorio do proprio script).
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Find-JavaBin {
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { return (Split-Path $cmd.Source -Parent) }
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { return "$env:JAVA_HOME\bin" }
    $local = "C:\Berkan\Desenvolvimento\jdk-21\bin"
    if (Test-Path "$local\java.exe") { return $local }
    throw "JDK (java) nao encontrado. Instale um JDK ou defina JAVA_HOME."
}

$binDir = Join-Path $root "bin"
if (-not (Test-Path (Join-Path $binDir "App.class"))) {
    Write-Host "bin ausente - compilando primeiro..." -ForegroundColor Yellow
    & (Join-Path $root "build.ps1")
}

$javaBin = Find-JavaBin
$javaExe = Join-Path $javaBin "java.exe"
Write-Host "Usando JDK: $javaBin" -ForegroundColor Cyan

# roda a partir de backend/ para que o indice (pasta dados/) fique aqui
Set-Location $root
& $javaExe -cp $binDir App @args

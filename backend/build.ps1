# build.ps1 — compila o backend (src + test) para a pasta bin\
# Detecta o JDK automaticamente: PATH -> JAVA_HOME -> JDK local conhecido.
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Find-JdkBin {
    $cmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($cmd) { return (Split-Path $cmd.Source -Parent) }
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) { return "$env:JAVA_HOME\bin" }
    $local = "C:\Berkan\Desenvolvimento\jdk-21\bin"
    if (Test-Path "$local\javac.exe") { return $local }
    throw "JDK (javac) nao encontrado. Instale um JDK ou defina JAVA_HOME."
}

$jdk = Find-JdkBin
Write-Host "Usando JDK: $jdk" -ForegroundColor Cyan

$junit  = Join-Path $root "lib\junit-platform-console-standalone-6.0.0-RC2.jar"
$fontes = (Get-ChildItem -Recurse (Join-Path $root "src"), (Join-Path $root "test") -Filter *.java).FullName
$bin    = Join-Path $root "bin"

Write-Host ("Compilando " + $fontes.Count + " arquivos .java ...")
& "$jdk\javac.exe" -encoding UTF-8 -cp $junit -d $bin $fontes
if ($?) { Write-Host "BUILD OK -> $bin" -ForegroundColor Green } else { Write-Host "BUILD FALHOU" -ForegroundColor Red }

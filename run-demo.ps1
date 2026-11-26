$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $jdk = 'C:\Program Files\Java\jdk-17\bin'
    if (Test-Path (Join-Path $jdk 'javac.exe')) { $env:PATH = "$jdk;$env:PATH" }
    New-Item -ItemType Directory -Force out | Out-Null
    $sources = @(Get-ChildItem -LiteralPath src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
    & javac --release 17 -encoding UTF-8 -d out @sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    & java -cp out mia.parking.gui.ParkingDashboardApp
} finally { Pop-Location }

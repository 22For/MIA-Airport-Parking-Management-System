$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force out | Out-Null
    $sources = @(Get-ChildItem -LiteralPath src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
    & javac --release 17 -encoding UTF-8 -d out @sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    Write-Host 'Compilation passed'
} finally { Pop-Location }

$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & mvn test
    if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed' }
} finally { Pop-Location }

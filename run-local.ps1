$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $projectRoot ".env"
$preferredJdk = "C:\Program Files\Java\jdk-21.0.11"

if (Test-Path -LiteralPath (Join-Path $preferredJdk "bin\java.exe")) {
    $env:JAVA_HOME = $preferredJdk
    $env:Path = (Join-Path $preferredJdk "bin") + ";" + $env:Path
}

if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing local configuration file: $envFile"
}

foreach ($line in Get-Content -LiteralPath $envFile) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith("#")) {
        continue
    }

    $parts = $trimmed.Split("=", 2)
    if ($parts.Count -ne 2) {
        throw "Invalid line in .env: $trimmed"
    }

    [Environment]::SetEnvironmentVariable(
        $parts[0].Trim(),
        $parts[1].Trim(),
        [EnvironmentVariableTarget]::Process
    )
}

Set-Location $projectRoot
& .\mvnw.cmd clean javafx:run
exit $LASTEXITCODE

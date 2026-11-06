$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$shortTemp = "C:\jtmp"

New-Item -ItemType Directory -Force -Path $shortTemp | Out-Null
$env:TEMP = $shortTemp
$env:TMP = $shortTemp
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"

if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
}

Push-Location $projectRoot
try {
    & .\gradlew.bat testDebugUnitTest lintDebug assembleDebug --no-daemon --console=plain --max-workers=1
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Host "APK: $projectRoot\app\build\outputs\apk\debug\app-debug.apk"
} finally {
    Pop-Location
}


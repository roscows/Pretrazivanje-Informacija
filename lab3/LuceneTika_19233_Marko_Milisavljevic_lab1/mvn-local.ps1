param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $MavenArgs
)

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$mavenVersion = "3.9.16"
$toolsDir = Join-Path $env:USERPROFILE ".cache\lucene-tika-lab-tools"
$mavenDir = Join-Path $toolsDir "apache-maven-$mavenVersion"
$mavenExe = Join-Path $mavenDir "bin\mvn.cmd"
$mavenLauncher = Join-Path $mavenDir "boot\plexus-classworlds-2.11.0.jar"

if (-not ((Test-Path $mavenExe) -and (Test-Path $mavenLauncher))) {
    New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null
    $zipPath = Join-Path $toolsDir "apache-maven-$mavenVersion-bin.zip"
    $downloadUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

    Write-Host "Preuzimam Apache Maven $mavenVersion..."
    Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath $toolsDir -Force
}

$env:MAVEN_OPTS = "$env:MAVEN_OPTS -Djavax.net.ssl.trustStoreType=Windows-ROOT"
& $mavenExe @MavenArgs
exit $LASTEXITCODE

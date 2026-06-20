param(
    [string]$TomcatVersion = "10.1.55"
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$toolsDir = Join-Path $projectRoot ".tools"
$tomcatDir = Join-Path $toolsDir "apache-tomcat-$TomcatVersion"
$tomcatZip = Join-Path $toolsDir "apache-tomcat-$TomcatVersion.zip"

function Get-JavaMajorVersion {
    $versionOutput = cmd /c "java -version 2>&1"
    $versionLine = ($versionOutput | Select-Object -First 1) -as [string]
    if ($versionLine -match '"(?<version>\d+)(\.(?<minor>\d+))?') {
        return [int]$Matches.version
    }
    throw "Java version could not be detected. Install Java 17 and try again."
}

if ((Get-JavaMajorVersion) -ne 17) {
    throw "Java 17 is required for this project."
}

New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null

if (-not (Test-Path $tomcatDir)) {
    if (-not (Test-Path $tomcatZip)) {
        $downloadUrl = "https://dlcdn.apache.org/tomcat/tomcat-10/v$TomcatVersion/bin/apache-tomcat-$TomcatVersion.zip"
        Invoke-WebRequest -UseBasicParsing $downloadUrl -OutFile $tomcatZip
    }

    Expand-Archive -LiteralPath $tomcatZip -DestinationPath $toolsDir -Force
}

$env:CATALINA_HOME = $tomcatDir
$env:CATALINA_BASE = $tomcatDir

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "CATALINA_HOME=$env:CATALINA_HOME"
Write-Host "CATALINA_BASE=$env:CATALINA_BASE"
Write-Host "Maven wrapper=.\mvnw.cmd"

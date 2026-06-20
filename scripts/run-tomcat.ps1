param(
    [int]$Port = 8080,
    [string]$TomcatVersion = "10.1.55"
)

$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$setupScript = Join-Path $PSScriptRoot "setup-dev.ps1"
& $setupScript -TomcatVersion $TomcatVersion

$tomcatHome = Join-Path $projectRoot ".tools\apache-tomcat-$TomcatVersion"
$serverXml = Join-Path $tomcatHome "conf\server.xml"
$warFile = Join-Path $projectRoot "target\approval-system.war"
$webappsDir = Join-Path $tomcatHome "webapps"
$deployedWar = Join-Path $webappsDir "approval-system.war"
$deployedDir = Join-Path $webappsDir "approval-system"

& (Join-Path $projectRoot "mvnw.cmd") clean package

if (Test-Path $deployedWar) {
    Remove-Item -LiteralPath $deployedWar -Force
}
if (Test-Path $deployedDir) {
    Remove-Item -LiteralPath $deployedDir -Recurse -Force
}
Copy-Item -LiteralPath $warFile -Destination $deployedWar -Force

[xml]$config = Get-Content -LiteralPath $serverXml
$connector = $config.Server.Service.Connector | Where-Object { $_.protocol -eq "HTTP/1.1" } | Select-Object -First 1
if ($null -eq $connector) {
    throw "Tomcat HTTP connector was not found in $serverXml"
}
$connector.port = [string]$Port
$config.Save($serverXml)

$env:CATALINA_HOME = $tomcatHome
$env:CATALINA_BASE = $tomcatHome

Write-Host "Starting Tomcat $TomcatVersion on http://localhost:$Port/approval-system/approval"
& (Join-Path $tomcatHome "bin\catalina.bat") run

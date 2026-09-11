param(
	[Parameter(Position = 0, ValueFromRemainingArguments = $true)]
	[string[]] $Tasks = @("runClient")
)

$ErrorActionPreference = "Stop"
$javaExe = $null

if ($env:JAVA_HOME) {
	$candidate = Join-Path $env:JAVA_HOME "bin\java.exe"
	if (Test-Path -LiteralPath $candidate) {
		$javaExe = $candidate
	}
}

if (-not $javaExe) {
	$javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
	if ($javaCommand) {
		$javaExe = $javaCommand.Source
	}
}

if (-not $javaExe) {
	throw "JDK 21 was not found. Install JDK 21 and set JAVA_HOME or add java.exe to PATH."
}

$previousErrorAction = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$javaVersionOutput = & $javaExe -version 2>&1
$javaVersionExitCode = $LASTEXITCODE
$ErrorActionPreference = $previousErrorAction
if ($javaVersionExitCode -ne 0) {
	throw "Could not run Java at $javaExe"
}
if (($javaVersionOutput -join " ") -notmatch 'version "21[.]') {
	throw "VisualParticle Better requires JDK 21. Active Java: $($javaVersionOutput[0])"
}

$drive = $null
foreach ($letter in @("Z", "Y", "W", "V", "U", "T")) {
	$candidateDrive = "{0}:\" -f $letter
	if (-not (Test-Path $candidateDrive)) {
		$drive = "{0}:" -f $letter
		break
	}
}

if (-not $drive) {
	throw "No free temporary drive letter is available."
}

& subst.exe $drive $PSScriptRoot
if ($LASTEXITCODE -ne 0) {
	throw "Could not map the project to temporary drive $drive"
}

Push-Location ("{0}\" -f $drive)
try {
	& $javaExe "-Dorg.gradle.appname=gradlew" "-Dfile.encoding=UTF-8" "-classpath" "gradle/wrapper/gradle-wrapper.jar" "org.gradle.wrapper.GradleWrapperMain" @Tasks
	exit $LASTEXITCODE
}
finally {
	Pop-Location
	& subst.exe $drive /D
}

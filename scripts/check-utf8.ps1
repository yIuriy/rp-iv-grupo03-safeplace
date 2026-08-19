#!/usr/bin/env pwsh

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

function Test-Utf8 {
    param (
        [Parameter(Mandatory = $true)]
        [byte[]] $Bytes
    )

    $encoding = [System.Text.UTF8Encoding]::new($false, $true)

    try {
        $null = $encoding.GetString($Bytes)
        return $true
    }
    catch {
        return $false
    }
}

function Test-BinaryContent {
    param (
        [Parameter(Mandatory = $true)]
        [byte[]] $Bytes
    )

    return $Bytes -contains 0
}

function ConvertTo-ProcessArgument {
    param (
        [Parameter(Mandatory = $true)]
        [string] $Argument
    )

    if ($Argument -notmatch '[\s"]') {
        return $Argument
    }

    $result = '"'
    $backslashCount = 0

    foreach ($character in $Argument.ToCharArray()) {
        if ($character -eq '\') {
            $backslashCount++
            continue
        }

        if ($character -eq '"') {
            $result += '\' * (($backslashCount * 2) + 1)
            $result += '"'
            $backslashCount = 0
            continue
        }

        if ($backslashCount -gt 0) {
            $result += '\' * $backslashCount
            $backslashCount = 0
        }

        $result += $character
    }

    if ($backslashCount -gt 0) {
        $result += '\' * ($backslashCount * 2)
    }

    $result += '"'

    return $result
}

function Get-StagedFileBytes {
    param (
        [Parameter(Mandatory = $true)]
        [string] $Path
    )

    $processInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = "git"
    $processInfo.Arguments = "show " + (ConvertTo-ProcessArgument -Argument ":$Path")
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.UseShellExecute = $false

    $process = [System.Diagnostics.Process]::Start($processInfo)
    $memory = [System.IO.MemoryStream]::new()
    $process.StandardOutput.BaseStream.CopyTo($memory)
    $process.WaitForExit()

    if ($process.ExitCode -ne 0) {
        $errorOutput = $process.StandardError.ReadToEnd()
        throw "Não foi possível ler o arquivo staged '$Path'. $errorOutput"
    }

    return $memory.ToArray()
}

function Invoke-GitBytes {
    param (
        [Parameter(Mandatory = $true)]
        [string] $Arguments
    )

    $processInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = "git"
    $processInfo.Arguments = $Arguments
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.UseShellExecute = $false

    $process = [System.Diagnostics.Process]::Start($processInfo)
    $memory = [System.IO.MemoryStream]::new()
    $process.StandardOutput.BaseStream.CopyTo($memory)
    $process.WaitForExit()

    if ($process.ExitCode -ne 0) {
        $errorOutput = $process.StandardError.ReadToEnd()
        throw "Falha ao executar git $Arguments. $errorOutput"
    }

    return $memory.ToArray()
}

function Get-StagedFiles {
    $bytes = Invoke-GitBytes -Arguments "-c core.quotepath=false diff --cached --name-only -z --diff-filter=ACMR"

    if ($bytes.Length -eq 0) {
        return @()
    }

    $encoding = [System.Text.UTF8Encoding]::new($false, $true)
    $files = @()
    $start = 0

    for ($index = 0; $index -lt $bytes.Length; $index++) {
        if ($bytes[$index] -ne 0) {
            continue
        }

        if ($index -gt $start) {
            $length = $index - $start
            $files += $encoding.GetString($bytes, $start, $length)
        }

        $start = $index + 1
    }

    return $files
}

$stagedFiles = Get-StagedFiles

if (-not $stagedFiles) {
    exit 0
}

$invalidFiles = @()

foreach ($file in $stagedFiles) {
    $content = Get-StagedFileBytes -Path $file

    if ($content.Length -eq 0) {
        continue
    }

    if (Test-BinaryContent -Bytes $content) {
        continue
    }

    if (-not (Test-Utf8 -Bytes $content)) {
        $invalidFiles += $file
    }
}

if ($invalidFiles.Count -gt 0) {
    Write-Host "Commit bloqueado: os arquivos abaixo não estão em UTF-8." -ForegroundColor Red
    $invalidFiles | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    Write-Host "Converta esses arquivos para UTF-8 antes de tentar novamente." -ForegroundColor Yellow
    exit 1
}

exit 0

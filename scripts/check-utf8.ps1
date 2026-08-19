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

$stagedFiles = git diff --cached --name-only --diff-filter=ACMR

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

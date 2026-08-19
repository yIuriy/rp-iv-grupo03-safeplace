#!/usr/bin/env pwsh

param (
    [Parameter(Mandatory = $true)]
    [string] $CommitMessageFile
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$allowedTypes = @(
    "build",
    "chore",
    "ci",
    "docs",
    "feat",
    "fix",
    "perf",
    "refactor",
    "revert",
    "style",
    "test"
)

$commitMessage = Get-Content -Raw -Encoding UTF8 $CommitMessageFile
$commitTitle = ($commitMessage -split "\r?\n" | Where-Object {
    $_ -and -not $_.StartsWith("#")
} | Select-Object -First 1)

if (-not $commitTitle) {
    Write-Host "Commit bloqueado: a mensagem do commit está vazia." -ForegroundColor Red
    exit 1
}

$specialCommitPatterns = @(
    "^Merge ",
    "^Revert ",
    "^fixup! ",
    "^squash! "
)

foreach ($pattern in $specialCommitPatterns) {
    if ($commitTitle -match $pattern) {
        exit 0
    }
}

$typesPattern = $allowedTypes -join "|"
$conventionalPattern = "^($typesPattern)(\([a-z0-9-]+\))?!?: .+"

if ($commitTitle -match $conventionalPattern) {
    exit 0
}

Write-Host "Commit bloqueado: use Conventional Commits na primeira linha." -ForegroundColor Red
Write-Host ""
Write-Host "Formato esperado:" -ForegroundColor Yellow
Write-Host "  tipo(escopo opcional): descrição" -ForegroundColor Yellow
Write-Host ""
Write-Host "Tipos aceitos:" -ForegroundColor Yellow
Write-Host "  $($allowedTypes -join ', ')" -ForegroundColor Yellow
Write-Host ""
Write-Host "Exemplos:" -ForegroundColor Yellow
Write-Host "  docs: atualiza README" -ForegroundColor Yellow
Write-Host "  feat(diagramas): adiciona diagrama de classes" -ForegroundColor Yellow
Write-Host "  fix!: ajusta estrutura incompatível" -ForegroundColor Yellow
Write-Host ""
Write-Host "Mensagem recebida:" -ForegroundColor Yellow
Write-Host "  $commitTitle" -ForegroundColor Yellow

exit 1

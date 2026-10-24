# Prepares the smoke-test scenarios that cannot live in the repository (Windows).
# Git cannot store a folder without read permission, and "moving" a vault must happen outside the repository, so
# this script builds them in a scratch folder. It never touches the vaults in smoke-data\vaults.
# Usage: powershell -ExecutionPolicy Bypass -File smoke-data\smoke.ps1 prepare|move|first-launch|restore-settings|clean
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("prepare", "move", "first-launch", "restore-settings", "clean")]
    [string]$Command
)
$ErrorActionPreference = "Stop"

$SmokeRoot = $PSScriptRoot
$Scratch = Join-Path $env:TEMP "notekea-smoke"
$Locked = Join-Path $Scratch "Locked Vault"
$Settings = Join-Path (Join-Path $env:APPDATA "Notekea") "settings.properties"
$Backup = "$Settings.smoke-backup"

function Clear-Scratch {
    if (Test-Path $Locked) { icacls $Locked /remove:d "$env:USERNAME" | Out-Null }
    if (Test-Path $Scratch) { Remove-Item -Recurse -Force $Scratch }
}

switch ($Command) {
    "prepare" {
        Clear-Scratch
        New-Item -ItemType Directory -Force (Join-Path $Scratch "New Vault Location\My Notes") | Out-Null
        New-Item -ItemType Directory -Force $Locked | Out-Null
        Copy-Item -Recurse (Join-Path $SmokeRoot "vaults\Personal Notes") (Join-Path $Scratch "Movable Vault")
        Set-Content (Join-Path $Scratch "New Vault Location\My Notes\Existing note.md") "# Existing note"
        Set-Content (Join-Path $Locked "Locked note.md") "# Locked note"
        icacls $Locked /deny "${env:USERNAME}:(RX)" | Out-Null
        Write-Output "Smoke scenarios ready in $Scratch (Movable Vault, Locked Vault, New Vault Location)"
    }
    "move" {
        Rename-Item (Join-Path $Scratch "Movable Vault") "Moved Vault"
        Write-Output "Moved to $(Join-Path $Scratch 'Moved Vault')"
    }
    "first-launch" {
        if (Test-Path $Settings) { Move-Item -Force $Settings $Backup }
        Write-Output "Notekea will start as on a first launch (backup: $Backup)"
    }
    "restore-settings" {
        if (Test-Path $Backup) { Move-Item -Force $Backup $Settings }
        Write-Output "Settings restored"
    }
    "clean" {
        Clear-Scratch
        Write-Output "Removed $Scratch"
    }
}

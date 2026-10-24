# Prepares the smoke-test scenarios that cannot live in the repository (Windows).
# Git cannot store a folder without read permission, and a moved vault must not change tracked files, so this script
# builds them in smoke-data\scratch (ignored by Git), next to the sample vaults. It never touches smoke-data\vaults.
# Usage: powershell -ExecutionPolicy Bypass -File smoke-data\smoke.ps1 prepare|move|lock|outside|large|clear-cache|first-launch|restore-settings|clean
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("prepare", "move", "lock", "outside", "large", "clear-cache", "first-launch", "restore-settings", "clean")]
    [string]$Command
)
$ErrorActionPreference = "Stop"

$SmokeRoot = $PSScriptRoot
$Scratch = Join-Path $SmokeRoot "scratch"
$Locked = Join-Path $Scratch "Locked Vault"
$Settings = Join-Path (Join-Path $env:APPDATA "Notekea") "settings.properties"
$Backup = "$Settings.smoke-backup"
$Database = Join-Path (Join-Path $env:APPDATA "Notekea") "notekea.db"
$Playground = Join-Path $Scratch "Playground Vault"
$Utf8 = New-Object System.Text.UTF8Encoding($false)

function Write-Note($Path, $Text) {
    [System.IO.File]::WriteAllText($Path, $Text, $Utf8)
}

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
        Copy-Item -Recurse (Join-Path $SmokeRoot "vaults\Personal Notes") $Playground
        Set-Content (Join-Path $Scratch "New Vault Location\My Notes\Existing note.md") "# Existing note"
        Set-Content (Join-Path $Locked "Locked note.md") "# Locked note"
        Write-Output "Smoke scenarios ready in $Scratch (Movable Vault, Locked Vault, New Vault Location, Playground Vault)"
    }
    "move" {
        Rename-Item (Join-Path $Scratch "Movable Vault") "Moved Vault"
        Write-Output "Moved to $(Join-Path $Scratch 'Moved Vault')"
    }
    "lock" {
        # Native folder pickers refuse to select a folder they cannot read, so it is locked after it is in Recent.
        icacls $Locked /deny "${env:USERNAME}:(RX)" | Out-Null
        Write-Output "Locked $Locked"
    }
    "outside" {
        # Changes Playground Vault the way another app or a sync would: one note created, one edited, one deleted.
        New-Item -ItemType Directory -Force (Join-Path $Playground "Inbox") | Out-Null
        Write-Note (Join-Path $Playground "Inbox\Created outside.md") "---`ntags: [inbox]`n---`n`n# Created outside`n"
        Write-Note (Join-Path $Playground "Travel\Lisbon trip.md") "---`ntags: [travel, edited]`ndates: 2026-11-14 to 2026-11-20`n---`n`n# Lisbon trip`n"
        Remove-Item -Force -ErrorAction SilentlyContinue (Join-Path $Playground "Ideas\Reading list.md")
        Write-Output "Changed ${Playground}: Inbox\Created outside.md created, Travel\Lisbon trip.md edited, Ideas\Reading list.md deleted"
    }
    "large" {
        # A vault of 5,000 notes in 50 folders, each with frontmatter and links, to time how fast the tree opens.
        $Large = Join-Path $Scratch "Large Vault"
        if (Test-Path $Large) { Remove-Item -Recurse -Force $Large }
        foreach ($f in 1..50) {
            $folder = "{0:D2}" -f $f
            $dir = New-Item -ItemType Directory -Force (Join-Path $Large "Folder $folder")
            foreach ($n in 1..100) {
                $note = "{0:D3}" -f $n
                Write-Note (Join-Path $dir "Note $folder $note.md") "---`ntags: [large, folder-$folder]`naliases: [Note $folder-$note]`n---`n`n# Note $folder $note`n`nSee [[Note $folder 001]].`n"
            }
        }
        Write-Output "Created $Large with 5000 notes"
    }
    "clear-cache" {
        Remove-Item -Force -ErrorAction SilentlyContinue $Database
        Write-Output "Removed Notekea's note cache; the next vault opens with a full scan"
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

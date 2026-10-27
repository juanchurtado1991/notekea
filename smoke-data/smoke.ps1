# Prepares the smoke-test scenarios that cannot live in the repository (Windows).
# Git cannot store a folder without read permission, and a moved vault must not change tracked files, so this script
# builds them in smoke-data\scratch (ignored by Git), next to the sample vaults. It never touches smoke-data\vaults.
# Usage: powershell -ExecutionPolicy Bypass -File smoke-data\smoke.ps1
#        prepare|move|lock|outside|large|edit-note|edit-note-soon|delete-note|check-note|clear-cache|first-launch|restore-settings|clean
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("prepare", "move", "lock", "outside", "large", "edit-note", "edit-note-soon", "delete-note", "check-note", "diff-properties",
        "clear-cache",
        "first-launch", "restore-settings", "clean")]
    [string]$Command
)
$ErrorActionPreference = "Stop"

# $env:SMOKE_SCRATCH = "scratch-<name>" builds everything in smoke-data\scratch-<name> instead, so an agent's checks
# never touch the developer's smoke-data\scratch. It changes only that folder: clear-cache, first-launch and
# restore-settings still use the app's real data folder.
$ScratchName = if ($env:SMOKE_SCRATCH) { $env:SMOKE_SCRATCH } else { "scratch" }
if ($ScratchName -notmatch '^scratch(-[A-Za-z0-9]+)?$') {
    Write-Error "SMOKE_SCRATCH must be scratch or scratch-<letters and digits>, not a path: $ScratchName"
    exit 2
}

$SmokeRoot = $PSScriptRoot
$Scratch = Join-Path $SmokeRoot $ScratchName
$Locked = Join-Path $Scratch "Locked Vault"
$Settings = Join-Path (Join-Path $env:APPDATA "Notekea") "settings.properties"
$Backup = "$Settings.smoke-backup"
$Database = Join-Path (Join-Path $env:APPDATA "Notekea") "notekea.db"
$Playground = Join-Path $Scratch "Playground Vault"
$Utf8 = New-Object System.Text.UTF8Encoding($false)
$Editor = Join-Path $Playground "Editor"
$SharedNote = Join-Path $Editor "Shared note.md"
$WindowsNote = Join-Path $Editor "Windows line endings.md"
$WindowsHeader = "---`r`ntitle: Written on Windows`r`ntags: [crlf]`r`n---`r`n"

function Write-Note($Path, $Text) {
    [System.IO.File]::WriteAllText($Path, $Text, $Utf8)
}

# Notes for the editor cases (1.5): every Markdown element Read mode shows, a note other apps change, and a note with
# Windows line breaks and frontmatter that must be written back unchanged. The accent of "Cafe" is added as a character
# code because Windows PowerShell reads this file without a BOM as ANSI.
function Write-EditorNotes {
    New-Item -ItemType Directory -Force $Editor | Out-Null
    $readMode = @'
---
tags: [editor, smoke]
---
# Read mode

## Links

Cook [[Arepas]], see [[CAFE_ACCENT con leche|the coffee]], open [[A note that does not exist yet]].
A Markdown link to [Pasta e fagioli](../Recipes/Pasta%20e%20fagioli.md) and to [the web](https://example.com).

## Text

Some **bold**, some *italic*, some `inline code` and a #smoke-tag.

> A quote with [[Welcome]] inside.

- [x] A finished task
- [ ] A task to do

## Code

```kotlin
fun servings(people: Int): Int {
    val perPerson = 2 // arepas per person
    return people * perPerson
}
```

```python
def greet(name):
    return f"Hello, {name}"  # [[not a link inside code]]
```
'@
    $readMode = ($readMode -replace "`r`n", "`n").Replace("CAFE_ACCENT", "Caf" + [char]0x00E9)
    Write-Note (Join-Path $Editor "Read mode.md") ($readMode + "`n")
    Write-Note $SharedNote "# Shared note`n`nAnother app changes or deletes this note while it is open in Notekea.`n"
    Write-Note $WindowsNote ($WindowsHeader + "# Windows line endings`r`n`r`nEvery line ends with CR LF.`r`n")
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
        # Dates that differ from the name order, so sorting Recipes by date modified visibly changes it.
        (Get-Item (Join-Path $Playground "Recipes\Pasta e fagioli.md")).LastWriteTime = [datetime]"2026-10-20 09:00"
        (Get-Item (Join-Path $Playground "Recipes\Arepas.md")).LastWriteTime = [datetime]"2026-10-10 09:00"
        # A wildcard instead of the accent: Windows PowerShell reads this file without a BOM as ANSI.
        (Get-Item (Join-Path $Playground "Recipes\Caf* con leche.md")).LastWriteTime = [datetime]"2026-10-01 09:00"
        Set-Content (Join-Path $Scratch "New Vault Location\My Notes\Existing note.md") "# Existing note"
        Set-Content (Join-Path $Locked "Locked note.md") "# Locked note"
        Write-EditorNotes
        # The tasks and callouts note (1.7) and the formatting practice note (1.17) are files of the repository, so
        # tests read the same text.
        Copy-Item (Join-Path $SmokeRoot "notes\Tasks and callouts.md") $Editor
        Copy-Item (Join-Path $SmokeRoot "notes\Formatting practice.md") $Editor
        # Images and attachments (1.8): the note, the note it embeds, its files in the vault's attachments folder, and files outside the
        # vault to pick, drop or locate.
        Copy-Item (Join-Path $SmokeRoot "notes\Images and files.md") $Editor
        Copy-Item (Join-Path $SmokeRoot "notes\Shopping basics.md") $Editor
        # Outline (1.9): a long note with headings of every level, and lines that only look like headings.
        Copy-Item (Join-Path $SmokeRoot "notes\Outline practice.md") $Editor
        # Properties (1.14): one property of each type, and a note whose frontmatter is not valid YAML.
        Copy-Item (Join-Path $SmokeRoot "notes\Properties practice.md") $Editor
        Copy-Item (Join-Path $SmokeRoot "notes\Broken properties.md") $Editor
        # Daily notes (1.10): a vault with Obsidian's Daily notes settings, a template and two earlier days.
        Copy-Item -Recurse (Join-Path $SmokeRoot "daily") (Join-Path $Scratch "Daily Vault")
        # Templates (1.12): a vault whose Obsidian settings put templates in Snippets, with notes to insert them in.
        Copy-Item -Recurse (Join-Path $SmokeRoot "templates") (Join-Path $Scratch "Templates Vault")
        Copy-Item (Join-Path $SmokeRoot "attachments\*") (Join-Path $Playground "attachments")
        $Outside = Join-Path $Scratch "Outside files"
        New-Item -ItemType Directory -Force $Outside | Out-Null
        Copy-Item (Join-Path $SmokeRoot "outside\*") $Outside
        Write-Output "Smoke scenarios ready in $Scratch (Movable Vault, Locked Vault, New Vault Location, Playground Vault with its Editor folder, Outside files, Daily Vault, Templates Vault)"
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
    "edit-note" {
        [System.IO.File]::AppendAllText($SharedNote, "`nEdited outside Notekea at $(Get-Date -Format HH:mm:ss).`n", $Utf8)
        Write-Output "Added a line to Editor\Shared note.md, as another app would"
    }
    "edit-note-soon" {
        # Leaving Notekea for PowerShell makes the editor save, so the change must land while you type in Notekea.
        Write-Output "In 3 seconds Editor\Shared note.md changes: go back to Notekea and keep typing in it"
        Start-Sleep -Seconds 3
        [System.IO.File]::AppendAllText($SharedNote, "`nEdited outside Notekea at $(Get-Date -Format HH:mm:ss).`n", $Utf8)
        Write-Output "Changed Editor\Shared note.md"
    }
    "delete-note" {
        Remove-Item -Force -ErrorAction SilentlyContinue $SharedNote
        Write-Output "Deleted Editor\Shared note.md, as another app would"
    }
    "check-note" {
        # Every line must still end with CR LF and the frontmatter must be exactly as prepare wrote it.
        $text = [System.IO.File]::ReadAllText($WindowsNote, $Utf8)
        $lines = ([regex]::Matches($text, "`n")).Count
        $crlf = ([regex]::Matches($text, "`r`n")).Count
        if ($lines -eq $crlf -and $text.StartsWith($WindowsHeader)) {
            Write-Output "OK: every line ends with CR LF and the frontmatter is unchanged"
        } else {
            Write-Output "CHANGED: $crlf of $lines lines end with CR LF; the note starts with:"
            Write-Output ($text.Substring(0, [Math]::Min(80, $text.Length)).Replace("`r", "\r").Replace("`n", "\n"))
        }
    }
    "diff-properties" {
        # Only the lines of the edited properties should differ from the file prepare copied.
        $original = Get-Content -Encoding UTF8 (Join-Path $SmokeRoot "notes\Properties practice.md")
        $note = Get-Content -Encoding UTF8 (Join-Path $Scratch "Playground Vault\Editor\Properties practice.md")
        $changes = Compare-Object $original $note
        if ($changes) { $changes | Format-Table -AutoSize } else { Write-Output "No change: the note is as prepare wrote it" }
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

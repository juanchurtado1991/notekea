#!/usr/bin/env bash
# Prepares the smoke-test scenarios that cannot live in the repository (macOS and Linux).
# Git cannot store a folder without read permission, and a moved vault must not change tracked files, so this script
# builds them in smoke-data/scratch (ignored by Git). It lives in the repository, not in /tmp, because sandboxed
# terminals and the native folder picker may each see a different /tmp. It never touches smoke-data/vaults.
# Usage: smoke-data/smoke.sh prepare | move | lock | outside | large | edit-note | edit-note-soon | delete-note |
#        check-note | clear-cache | first-launch | restore-settings | clean
set -euo pipefail

SMOKE_ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRATCH="$SMOKE_ROOT/scratch"
SETTINGS_FILE="settings.properties"
DATABASE_FILE="notekea.db"
LARGE_FOLDERS=50
LARGE_NOTES_PER_FOLDER=100
BACKUP_SUFFIX=".smoke-backup"

app_data_dir() {
    if [ "$(uname -s)" = "Darwin" ]; then
        echo "$HOME/Library/Application Support/Notekea"
    else
        echo "${XDG_DATA_HOME:-$HOME/.local/share}/notekea"
    fi
}

clean() {
    if [ -d "$SCRATCH/Locked Vault" ]; then chmod 755 "$SCRATCH/Locked Vault"; fi
    if [ -d "$SCRATCH" ]; then
        # Only ever deletes smoke-data/scratch, next to this script.
        find "$SCRATCH" -depth -delete
    fi
}

prepare() {
    clean
    mkdir -p "$SCRATCH/New Vault Location/My Notes" "$SCRATCH/Locked Vault"
    cp -R "$SMOKE_ROOT/vaults/Personal Notes" "$SCRATCH/Movable Vault"
    cp -R "$SMOKE_ROOT/vaults/Personal Notes" "$SCRATCH/Playground Vault"
    # Dates that differ from the name order, so sorting Recipes by date modified visibly changes it.
    touch -t 202610200900 "$SCRATCH/Playground Vault/Recipes/Pasta e fagioli.md"
    touch -t 202610100900 "$SCRATCH/Playground Vault/Recipes/Arepas.md"
    touch -t 202610010900 "$SCRATCH/Playground Vault/Recipes/Café con leche.md"
    echo "# Existing note" > "$SCRATCH/New Vault Location/My Notes/Existing note.md"
    echo "# Locked note" > "$SCRATCH/Locked Vault/Locked note.md"
    editor_notes "$SCRATCH/Playground Vault/Editor"
    echo "Smoke scenarios ready in $SCRATCH:"
    echo "  Movable Vault       open it, quit, run 'move', relaunch: folder not found, Locate…"
    echo "  Locked Vault        open it, then run 'lock' and open it from Recent: permission error"
    echo "  New Vault Location  already has a 'My Notes' folder with a note: create-vault error"
    echo "  Playground Vault    a copy of Personal Notes to create, rename, delete and update links in;"
    echo "                      its Editor folder has notes for the editor cases (1.5)"
}

# Notes for the editor cases (1.5): every Markdown element Read mode shows, a note other apps change, and a note with
# Windows line breaks and frontmatter that must be written back unchanged.
editor_notes() {
    local folder="$1"
    mkdir -p "$folder"
    cat > "$folder/Read mode.md" <<'NOTE'
---
tags: [editor, smoke]
---
# Read mode

## Links

Cook [[Arepas]], see [[Café con leche|the coffee]], open [[A note that does not exist yet]].
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
NOTE
    printf -- '# Shared note

Another app changes or deletes this note while it is open in Notekea.
' > "$folder/Shared note.md"
    printf -- '---
title: Written on Windows
tags: [crlf]
---
# Windows line endings

Every line ends with CR LF.
' \
        > "$folder/Windows line endings.md"
}

# Checks that the Windows note still has CR LF on every line and its frontmatter exactly as prepare wrote it.
check_note() {
    local note="$SCRATCH/Playground Vault/Editor/Windows line endings.md"
    local lines crlf header
    # grep -c exits with 1 when it counts nothing, which set -e would treat as a failure.
    lines=$(grep -c '' "$note" || true)
    crlf=$(grep -c $'\r$' "$note" || true)
    header=$(head -n 4 "$note")
    if [ "$lines" -eq "$crlf" ] && [ "$header" = $'---\r\ntitle: Written on Windows\r\ntags: [crlf]\r\n---\r' ]; then
        echo "OK: every line ends with CR LF and the frontmatter is unchanged"
    else
        echo "CHANGED: $crlf of $lines lines end with CR LF; frontmatter:"
        echo "$header" | cat -vet
    fi
}

# Changes Playground Vault the way another app or a sync would: one note created, one edited, one deleted.
outside() {
    local vault="$SCRATCH/Playground Vault"
    mkdir -p "$vault/Inbox"
    printf -- '---\ntags: [inbox]\n---\n\n# Created outside\n' > "$vault/Inbox/Created outside.md"
    printf -- '---\ntags: [travel, edited]\ndates: 2026-11-14 to 2026-11-20\n---\n\n# Lisbon trip\n' > "$vault/Travel/Lisbon trip.md"
    rm -f "$vault/Ideas/Reading list.md"
    echo "Changed $vault: Inbox/Created outside.md created, Travel/Lisbon trip.md edited, Ideas/Reading list.md deleted"
}

# A vault of 5,000 notes in 50 folders, each with frontmatter and links, to time how fast the tree opens.
large() {
    local vault="$SCRATCH/Large Vault"
    rm -rf "$vault"
    for folder in $(seq -w 1 "$LARGE_FOLDERS"); do
        mkdir -p "$vault/Folder $folder"
        for note in $(seq -w 1 "$LARGE_NOTES_PER_FOLDER"); do
            printf -- '---\ntags: [large, folder-%s]\naliases: [Note %s-%s]\n---\n\n# Note %s %s\n\nSee [[Note %s 001]].\n' \
                "$folder" "$folder" "$note" "$folder" "$note" "$folder" > "$vault/Folder $folder/Note $folder $note.md"
        done
    done
    echo "Created $vault with $((LARGE_FOLDERS * LARGE_NOTES_PER_FOLDER)) notes"
}

settings_path() {
    echo "$(app_data_dir)/$SETTINGS_FILE"
}

case "${1:-}" in
    prepare)
        prepare
        ;;
    move)
        mv "$SCRATCH/Movable Vault" "$SCRATCH/Moved Vault"
        echo "Moved to $SCRATCH/Moved Vault"
        ;;
    outside)
        outside
        ;;
    large)
        large
        ;;
    clear-cache)
        rm -f "$(app_data_dir)/$DATABASE_FILE"
        echo "Removed Notekea's note cache; the next vault opens with a full scan"
        ;;
    lock)
        # Native folder pickers refuse to select a folder they cannot read, so the folder is locked after it is in
        # Notekea's recent list, like a vault whose permissions change or whose drive becomes unreadable.
        chmod 000 "$SCRATCH/Locked Vault"
        echo "Locked $SCRATCH/Locked Vault"
        if [ "$(id -u)" -eq 0 ]; then echo "Warning: as root, 'Locked Vault' stays readable." >&2; fi
        ;;
    first-launch)
        settings="$(settings_path)"
        if [ -f "$settings" ]; then mv "$settings" "$settings$BACKUP_SUFFIX"; fi
        echo "Notekea will start as on a first launch (backup: $settings$BACKUP_SUFFIX)"
        ;;
    restore-settings)
        settings="$(settings_path)"
        if [ -f "$settings$BACKUP_SUFFIX" ]; then mv "$settings$BACKUP_SUFFIX" "$settings"; fi
        echo "Settings restored"
        ;;
    edit-note)
        printf -- '\nEdited outside Notekea at %s.\n' "$(date +%H:%M:%S)" >> "$SCRATCH/Playground Vault/Editor/Shared note.md"
        echo "Added a line to Editor/Shared note.md, as another app would"
        ;;
    edit-note-soon)
        # Leaving Notekea for the terminal makes the editor save, so the change must land while you type in Notekea.
        echo "In 3 seconds Editor/Shared note.md changes: go back to Notekea and keep typing in it"
        sleep 3
        printf -- '\nEdited outside Notekea at %s.\n' "$(date +%H:%M:%S)" >> "$SCRATCH/Playground Vault/Editor/Shared note.md"
        echo "Changed Editor/Shared note.md"
        ;;
    delete-note)
        rm -f "$SCRATCH/Playground Vault/Editor/Shared note.md"
        echo "Deleted Editor/Shared note.md, as another app would"
        ;;
    check-note)
        check_note
        ;;
    clean)
        clean
        echo "Removed $SCRATCH"
        ;;
    *)
        echo "Usage: $0 prepare | move | lock | outside | large | edit-note | edit-note-soon | delete-note |" \
            "check-note | clear-cache | first-launch | restore-settings | clean" >&2
        exit 1
        ;;
esac

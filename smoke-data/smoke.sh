#!/usr/bin/env bash
# Prepares the smoke-test scenarios that cannot live in the repository (macOS and Linux).
# Git cannot store a folder without read permission, and a moved vault must not change tracked files, so this script
# builds them in smoke-data/scratch (ignored by Git). It lives in the repository, not in /tmp, because sandboxed
# terminals and the native folder picker may each see a different /tmp. It never touches smoke-data/vaults.
# Usage: smoke-data/smoke.sh prepare | move | lock | outside | large | clear-cache | first-launch | restore-settings | clean
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
    echo "Smoke scenarios ready in $SCRATCH:"
    echo "  Movable Vault       open it, quit, run 'move', relaunch: folder not found, Locate…"
    echo "  Locked Vault        open it, then run 'lock' and open it from Recent: permission error"
    echo "  New Vault Location  already has a 'My Notes' folder with a note: create-vault error"
    echo "  Playground Vault    a copy of Personal Notes to create, rename, delete and update links in"
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
    clean)
        clean
        echo "Removed $SCRATCH"
        ;;
    *)
        echo "Usage: $0 prepare | move | lock | outside | large | clear-cache | first-launch | restore-settings | clean" >&2
        exit 1
        ;;
esac

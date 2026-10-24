#!/usr/bin/env bash
# Prepares the smoke-test scenarios that cannot live in the repository (macOS and Linux).
# Git cannot store a folder without read permission, and "moving" a vault must happen outside the repository, so
# this script builds them in a scratch folder. It never touches the vaults in smoke-data/vaults.
# Usage: smoke-data/smoke.sh prepare | move | first-launch | restore-settings | clean
set -euo pipefail

SMOKE_ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRATCH_NAME="notekea-smoke"
SCRATCH_PARENT="${TMPDIR:-/tmp}"
SCRATCH="${SCRATCH_PARENT%/}/$SCRATCH_NAME"
SETTINGS_FILE="settings.properties"
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
        # Only ever deletes a folder named notekea-smoke inside the temporary directory.
        find "$SCRATCH" -depth -delete
    fi
}

prepare() {
    clean
    mkdir -p "$SCRATCH/New Vault Location/My Notes" "$SCRATCH/Locked Vault"
    cp -R "$SMOKE_ROOT/vaults/Personal Notes" "$SCRATCH/Movable Vault"
    echo "# Existing note" > "$SCRATCH/New Vault Location/My Notes/Existing note.md"
    echo "# Locked note" > "$SCRATCH/Locked Vault/Locked note.md"
    chmod 000 "$SCRATCH/Locked Vault"
    echo "Smoke scenarios ready in $SCRATCH:"
    echo "  Movable Vault       open it, quit, run 'move', relaunch: folder not found, Locate…"
    echo "  Locked Vault        no read permission: permission error"
    echo "  New Vault Location  already has a 'My Notes' folder with a note: create-vault error"
    if [ "$(id -u)" -eq 0 ]; then echo "Warning: as root, 'Locked Vault' stays readable." >&2; fi
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
        echo "Usage: $0 prepare | move | first-launch | restore-settings | clean" >&2
        exit 1
        ;;
esac

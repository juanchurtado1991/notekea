#!/usr/bin/env bash
# Starts an installed Notekea and fails unless its window opens and it stays up with no crash report: the check of a
# packaged app (ROADMAP.md 2.13), for what only a package shows, as a Java module left out. The app logs that its
# window opened, so the same check holds on the three systems. It needs a data folder that does not exist yet: it must
# see this start's log, and it never reads or changes a person's notes.
#
# Usage: check-start.sh <path to the installed executable>
set -uo pipefail

readonly opened="Window opened"
readonly open_seconds=120
readonly stay_seconds=20

app="$1"
case "$(uname -s)" in
  Linux) data="${XDG_DATA_HOME:-$HOME/.local/share}/notekea" ;;
  Darwin) data="$HOME/Library/Application Support/Notekea" ;;
  *) data="$(cygpath -u "$APPDATA")/Notekea" ;;
esac
log="$data/logs/notekea.log"
output="$(mktemp)"

if [ -e "$data" ]; then
  echo "The data folder already exists: $data"
  exit 1
fi

fail() {
  echo "FAILED: $1"
  echo "--- output"
  tail -n 40 "$output"
  echo "--- log"
  cat "$log" 2>/dev/null
  echo "--- crash reports"
  find "$data/crashes" -type f -exec cat {} \; 2>/dev/null
  exit 1
}

window_opened() { grep -q "$opened" "$log" 2>/dev/null; }
has_crash_report() { [ -n "$(find "$data/crashes" -type f 2>/dev/null)" ]; }

# Fails on a crash report or the app ending; returns once the given test passes or the seconds run out.
watch_app() {
  local seconds="$1" test="$2"
  for ((second = 0; second < seconds; second++)); do
    has_crash_report && fail "a crash report was written"
    kill -0 "$app_pid" 2>/dev/null || fail "the app ended by itself"
    "$test" && return 0
    sleep 1
  done
  return 1
}

"$app" >"$output" 2>&1 &
app_pid=$!
trap 'kill "$app_pid" 2>/dev/null' EXIT

watch_app "$open_seconds" window_opened || fail "no window after $open_seconds seconds"
watch_app "$stay_seconds" false
echo "Notekea opened its window and stayed up for $stay_seconds seconds."

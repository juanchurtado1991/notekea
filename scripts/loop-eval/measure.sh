#!/bin/bash
# measure.sh <label> <frame|tuned|dev|pool|reference|sealed> <medium|large> [seed]
#
# Measures the loop with a real model and copies the reports to $NOTEKEA_RUNS/<label> (docs/loop-goal-plan.md).
#   frame   the frame alone on the tuned and development tasks (FrameEvalChatModelTest), minutes
#   tuned   the 259 tuned tasks and the fresh vault ("the one loop is measured*"), about 40 minutes
#   dev     the nine exams retired from the blind set ("blind exams*"), about 35 minutes
#   pool    ten unsealed exams of the blind pool, picked by <seed>, about 40 minutes
#   reference  the 40 unsealed exams, once, to compare every round with: about 3 hours the 4B, 4 hours the 9B
#   sealed  the ten sealed exams: once, for the final number, and only with NOTEKEA_FINAL=yes
#
# It measures the commit of this checkout in the worktree $NOTEKEA_MEASURE (default ../notekea-measure), because the
# evaluation reads the vaults from git HEAD: what is not committed is not measured. Never run another Gradle build
# while it runs, and never commit meanwhile (the commit hook runs Gradle).
set -u
label=${1:?label}; what=${2:?frame, tuned, dev, pool, reference or sealed}; size=${3:?medium or large}; seed=${4:-1}
repo=$(git -C "$(dirname "$0")" rev-parse --show-toplevel)
worktree=${NOTEKEA_MEASURE:-$repo/../notekea-measure}
runs=${NOTEKEA_RUNS:-$HOME/.cache/notekea-loop-runs}
models=${NOTEKEA_MODELS:-$HOME/.cache/notekea-spike/models}
out=$runs/$label
tests=app.notekea.chat.eval.AgentEvalChatModelTest

# NOTEKEA_REPLIES=<folder> keeps the model's replies there and reads them back (ReplyCache): a round made again asks
# the model only what changed. Never for the pool, the reference nor the sealed exams, whose numbers count.
replies=()
case $what in frame|tuned|dev) [ -n "${NOTEKEA_REPLIES:-}" ] && replies=("-PchatReplies=$NOTEKEA_REPLIES") ;; esac
# NOTEKEA_TASKS=<file> measures only the tasks the file lists, a line each (fastset.py): a shorter round to tune with.
[ -n "${NOTEKEA_TASKS:-}" ] && case $what in tuned|dev) replies+=("-PchatTaskFile=$NOTEKEA_TASKS") ;; esac

case $what in
  frame) filter="app.notekea.chat.eval.FrameEvalChatModelTest"; extra=(); files="frame-*-$size.md" ;;
  tuned) filter="$tests.the one loop is measured*"; extra=(); files="loop-*-$size.md" ;;
  dev) filter="$tests.blind exams*"; extra=(); files="blind-*-$size.md" ;;
  pool) filter="$tests.blind pool*"; extra=(-PchatPool=sample "-PchatPoolSeed=$seed"); files="pool-*-$size.md" ;;
  reference) filter="$tests.blind pool*"; extra=(-PchatPool=all); files="pool-*-$size.md" ;;
  sealed)
    [ "${NOTEKEA_FINAL:-}" = yes ] || { echo "The sealed exams are for the final number only."; exit 1; }
    filter="$tests.blind pool*"; extra=(-PchatPool=sealed); files="pool-*-$size.md" ;;
  *) echo "frame, tuned, dev, pool, reference or sealed"; exit 1 ;;
esac

mkdir -p "$out"
exec 9>"$runs/.lock"
flock -n 9 || { echo "Another measurement is running."; exit 1; }

commit=$(git -C "$repo" rev-parse HEAD)
git -C "$worktree" checkout --quiet --detach "$commit" || exit 1
echo "$what $size seed=$seed commit $(git -C "$repo" rev-parse --short HEAD)" >> "$out/commit.txt"

cd "$worktree" || exit 1
rm -f app/build/reports/$files
start=$(date +%s)
./gradlew :app:chatModelTest --rerun --console=plain --tests "$filter" \
  "-PchatModels=$models" "-PchatSize=$size" "${extra[@]}" "${replies[@]}" > "$out/gradle-$what-$size.log" 2>&1
echo "$what $size exit=$? minutes=$(( ($(date +%s) - start) / 60 ))" | tee -a "$out/status.txt"
[ -n "${NOTEKEA_REPLIES:-}" ] && [ ${#replies[@]} -gt 0 ] && tail -n 1 "$NOTEKEA_REPLIES/rounds.txt" | tee -a "$out/status.txt"
cp app/build/reports/$files "$out/" 2>/dev/null

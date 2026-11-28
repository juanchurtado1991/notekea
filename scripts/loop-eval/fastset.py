#!/usr/bin/env python3
"""fastset.py <tuned|dev> <small|medium|large> <prefix> [cause]: the tasks worth a round that tunes, a line each.

Reads every round of $NOTEKEA_RUNS whose label starts with <prefix> and holds the whole set, and prints the tasks
that failed in at least one of them: a task right in every round says nothing new. With [cause], the number a
failure is counted under (buckets.py: 1 to 7), only the tasks that ever failed by that cause. The counts go to
stderr. A fast round misses what breaks a task that never failed: confirm on the whole set (docs/gemma-plan.md, 7).
"""
import collections
import os
import sys

import reports

sets, size, prefix = sys.argv[1:4]
cause = sys.argv[4] if len(sys.argv) > 4 else ""
rounds = []
for label in sorted(os.listdir(reports.RUNS)):
    if label.startswith(prefix):
        rows = [row for path in reports.reports_of(label, sets, size) for row in reports.tasks_of(path)]
        if rows:
            rounds.append(rows)
whole = max((len(rows) for rows in rounds), default=0)
rounds = [rows for rows in rounds if len(rows) == whole]
failed = collections.defaultdict(set)
for rows in rounds:
    for row in rows:
        failed[row["task"]] |= set() if row["right"] else {reports.cause_of(row)[0]}
picked = sorted(task for task, causes in failed.items() if causes and (not cause or cause in causes))
print("\n".join(picked))
print("%d rounds of %d tasks, %d picked" % (len(rounds), whole, len(picked)), file=sys.stderr)

#!/usr/bin/env python3
"""compare.py <before label> <after label> <tuned|fresh|dev|pool|all> <medium|large> [--tasks]

The same tasks before and after a change: how many were lost and won, by cause. --tasks also lists them, which is
allowed on the tuned and dev sets only, never on the fresh vault or the pool (docs/eval-blind.md).
"""
import collections
import sys

import reports

before, after, sets, size = sys.argv[1:5]
listed = "--tasks" in sys.argv
if listed and sets not in ("tuned", "dev"):
    sys.exit("--tasks is for the tuned and dev sets only: the fresh vault and the pool are read by cause.")


def by_task(label):
    return {
        (row["report"], row["task"]): row
        for path in reports.reports_of(label, sets, size)
        for row in reports.tasks_of(path)
    }


old, new = by_task(before), by_task(after)
shared = [key for key in new if key in old]
lost = [key for key in shared if old[key]["right"] and not new[key]["right"]]
won = [key for key in shared if not old[key]["right"] and new[key]["right"]]
was = sum(old[key]["right"] for key in shared)
now = sum(new[key]["right"] for key in shared)
print("%d tasks in both runs: %d right before, %d right after (%+d)" % (len(shared), was, now, now - was))
for name, keys, run in (("Lost", lost, new), ("Won", won, old)):
    print("\n%s: %d" % (name, len(keys)))
    for cause, count in sorted(collections.Counter(reports.cause_of(run[key]) for key in keys).items()):
        print("  %3d  %s" % (count, cause))
    if listed:
        for key in keys:
            print("    - [%s] %s" % (key[0], key[1][:110]))

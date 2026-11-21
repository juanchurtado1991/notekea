#!/usr/bin/env python3
"""buckets.py <label> <tuned|fresh|dev|pool|all> <medium|large>: right answers by set and failures by cause.

Prints counts only, never a task: this is how the fresh vault and the pool are read (docs/eval-blind.md).
"""
import collections
import sys

import reports

label, sets, size = sys.argv[1:4]
right = total = 0
causes = collections.Counter()
kinds = collections.Counter()
for path in reports.reports_of(label, sets, size):
    rows = reports.tasks_of(path)
    done = sum(row["right"] for row in rows)
    right, total = right + done, total + len(rows)
    print("%-44s %3d of %3d (%2d %%)" % (rows[0]["report"] if rows else path, done, len(rows), 100 * done // max(len(rows), 1)))
    for row in rows:
        if not row["right"]:
            causes[reports.cause_of(row)] += 1
            kinds[row["kind"]] += 1
if total:
    print("\nTOTAL %d of %d right (%.1f %%), %d failures" % (right, total, 100.0 * right / total, total - right))
    print("\nFailures by cause:")
    for cause, count in sorted(causes.items()):
        print("  %3d  %s" % (count, cause))
    print("\nFailures by the report's kind:")
    for kind, count in kinds.most_common():
        print("  %3d  %s" % (count, kind))

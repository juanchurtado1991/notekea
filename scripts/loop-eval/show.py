#!/usr/bin/env python3
"""show.py <report.md> <start of a task> [lines]: every call of one task. For the tuned and dev sets only."""
import sys

import reports

limit = int(sys.argv[3]) if len(sys.argv) > 3 else 60
for row in reports.tasks_of(sys.argv[1]):
    if row["task"].lower().startswith(sys.argv[2].lower()):
        print(("✓ " if row["right"] else "✗ ") + row["task"])
        print("why: %s | kind: %s" % (row["why"], row["kind"]))
        calls = row["text"].split("Every call</summary>")[-1].strip().split("\n")
        print("\n".join(line[:420] for line in calls[:limit]))
        print("-----")

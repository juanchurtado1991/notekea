#!/usr/bin/env python3
"""holds.py <label> <tuned|fresh|dev|pool|all> <medium|large>: what the loop held back, and how those tasks ended.

For each message the loop gives when it holds a call back ("(Not done: …)", "(Not asked: …)"), how many failed
tasks got it and how many of those ran out of steps. A report keeps the calls of failed tasks only, so this shows
where a hold did not help, not where it did. Counts only, never a task.
"""
import collections
import re
import sys

import reports



def plain(message):
    """A hold's message without what it names: no note, no figure, no quoted word, so no task can be read from it."""
    kind, said = message.split(": ", 1)
    said = re.sub(r"[\"“”'‘’].*", "", said)
    words = [word for word in said.split() if word[0].islower() and "/" not in word and not re.search(r"\d", word)]
    return "%s: %s" % (kind, " ".join(words[:9]))


label, sets, size = sys.argv[1:4]
got = collections.Counter()
ran_out = collections.Counter()
tasks = failed = 0
for path in reports.reports_of(label, sets, size):
    for row in reports.tasks_of(path):
        tasks += 1
        failed += not row["right"]
        held = {plain(message) for message in re.findall(r"\((Not [a-z ]+: [^.)]{0,160})", row["text"])}
        for message in held:
            got[message] += 1
            ran_out[message] += "ran out of steps" in row["kind"]
print("%d tasks, %d failed. Holds seen in the failed tasks:\n" % (tasks, failed))
print("%5s %8s  %s" % ("tasks", "ran out", "hold"))
for message, count in got.most_common():
    print("%5d %8d  %s" % (count, ran_out[message], message))

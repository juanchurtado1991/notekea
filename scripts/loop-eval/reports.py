"""Reads the loop's evaluation reports (build/reports/{loop,blind,pool}-<set>-<size>.md) for the scripts beside it."""
import glob
import os
import re

RUNS = os.path.expanduser(os.environ.get("NOTEKEA_RUNS", "~/.cache/notekea-loop-runs"))

# The causes a failure is counted under (docs/loop-goal-plan.md, "Where the failures are").
ASK_MISSED = "1 should have asked, did not"
ASK_UNNEEDED = "2 asked, should not have"
DAYS = "3 days between dates"
FIGURE = "4 figure worked out (sum, count, average, compare)"
CHANGE = "5 change wrong or incomplete"
LOOKUP = "6 fact wrong or incomplete"
OTHER = "7 other (routed to talk, stopped)"

_DAYS = re.compile(r"\bdays?\b|\bd[ií]as?\b")
_FIGURE = re.compile(
    r"how much|how many|total|average|promedio|cu[aá]nt|suma|add up|\bsum\b|altogether|\bmost\b|\bm[aá]s\b|menos"
)


def tasks_of(path):
    """Every task of one report: whether it was right, its route, its moves, why it failed and the report's kind."""
    text = open(path, encoding="utf-8").read()
    rows = []
    for part in re.split(r"\n(?=## [✗✓] )", text)[1:]:
        head = part.split("\n", 1)[0]
        body = part.split("<details>")[0]
        route = re.search(r"Routed to (\w+)", part)
        kind = re.search(r"^Kind: (.+)$", part, re.M)
        rows.append(
            {
                "report": os.path.basename(path),
                "right": head.startswith("## ✓"),
                "task": head[5:],
                "route": route.group(1) if route else "",
                "moves": [line[2:] for line in body.split("\n") if line.startswith("- ")],
                "why": " | ".join(re.findall(r"^\*\*(.+?)\*\*$", body, re.M)),
                "kind": kind.group(1) if kind else "",
                "text": part,
            }
        )
    return rows


def cause_of(row):
    """The cause a failed task is counted under. Reads the task's words only to tell a date sum from another."""
    why, task = row["why"], row["task"].lower()
    if "not as ASK" in why:
        return ASK_MISSED
    if "ended as Asked" in why:
        return ASK_UNNEEDED
    if row["route"] in ("organize", "write"):
        return CHANGE
    if row["route"] != "ask":
        return OTHER
    if _DAYS.search(task):
        return DAYS
    if _FIGURE.search(task):
        return FIGURE
    return LOOKUP


def reports_of(label, sets, size):
    """The report files of a run: label is a folder of NOTEKEA_RUNS; sets is tuned, fresh, dev, pool or all."""
    folder = label if os.path.isdir(label) else os.path.join(RUNS, label)
    names = {
        "tuned": ["loop-*-%s.md"],
        "fresh": ["loop-fresh-%s.md"],
        "dev": ["blind-*-%s.md"],
        "pool": ["pool-*-%s.md"],
        "all": ["loop-*-%s.md", "blind-*-%s.md", "pool-*-%s.md"],
    }[sets]
    found = sorted(path for name in names for path in glob.glob(os.path.join(folder, name % size)))
    if sets == "tuned":
        found = [path for path in found if "loop-fresh-" not in path]
    return found

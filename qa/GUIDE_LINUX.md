# Notekea QA guide — Linux

Follow the steps in order. Tested on the developer's machine (record the distribution and desktop, e.g. Pop!_OS 24.04
with COSMIC, because the folder picker and the trash depend on the desktop).

## What you receive

A link to a **release** on GitHub, named like `Notekea m1-rc1`. Under **Assets**, download `notekea_<version>.deb`,
`notekea-self-test_<version>.deb`, `smoke-data.zip` and `REPORT_TEMPLATE.txt`.

## 1. Install the app

```bash
sudo apt install ./notekea_<version>.deb
```

Notekea appears in the applications menu. Remove an earlier test build first with `sudo apt remove notekea` if the
install refuses to downgrade.

## 2. Run the automatic test first

Install `notekea-self-test_<version>.deb` the same way, open **Notekea Self-Test**, click **Run** and do not use the
computer while it runs. ✅ continue; ❌ click **Copy report**, paste it into section 1 of the report and **stop**.

## 3. Prepare the sample notes

```bash
unzip smoke-data.zip -d ~/Documents && bash ~/Documents/smoke-data/smoke.sh prepare
```

## 4. Run the manual cases

Open the run's list (`qa/runs/M<N>.md`); each case ID is described in `MVP_SMOKE_TEST.md`. Shortcuts use **Ctrl**.
One line per case in section 2 of the report: the ID and **PASS**, **FAIL** or **SKIP**.

## 5. When a case fails

- **Screenshot**: **Print Screen** (the desktop's screenshot tool); attach the file.
- **Diagnostics**: **Help → Copy diagnostics** in Notekea, pasted under the case.
- If Notekea closed or froze: attach the newest file of `~/.local/share/notekea/logs` (or
  `$XDG_DATA_HOME/notekea/logs`). Launching `notekea` from a terminal also shows the log live.

## 6. Send the report

Save it (e.g. `report-linux-m1-rc1.txt`) and add it to the run record.

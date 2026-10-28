# Notekea QA guide — Windows

Thank you for testing Notekea. This guide takes about two hours the first time. You do not need to know anything
about programming: follow the steps in order and write down what you see. If something here is unclear, that is a
problem with the guide, not with you: say so in your report.

## What you receive

A link to a **release** on GitHub, named like `Notekea m1-rc1` (the "build"). On that page, under **Assets**,
download:

- `Notekea-<version>.msi` — the app.
- `Notekea-Self-Test-<version>.msi` — the automatic test.
- `smoke-data.zip` — the sample notes used by the tests.
- `REPORT_TEMPLATE.txt` — the report you will fill in (also in this folder of the repository).

## 1. Install the app

1. Double-click `Notekea-<version>.msi`.
2. Windows may show **"Windows protected your PC"**. This appears because Notekea's builds are not signed. Click
   **More info**, then **Run anyway**.
3. Follow the installer and finish. Notekea is now in the Start menu.

## 2. Run the automatic test first

1. Install `Notekea-Self-Test-<version>.msi` the same way (same "More info → Run anyway" if it appears).
2. Open **Notekea Self-Test** from the Start menu and click **Run**. Do not use the computer while it runs (a few
   minutes): it clicks and types by itself.
3. When it finishes it shows:
   - ✅ **All tests passed** — continue with step 3.
   - ❌ **Some tests failed** — click **Copy report**, paste it into section 1 of `REPORT_TEMPLATE.txt` (open it with
     Notepad and press Ctrl+V), save it and send it. **Stop here**: the manual tests wait for a new build.

## 3. Prepare the sample notes

1. Right-click `smoke-data.zip` → **Extract All…** → choose your **Documents** folder → **Extract**. You now have
   `Documents\smoke-data`.
2. Open the Start menu, type **PowerShell** and open **Windows PowerShell**.
3. Copy this line, paste it into the window with a right-click, and press Enter:

   ```powershell
   powershell -ExecutionPolicy Bypass -File "$HOME\Documents\smoke-data\smoke.ps1" prepare
   ```

4. It answers with "Smoke scenarios ready in …". Leave that window open: some cases ask you to paste another line
   there (they say exactly which).

## 4. Run the manual cases

1. Open the run's list of cases: the coordinator sends you its link (`qa/runs/M<N>.md` in the repository). For the M1
   run, every case is written out step by step in [M1_SMOKE_TEST.md](M1_SMOKE_TEST.md), which opens on GitHub in your
   browser; later runs describe their cases in `MVP_SMOKE_TEST.md`.
2. For each case: do its steps in Notekea and check what it says under **Pass when**. On Windows, the shortcuts use
   **Ctrl**.
3. Write one line per case in section 2 of the report: the case ID and **PASS**, **FAIL** or **SKIP** (when the case
   says it does not apply to Windows).

## 5. When a case fails

Under the case in the report, write what you did, what you expected and what happened, then:

- **Screenshot**: press **Windows key + Print Screen**. The screen flashes and the picture is saved in
  `Pictures\Screenshots`. Attach it to your message and write its name in the report.
- **Diagnostics**: in Notekea, open the **Help** menu and click **Copy diagnostics**, then paste (Ctrl+V) under the
  case. This copies the app version, your Windows version and the app's recent log; it never contains your notes.
- If Notekea closed or froze and you cannot open the Help menu: press **Windows key + R**, paste
  `%APPDATA%\Notekea\logs` and press Enter. Attach the newest file of that folder to your message.

## 6. Send the report

Save `REPORT_TEMPLATE.txt` (you can rename it, e.g. `report-windows-m1-rc1.txt`) and send it with your screenshots
and attached files to the coordinator. You will receive a new build if anything has to be fixed; then you only repeat
the Self-Test and the cases you are told.

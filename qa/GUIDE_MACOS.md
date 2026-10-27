# Notekea QA guide — macOS

Follow the steps in order; about two hours the first time. If something here is unclear, say so in your report.

## What you receive

A link to a **release** on GitHub, named like `Notekea m1-rc1`. Under **Assets**, download `Notekea-<version>.dmg`,
`Notekea-Self-Test-<version>.dmg`, `smoke-data.zip` and `REPORT_TEMPLATE.txt`.

## 1. Install the app

1. Open `Notekea-<version>.dmg` and drag **Notekea** to **Applications**.
2. The first time, macOS may say Notekea **cannot be opened** because the developer cannot be verified (Notekea's
   builds are not signed). In **Applications**, **right-click** Notekea → **Open** → **Open**. On macOS 15 or later, if
   there is no Open button: **System Settings → Privacy & Security**, scroll down to the message about Notekea and
   click **Open Anyway**.

## 2. Run the automatic test first

1. Install `Notekea-Self-Test-<version>.dmg` the same way and open **Notekea Self-Test**; click **Run** and do not use
   the Mac while it runs.
2. ✅ **All tests passed** — continue with step 3. ❌ **Some tests failed** — click **Copy report**, paste it
   (Cmd+V) into section 1 of `REPORT_TEMPLATE.txt` (open it with TextEdit), save it and send it. **Stop here.**

## 3. Prepare the sample notes

1. Double-click `smoke-data.zip`; move the `smoke-data` folder into your **Documents** folder.
2. Open **Terminal** (Cmd+Space, type Terminal, Enter), paste this line and press Enter:

   ```bash
   bash ~/Documents/smoke-data/smoke.sh prepare
   ```

3. It answers with "Smoke scenarios ready in …". Keep Terminal open: some cases ask you to paste another line there.

## 4. Run the manual cases

Open the run's list (`qa/runs/M<N>.md`, link from the coordinator); each case ID is described in
`MVP_SMOKE_TEST.md`. Do the **Steps** and compare with **Expected**. On macOS, shortcuts use **Cmd**. Write one line
per case in section 2 of the report: the ID and **PASS**, **FAIL** or **SKIP**.

## 5. When a case fails

- **Screenshot**: **Cmd+Shift+3** saves the whole screen to the Desktop (**Cmd+Shift+4** lets you select an area).
- **Diagnostics**: in Notekea, **Help → Copy diagnostics**, then paste under the case. It never contains your notes.
- If Notekea closed or froze: in Finder press **Cmd+Shift+G**, paste `~/Library/Application Support/Notekea/logs`
  and press Enter; attach the newest file.

## 6. Send the report

Save the report (e.g. `report-macos-m1-rc1.txt`) and send it with the screenshots and files.

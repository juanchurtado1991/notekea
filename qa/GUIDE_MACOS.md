# Notekea QA guide — macOS

Thank you for testing Notekea. You do not need to know anything about programming. You install one app,
**Notekea QA**: Notekea itself, with a small window at the bottom left, the **manager**, which tests it and tells you
what to do. It keeps its own notes and settings, so it never touches a Notekea you use every day. The automatic part
runs by itself in about 20 minutes; the manual cases take one to two hours, and you can stop and go on later. If
something here is unclear, that is a problem with the guide, not with you: say so when you send the report.

## 1. Download

You receive a link to a **release** on GitHub, named like `Notekea m1-rc1`. On that page, under **Assets**, download
`Notekea QA-<version>.dmg`.

## 2. Install

Double-click `Notekea QA-<version>.dmg` and drag **Notekea QA** onto **Applications** in the window that opens. The
first time you open it, macOS may say it **cannot check it for malicious software**: the builds are not signed yet.
Click **Done**, open **System Settings › Privacy & Security**, scroll down and click **Open Anyway** next to Notekea
QA, then **Open**.

## 3. The automatic part

1. Open **Notekea QA** from **Applications** (or Launchpad). Notekea opens, and the manager opens at the bottom left of the screen. You can move it, and minimize it
   when it is in your way; closing it only minimizes it.
2. In the manager, click **Start the automatic tests**. It says **Running tests** and shows, at every moment, the time,
   how many cases are done and how long is left, how many passed and failed, the case it runs **now** with each step
   as it happens, and the last ones done. They run out of sight, on copies of the test notes: you can keep using the
   computer.
3. When it is done, failed cases are red: there is nothing to do about them now, they go in the report. Click
   **Next: manual cases**.

## 4. The manual cases

The manager shows one case at a time, and its steps as a line: ✓ done, the current one in bold, the rest to come.

1. Do the step **in Notekea**, then click **Done, next step**. **Back** goes to the step before.
2. When a step needs the case's sample notes, **Open the test data** shows the **Test data** folder: in Notekea, open
   the vault the step names with **Open folder…**.
3. A few cases ask you to **Restart Notekea for this case**: click it, and Notekea and the manager come back on that
   case.
4. After the last step, the manager shows what you should have seen. Click the green answer if you saw it, or the red
   one if not: write **What did you see?**, click **Add a screenshot** if you took one, and **Save and go to the next
   case**. Your last steps in Notekea are added to the report for you. **Skip this case** leaves it for later.

**Screenshots:** press **Cmd + Shift + 4**, then the **Space bar**, and click the window. The picture is saved on the
Desktop; choose it with **Add a screenshot**. On macOS the shortcuts the cases name use **Cmd**.

Your answers are saved: you can close Notekea QA and open it again later, it goes on where you left. The last case is
15 minutes of free use: use Notekea as you really would, and say whether you found anything odd.

## 5. Send the report

1. In the manager, click **Report**, then **Copy report**, and paste it into a message (email, WhatsApp…) to whoever
   asked you to test.
2. Click **Open the Report folder** and attach the pictures in it, and the file `output.txt`, to the same message.

## 6. Finish

When you have sent the report, you can drag Notekea QA from **Applications** to the Trash. Its notes and settings were its own: your everyday Notekea, if you have one, is as it was. When a new build
comes (`m1-rc2`), install it over the old one and repeat this guide: the manager only asks for what is left.

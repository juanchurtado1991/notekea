# Notekea QA guide — Linux

Thank you for testing Notekea. You do not need to know anything about programming: two apps do the work,
**Notekea** (the app being tested) and **Notekea Self-Test** (which tests it and tells you what to do). The automatic
part runs by itself in about 20 minutes; the manual cases take one to two hours, and you can stop and go on later.
If something here is unclear, that is a problem with the guide, not with you: say so when you send the report.

## 1. Download

You receive a link to a **release** on GitHub, named like `Notekea m1-rc1`. On that page, under **Assets**, download
the two files for Linux:

- `notekea_<version>_amd64.deb` — the app.
- `notekea-self-test_<version>_amd64.deb` — the test.

These are for Debian, Ubuntu, Pop!_OS and other systems that install `.deb` files.

## 2. Install both apps

1. Double-click `notekea_<version>_amd64.deb`: your software center (Ubuntu's App Center, Pop!_Shop, GNOME
   Software) opens it; click **Install** and type your password. If it does not open, right-click the file, **Open
   With**, and choose the software center.
2. Do the same with `notekea-self-test_<version>_amd64.deb`.

Both are now in the applications menu.

## 3. The automatic part

1. Open **Notekea Self-Test** from the applications menu.
2. Click **Start the automatic tests**. You can keep using the computer while it runs; it uses its own copies of
   the test notes and never touches yours.
3. When it says **Done**, look at the result. Failed cases are red: there is nothing to do about them now, they go in
   the report. Close Notekea if it is open, and click **Next: manual cases**.

## 4. The manual cases

The Self-Test shows one case per screen:

- **Do this** says what to do, step by step. **Open Notekea** starts Notekea for you, and **Open the folder** shows
  the **Test data** folder, where each case's sample notes are ready (Notekea asks for a folder with **Open folder…**:
  choose the one the case names there).
- **You should see** says what must happen.
- Click **Passed** when it did, or **Failed** when it did not: write **What did you see?**, click **Add a
  screenshot…** if you took one, and **Save as failed**. **Skip for now** leaves a case for later.

**Screenshots:** press **Print Screen** and save the picture (most desktops save it in **Pictures › Screenshots**);
choose it with **Add a screenshot…**. Write your distribution and desktop (for example Pop!_OS 24.04 with COSMIC) in
the report's message: the file dialogs and the trash depend on it.

Your answers are saved: you can close the Self-Test and open it again later, it goes on where you left. The last
case is 15 minutes of free use: use Notekea as you really would, and say whether you found anything odd.

## 5. Send the report

1. After the last case, click **Copy report** and paste it into a message (email, WhatsApp…) to whoever asked you to
   test.
2. Click **Open the Report folder** and attach the pictures in it to the same message.

## 6. Finish

1. Close Notekea, then click **Finish and clean up**: it puts Notekea back exactly as it was before the test and
   deletes the test notes. It says **All clean** when it is done.
2. Uninstall **Notekea Self-Test** from your software center (search for it, **Uninstall**).

You keep Notekea installed for everyday use. When a new build comes (`m1-rc2`), install both apps again over the old
ones and repeat this guide: the Self-Test only asks for the cases it needs.

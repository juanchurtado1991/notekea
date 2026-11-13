# M4b — Sources, study cards and agent

Archived from `ROADMAP.md` once done; its numbers are the ones every document cites.

Added to the MVP with the developer on 2026-11-04. It comes after M4 so Git sync is done, and before M5 so the
performance work of 5.4 and the release run cover all of it. Repositories stays after the MVP.

### ✅ 4.4 Web clips — `feature/m4-sync-sources` — [notekea#140](https://github.com/juanchurtado1991/notekea/pull/140)

Notes from a web page: give Notekea a URL and it writes notes about the article into a folder.

- **Flow:** the URL is downloaded (`GUIDELINES.md` §13), the article is taken out of the page (a library to approve in
  `LIBRARIES.md`), the local model writes the notes with 3.5, each one checked against the article, and Notekea creates
  `Web sources/<title>.md` with the properties `source` and `clipped`, the notes, and the article's text under
  `## Original text`, so search and the chat keep working if the page disappears.
- **Where:** "Clip a web page" in the start actions shown where no note is open, and in the chat as an action of 3.9
  ("clip https://…"). Not in the editor's More menu: the start actions keep the editor's toolbar as it is.
- **With no model installed,** it saves the article alone and says the notes need a model.
- **What fails, in plain words:** a page that needs JavaScript or a login, a paywall, a page that is not an article. No
  browser is built into the app.
- **Done when:** the articles of an evaluation set of 20 saved pages make notes whose every sentence is backed by the
  article, and a page that cannot be read leaves no note behind.

### ✅ 4.5 Documents — `feature/m4-sync-sources` — [notekea#140](https://github.com/juanchurtado1991/notekea/pull/140)

Documents inside the vault are indexed like notes, whatever their format (decided with the developer, 2026-11-04):
search, Related and the chat find them. Notekea reads them and never edits them, and they do not become notes.

- **Plain text:** `.txt`, `.rst`, `.org`, `.csv`, `.json` and `.html`, with almost no new code.
- **Office and books:** PDF, Word (`.docx`), LibreOffice (`.odt`), EPUB, PowerPoint (`.pptx`) and Excel (`.xlsx`). The item
  starts by measuring what Apache Tika adds to the app's size against one small library per format; the choice goes in
  `LIBRARIES.md`.
- **One reader per format,** so another format is cheap to add later.
- **Citations:** a click opens the file in a read-only tab (decided with the developer, 2026-11-11): a PDF as its real
  pages, drawn with PDFBox, at the cited page with the passage marked; the other formats as their text. "Open in the
  system's app" is always there. Apache Tika reads every format (`LIBRARIES.md` §5b).
- **Left out:** scanned PDFs and images with no text, listed as not readable (OCR comes after the MVP), other binary
  files, and source code, which Repositories indexes properly.
- **Done when:** a sentence of each format can be found and cited, a change to a file indexes it again, and a damaged
  file is only skipped and reported.

### ✅ 4.6 Study cards — `feature/m4-sync-sources` — [notekea#140](https://github.com/juanchurtado1991/notekea/pull/140)

Flashcards made by the local model from a note: each question cites the sentence it comes from and passes the
verification of 3.5; reviewed with spaced repetition in a review tab, and stored in the note in the format of Obsidian's
Spaced Repetition plugin, so they work in both. Chosen with the developer on 2026-10-28, when the answer pipeline is
measured.

### ✅ 4.7 Agent mode — `feature/m4-sync-sources` — [notekea#140](https://github.com/juanchurtado1991/notekea/pull/140)

Work that must decide as it goes (decided with the developer, 2026-11-04): "read these 10 articles, compare them and
reorganize my notes about it". Local models are small and choosing tools over several steps is the hardest thing asked
of them, so it is built to be safe when they get it wrong, and 3.0 and 3.4 decide which model sizes offer it.

- **The loop:** the agent chooses one step at a time, from the read-only actions of 3.9 and the URLs in the request,
  as JSON constrained by a grammar, and sees what each one found. Notekea stops it at 30 steps, at a time limit, or on
  Stop, and it runs in the background with its progress in the chat.
- **Nothing is written while it works:** every change goes into a plan, shown at the end on the card of 3.9, with all
  its notes listed, and nothing happens until the user confirms it. Undoing it undoes the whole plan.
- **Safety:** here the agent does read notes and pages, so their text can steer what it proposes. It cannot fetch a URL
  the user did not write, cannot leave the vault, and every change still passes the user's confirmation.
- **Tools:** plain coroutines like the rest of the pipeline: search, read a note, find notes by their properties, the
  open tasks, and fetch a page whose address the request writes.
- **Evaluation:** 30 tasks over the Sample Vault with the plan or answer each should reach (`AgentEvalChatModelTest`);
  a size offers agent mode at 80 % right.
- **Done when:** the model sizes that offer it reach the threshold set in 3.4 on those tasks, and a failed run ends with
  no change to the vault. A model size that falls short does not offer it; the rest of the chat is unaffected.

### ↪ 4.8 M4b performance pass — moved into 5.4

The performance pass of M4b (see "Closing a milestone"): memory and time of a web clip, of indexing large documents and of a
long agent run. Compared with 4.3 in `docs/performance.md`.

**Moved into 5.4 (the developer's decision, 2026-11-11):** measured there, with all of M5 built, and checked by the
single release run.

### 🚧 4.9 Videos in notes — `feature/videos` (built; in the PR with M5)

Added with the developer on 2026-11-11, decided after a spike (`LIBRARIES.md` §5c, privacy in issue #141):

- **A video in a note** (`![[trip.mp4]]`, and mov, m4v, webm, mkv, ogv) is a card: a dark frame with a play mark,
  its name and format. A click plays it in place with the system's player (ComposeMediaPlayer), with pause, a bar to
  move in it, its time and ✕ back to the card. A format the system cannot play says so and opens with its app.
- **A YouTube video** (`![Title](https://youtu.be/…)`, as Obsidian embeds it) is the same card. A click plays it in a
  Notekea window of its own: the system's web view, in Notekea started again as a separate process.
  - Pasting a YouTube link writes its embed, with the title and thumbnail YouTube gives, asked once.
  - Showing a note never goes on the network.
- **A clipped page's YouTube videos** are written under `## Videos` as cards, with the titles the page gives them.
- **Videos over 100 MB** (GitHub's limit per file) are recompressed to H.264 under 100 MB when added, at the best
  quality that fits. FFmpeg (LGPL, about 27 MB) is downloaded the first time, verified with a checksum.

---

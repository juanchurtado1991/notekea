# Spike — One editing mode: hiding the Markdown marks in the text field

2026-11-09, Linux, the test JVM (`ImageComposeScene`, software drawing), Compose Multiplatform 1.12.2. Step 1 of the
plan of One editing mode (`docs/HANDOFF.md`). The spike's code was a temporary test and is not in the repository.

## Question

Can the editor's one text field hide the Markdown marks of every line but the cursor's, from
`MarkdownOutputTransformation`, and stay fluid on a long note?

## What works

- The transformation runs again when the cursor moves: Compose reads the field's text and selection together, and
  `TextFieldBuffer.selection` gives the cursor's line.
- `TextFieldBuffer.replace` removes the marks from what is shown; Compose maps clicks, the cursor and the selection
  itself, and the cursor never lands inside removed text. The note's text and undo do not change.
- Giving the marks a tiny font instead of removing them is not an option: 3.7 s per line change on the long note.

## Measures

Median of 40, milliseconds per frame. *Ghost* is the longest note of the Ghost Serializer vault
(`ghost-serialization.md`, 330 lines, 35,000 characters); *Long* is the generated 2,000-line note of the memory runs
(93,000 characters, 13,200 highlighted spans).

| | Cursor to another line | Cursor inside its line | One character typed |
|---|---|---|---|
| Ghost, today | 7 | 7 | 75 |
| Ghost, today, style tracking off | 2 | 2 | 70 |
| Ghost, marks removed, style tracking off | 50 | 10 | 51 |
| Long, plain text with no styles | 2 | 2 | 52 |
| Long, today | 272 | 271 | 1,380 |
| Long, today, style tracking off | 5 | 5 | 1,106 |
| Long, today, tracking off, one paragraph per heading | 7 | 6 | 304 |
| Long, today, tracking off, one paragraph per line | 12 | 11 | 742 |
| Long, marks removed, tracking off | 849 | 314 | 848 |

The process's memory in this harness varied too much between runs (0.5–1.4 GB) to compare: the memory runs of
`:auto-test` are the instrument for that.

## Findings

1. **A bug of today's editor.** Since Compose 1.12, `ComposeFoundationFlags.isBasicTextFieldStyledTextEnabled` is
   on, and every `TextFieldBuffer.addStyle` also records a change: adding the styles costs time squared in their
   number. On the long note that is 270 ms for every cursor move and every key, in `addStyle` alone (13,202 calls).
   With the flag off it is 3 ms. The flag is experimental and Compose plans to remove it.
2. **Removing the marks has the same cost**, one `replace` per mark: 310 ms per run on the long note, flag on or off.
   One pass that builds the shown text and replaces the whole buffer once would avoid it, at the price of mapping
   the offsets ourselves.
3. **The text field lays out the whole note on every change**, and styles make it far slower than plain text (1.1 s
   against 52 ms per key on the long note). Showing another line's marks changes the text shown, so moving the
   cursor to another line costs what a key costs: 50 ms on Ghost, 0.85 s on the long note. Splitting the text into
   paragraphs helps typing (304 ms) but stays far from fluid.

## Result

Hiding the marks in the one text field holds for the notes people write (50 ms per line change on the longest real
note) and does not hold for a 2,000-line note, where today's editor already does not hold either. Only laying out
what is on screen fixes that, and it is the step to decide before step 2.

## What the change touches

Everything that reads the text field's layout with offsets of the note's text needs the offsets of the text shown
once marks are removed: the outline's heading positions and Find's scroll (`EditorBody.kt`), the attachments'
overlay and their paragraph styles (`AttachmentOverlay.kt`, `AttachmentRoom.kt`), the task checkboxes and the code
blocks' background (`EditorDecorations.kt`), and Find's match styles inside the transformation. The toolbar's
formats, list editing, links, the status bar's counts and related notes read the note's text and are not touched.

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

## Second spike — visible blocks

The developer chose to lay out only what is on screen by splitting the note into blocks. The second spike, also a
temporary test, answered how to do it with the least code.

**The design that held.** The note is cut into blocks at its blank lines (never inside a code fence), and a
`LazyColumn` shows one item per block. Every block is drawn by Preview's renderer except the block of the cursor,
which is a `BasicTextField` over the note's one `TextFieldState`: its output transformation removes everything
outside that block with two `replace` calls (the window), and the marks outside the cursor's line as the first spike
did. So there is still one text, one selection and one undo history for the whole note, and the toolbar's formats,
list editing and every other reader of the note's text stay as they are. The window follows the selection: it is
the blocks from the selection's start to its end, so a selection across blocks widens the text field, and Select all
turns the whole note into today's editor. The text field's item keeps one key wherever the window is, so the same
field moves down the list and keeps the keyboard.

| | Long (1,202 blocks) | Ghost (54 blocks) |
|---|---|---|
| Blocks composed at first | 11 | 7 |
| One character typed | 6 | 4 |
| Cursor inside its block | 4 | 3 |
| Cursor to the next block | 9 | 6 |
| Scroll of 300 px | 5 | 3 |
| Select all (the whole note in the field) | 877 | 65 |
| Java heap after a collection | 51 MB | 55 MB |

Milliseconds per frame, median of 40, the same harness as above. Typing on the long note goes from 1,380 ms to 6.
The keyboard focus was kept in the 40 moves to another block.

**Why a lazy list failed before.** The Markdown renderer parses off the UI thread by default and is an empty box of
no height until it is done. In a `LazyColumn` every item then fits on screen, so the list composes them all: 1,201
blocks at first on the long note, 1,051 ms for the first frame and 436 ms per scroll. Parsing each block at once
(`rememberMarkdownState(immediate = true)`), which is cheap for one block, gives the figures above. The renderer of
the blocks must always have its height on its first frame.

**What is left to build**, none of it measured here: a click on a drawn block puts the cursor at that place; the
arrow keys, a drag and Shift with a click cross blocks; a block keeps its height when it turns into text and back
as far as the two renderings allow; Find in note, the outline, the scroll position of each tab and the embedded
files read the list instead of the text field's layout.

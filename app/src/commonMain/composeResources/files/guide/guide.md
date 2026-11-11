<!--
The guide the chat answers "how do I…" and "what can you do" from (AnswerHelpUseCase, GUIDELINES.md §15). One topic per
"# " heading; "words:" lists what people say for it, in English and Spanish; "try:" is a request the chat can do for
them (a trailing "…" means they finish it). {name} is a shortcut of PlatformConventions (newNote for
newNoteShortcut), written for the person's system. Every feature that adds or changes something people do updates it.
-->

# What the chat can do
words: what can you do, help, ayuda, qué puedes hacer, que puedes hacer, para qué sirves, capabilities, chat
Ask your notes anything and the chat answers from what they say, citing the notes as numbered chips; click a chip to
open the passage. It also does things for you when you ask in plain words: create a note (also from a template or from
other notes, like a summary), add text or a task to a note, tag notes, set a property, link notes, bookmark, rename,
move or trash notes, create folders, turn a pasted text into a folder of linked notes, list notes by their properties
or your tasks, open a note or today's note, show a panel, and change the color theme. Anything that changes your notes
is shown first on a card with Do it and Cancel, and Undo takes it back. It can also explain how to use Notekea. Open
the chat with {chat}.

# What the chat cannot do
words: what can't you do, what cannot you do, limits, limitations, qué no puedes hacer, que no puedes hacer, qué no podés hacer, que no podes hacer, límites, no puedes, can't you
It works only with the notes of this vault, on this computer, and it never goes online by itself. It cannot send
emails or messages, set reminders or alarms (it can add a task with a due date, which shows in Today and Tasks),
look at the calendar, search the web or know the weather and the news (it reads a web page only when you paste its
address, to clip it), change Settings or the AI model (it can change the color theme), delete notes for good (it moves
them to the trash, and Undo brings them back), or reach files outside the vault. When it cannot do something it says
so, and what it can do instead.

# Create a note
words: create a note, new note, make a note, crear nota, nueva nota, nota nueva, crear una nota, creo una nota, hacer una nota, add a note
try: create a note called …
Press {newNote}, or right-click a folder in the Files sidebar and choose New note: the note is created in that folder
and its title is ready to type. You can also ask the chat, for example "create a note Groceries in Recipes with a
shopping list", and confirm the card.

# Create a note from a template
words: template, plantilla, from a template, desde una plantilla, insert template, insertar plantilla
try: create a note … from the … template
Templates are the notes in the Templates folder (or the one set in Obsidian's templates settings). Right-click a folder
in the sidebar and choose New note from template, or press {template} in a note to insert a template at the cursor.

# Create a folder
words: folder, carpeta, new folder, nueva carpeta, crear carpeta
try: create a folder …
Right-click a folder in the Files sidebar and choose New folder, then type its name. The chat can create one too.

# Rename or delete a note
words: rename, renombrar, cambiar nombre, delete, borrar, eliminar, trash, papelera
Select the note in the sidebar and press {rename} to rename it, or click its title above the text and type. Links to it
are updated so none breaks. Press {delete} to delete it: it goes to the system's trash after you confirm.

# Move a note
words: move, mover, drag, arrastrar, another folder, otra carpeta
try: move … to …
Ask the chat, for example "move Retro October to Archive", and confirm the card: links to it are updated, and Undo
moves it back.

# Find a note
words: find a note, open a note, buscar nota, abrir nota, quick switcher, search names
Press {quickSwitcher} and type part of its name, alias or folder; arrows choose and Enter opens it.

# Search in all notes
words: search, buscar, search notes, buscar en las notas, find text, encontrar texto, content
try: search for …
Press {searchNotes} and type: Notekea finds notes by meaning and by their words, shows the passage that matched, and
opens the note with it selected. A #tag finds the notes with that tag. Press {find} to find text inside the open note.

# Today's note
words: today, hoy, daily note, nota diaria, journal, diario, yesterday, tomorrow, ayer, mañana
try: open today's note
Press {today} or click Today in the sidebar: today's note opens, created if it does not exist yet (in Journal, or the
folder of Obsidian's daily notes settings). {previousDay} and {nextDay} go to the day before and after.

# Format text
words: bold, negrita, italic, cursiva, heading, título, encabezado, list, lista, quote, cita, table, tabla, code, format, formato, markdown
The toolbar above the note formats without knowing Markdown: Heading, Bold ({bold}), Italic ({italic}), Link to a note,
Tag, Checklist, Bulleted list, Insert template, and More for quotes, tables, code, callouts and the rest. The note
looks finished as you write; the line with the cursor shows its Markdown marks. {toggleSource}, or Source at the end
of the toolbar, shows the whole note as written in Markdown.

# Link notes
words: link, enlace, enlazar, wiki link, vincular, connect notes, conectar notas
try: link … to …
Press {noteLink} (or Link to a note in the toolbar) and choose the note: it writes [[Note]]. {webLink} adds a web link.
The Backlinks panel lists the notes that link to the open one, and the Related panel suggests notes to link.

# Tasks
words: task, tarea, checklist, to do, pendiente, checkbox, casilla, tareas
try: show my tasks
Write "- [ ] " at the start of a line (or use Checklist in the toolbar) to make a task; click its box or press
{toggleTask} on its line to check it. The Tasks tab, in the tool rail on the left, shows the tasks of every note, by
note or by date, with filters by folder, tag and done.

# Tags
words: tag, etiqueta, tags, etiquetas, hashtag
try: tag … with …
Write #tag in the text, or add it under the note's Properties. Click a tag to search its notes. The chat can tag
several notes at once ("tag every note in Recipes with food").

# Properties
words: property, propiedad, properties, propiedades, frontmatter, status, estado, rating, metadata
try: set the … of … to …
The Properties card under the title shows the note's properties (status, rating, dates, tags) and lets you add or
edit them. The Query tab in the tool rail lists notes by their properties, and you can describe what you want in
plain words.

# Query notes by their properties
words: query, consulta, table of notes, tabla de notas, filter, filtrar, dataview, list notes, listar notas
try: books I rated 4 or more
Open the Query tab in the tool rail and add rules (is, contains, more than, before a date, has a tag, in a folder), or
type what you want in "Describe what you want". The chat does the same when you ask for notes by their properties.

# Bookmarks
words: bookmark, marcador, favorito, favorite, star, estrella
try: bookmark …
Click the star beside the note's title, choose Bookmark in a note's menu in the sidebar, or press {bookmark}. Bookmarks
show above the tree.

# Tabs and split view
words: tab, pestaña, split, dividir, side by side, lado a lado, two notes, dos notas
Every note opens in a tab. {closeTab} closes it, {nextTab} and {previousTab} move between tabs. {splitRight} opens the
note beside the other one; you can drag a tab between the two sides.

# Panels: outline, backlinks, related notes, chat
words: panel, outline, esquema, backlinks, related, relacionadas, inspector, side panel, panel lateral
try: show the backlinks
The side panel on the right holds the Inspector (outline, backlinks, related notes, properties) and the Chat. {toggleSidePanel}
shows or hides it, {outline} goes to the outline, {chat} opens the chat. {toggleSidebar} shows or hides the Files
sidebar on the left.

# Graph
words: graph, grafo, map, mapa, connections, conexiones
Press {graph} or click Graph in the tool rail: a map of how your notes link, starting from the open note.

# Images and files
words: image, imagen, picture, foto, paste, pegar, attachment, adjunto, file, archivo, pdf
Paste an image ({paste}) or drop a file into a note: it is saved in the attachments folder and linked where the cursor
is. Image or file in the toolbar picks one with the system's picker.

# Videos in notes
words: video, vídeo, youtube, mp4, mov, play, reproducir, película, clip de video, ver un video
Drop or pick a video file (mp4, mov, m4v, webm, mkv, ogv) into a note: it shows as a card with a play mark. Click it
to play it right there, with pause, a bar to move in it and ✕ back to the card. A format this computer can't play
opens with its app instead. Paste a YouTube link alone on a line: it becomes a card, and Notekea asks YouTube once for
its title and picture; click it to play the video in a Notekea window of its own. Showing a note never goes on the
internet. A video over 100 MB, GitHub's limit for one file, is made smaller before it is attached, at the best
quality that fits; the first time, Notekea downloads the video tool FFmpeg (about 27 MB). Cancel stops it, and the
video stays where it was.

# Color theme and appearance
words: theme, tema, dark mode, modo oscuro, light mode, colors, colores, appearance, apariencia
try: use the … theme
Open Settings (the gear at the bottom of the tool rail) and then Appearance. Mode is Light, Dark or System, which
follows your computer's light or dark setting. Theme is the set of colors the whole app takes; each card shows it in
light and dark. Reduce motion makes panels and lists change at once, without sliding; it is also on when your computer
asks for less motion. The chat can change the theme too.

# AI model
words: model, modelo, download, descargar, ai model, modelo de ia, llm, small, medium, large, pequeño, mediano, grande, memory, memoria, gpu
Open Settings, then AI model: Notekea checks this computer's memory and recommends a size. Small is fast and light
but gets more answers wrong, so check its sources; Medium gives good answers on most computers; Large gives the best
answers and needs a GPU or 32 GB of memory. Click Download (it can be paused and goes on where it stopped), then Use.
The first download also brings the model search uses. The model runs on your computer and works without internet;
your notes never leave it. Delete frees its space on the disk.

# First-run welcome
words: welcome, bienvenida, onboarding, getting started, primeros pasos, sample vault, vault de ejemplo, start, empezar
The first time Notekea starts, a welcome in three steps shows over the welcome screen: what Notekea does, choose your
notes (open a folder, create a vault, or try the sample vault, a copy made in Documents), and get the AI, the model
size that fits this computer, downloaded in the background. Skip or Esc closes it and it does not come back; Settings
→ General → Welcome → Show again brings it back the next time Notekea starts. The welcome screen also has Open the
sample vault.

# Settings: General
words: settings, ajustes, configuración, preferences, preferencias, language, idioma, español, english, startup, al iniciar, last vault, último vault, welcome again, bienvenida otra vez
Open Settings (the gear at the bottom of the tool rail; {settings} where the system has it), then General. Language
is English, Español or System, which follows your computer's language; the chat always answers in the language you
write in. Open the last vault at startup: off, Notekea starts on the welcome screen with your vaults listed. Welcome,
Show again: the three first-run steps come back the next time Notekea starts.

# Settings: Editor
words: text size, tamaño de letra, font size, letra más grande, bigger text, line width, ancho de línea, readable, wide, full width, columna
Settings → Editor: Text size makes the note's text bigger or smaller, headings, code and tables with it (zoom changes
the whole window instead). Line width is Readable (a narrow column), Wide (the default) or Full width.

# Settings: Files
words: attachments folder, carpeta de adjuntos, daily notes folder, carpeta de notas diarias, date format, formato de fecha, templates folder, carpeta de plantillas
Settings → Files, with a vault open: where new attachments go, the daily notes' folder and their date format (as
YYYY-MM-DD), and the templates' folder. Blank is the vault's top folder. They are saved in the vault's .obsidian
folder, the same settings Obsidian uses, so both apps agree.

# Settings: Shortcuts and About
words: shortcuts list, lista de atajos, keyboard shortcuts, atajos de teclado, about, acerca de, version, versión, diagnostics, diagnóstico, privacy, privacidad, licenses, licencias
Settings → Shortcuts lists every keyboard shortcut of your system. Settings → About shows Notekea's version, Copy
diagnostics (what helps fix a problem, without any note's text, to paste in a bug report), the privacy promise and
the licenses, with the project on GitHub.

# Ask about Settings
words: settings help, ayuda de ajustes, ask about settings, preguntar sobre ajustes, what does this setting do, qué hace este ajuste
Beside Settings, Ask about Settings answers what an option does or how to do something, about the section open, in
your language and from this guide. When a question or its answer names a setting, Settings opens its section and
the setting blinks; Show under the answer shows it again. With Sync open it shows the steps to set up GitHub. The
steps and the blinking need no model; the answers need the AI model.

# Open another vault
words: vault, bóveda, boveda, open folder, abrir carpeta, another vault, otra bóveda, switch vault
Press {openFolder} to open a folder as a vault, or click the vaults button at the bottom of the tool rail to switch to a
recent one.

# Chat modes: Auto, Fast, Deep thinking, Agent
words: mode, modo, modes, modos, deep thinking, deep answer, respuesta profunda, pensar, more precise, más preciso, slower, drafts, auto, fast, rápido, normal
At the bottom right of the chat's question field it says the model in use and the mode, such as "Medium · Auto"; a
click opens the modes. Auto, the default, picks for each request: Deep thinking for a question that asks to compare,
explain or sum up, the agent for a task whose note it must look for first, and a fast answer for the rest. Fast is
one answer, the quickest. Deep thinking writes three drafts and keeps the one they agree on: slower, more often right.
Agent works step by step. Deep thinking comes with the Medium model, Agent with Medium and Large.

# Copy the chat
words: copy chat, copiar chat, clear history, borrar historial, report, reportar
Click ⋯ at the top right of the chat: Copy chat puts the conversation on the clipboard, and Clear history empties it.

# The chat's memory
words: memory, memoria, context, contexto, context window, ventana de contexto, compact, compactar, forget, olvidar, ring, rueda, círculo
The chat remembers the conversation so you can ask "and the water?" or "make it shorter". The ring beside the mode, at
the bottom right of the question field, fills as the conversation grows; hold the pointer on it to see how full it
is. When it is full, or when you ask again after half an hour or in a chat from an earlier session, the chat writes
the earlier conversation into a short summary by itself and keeps going with it.
Compact chat, in the ⋯ menu, does it at once, and Clear history forgets everything. The chat replies in the language
you write in, or the one you ask for ("in Spanish please").

# Sync with Git
words: sync, sincronizar, sincronización, git, gitlab, backup, copia de seguridad, another computer, otro computador, otra computadora
New to GitHub? See Set up GitHub sync, step by step. Open Settings (the gear at the bottom of the tool rail), then Sync: paste the HTTPS address of a Git repository (for
example on GitHub) and an access token that can write to it, then Connect. Notekea saves your changes to it 10
seconds after you stop typing and brings the changes made on your other computers; the status bar says Up to date,
Syncing… or how many changes are pending, and a click on it opens Settings → Sync. Sync now sends everything at once,
and Stop syncing forgets the repository and the token. The token is kept in your system's keychain.

# Set up GitHub sync, step by step
words: github, qué es github, que es github, how do I connect github, cómo conecto github, como conecto github, no sé usar github, no se usar github, token, access token, personal access token, repository, repositorio, crear repositorio, cuenta de github, github account, set up sync, configurar sincronización
GitHub is a free website that keeps folders of files online. Notekea saves your vault there, in a private place only
you can see, so your notes are the same on every computer and safe if this one breaks. You need it only to sync; a
vault that stays on this computer needs nothing. Five steps, once:
1. Make a free account at github.com (Sign up).
2. Make a private repository, the place your vault goes: the + at the top right of GitHub, New repository. Name it
   (for example notes), choose Private, leave Add a README off, and click Create repository.
3. Copy its address: the page that opens shows it under HTTPS, like https://github.com/your-name/notes.git.
4. Make a token, the key Notekea uses instead of your password: your picture at the top right, Settings, Developer
   settings (at the bottom), Personal access tokens, Fine-grained tokens, Generate new token. Name it Notekea, pick
   an expiration, under Repository access choose Only select repositories and your notes repository, and under
   Repository permissions set Contents to Read and write. Click Generate token and copy it: GitHub shows it once.
5. In Notekea open Settings (the gear at the bottom of the tool rail), then Sync: paste the address and the token,
   and click Connect. On your other computer, open the same Settings → Sync with the same address and a token.
Your token stays in your system's keychain, never in the vault. Beside Settings, Ask about Settings shows these steps
when Sync is open and answers questions on them, as on every setting.

# Sync sign-in failed
words: sync sign-in failed, sign-in failed, error de sincronización, no sincroniza, token expired, token caducado, token vencido, the repository refused the token, rechazó el token
The repository refused the token. Most often the token expired (GitHub tokens last the time you chose), was made for
another repository, or cannot write: Contents must be Read and write. Make a new token as in Set up GitHub sync, step
4, paste it in Settings → Sync and click Connect; your notes stay as they are.

# GitHub's limits for your vault
words: limit, límite, 100 mb, 1 gb, too large, demasiado grande, video, vídeo, big file, archivo grande, github free, cuenta gratuita, repository size, tamaño del repositorio
GitHub refuses any single file over 100 MB, and asks that a repository stay under about 1 GB (5 GB at most). When a
vault over 1 GB is synced with GitHub, or its GitHub address is typed in Settings → Sync, Large vault for GitHub says
how much it takes, since a free account may refuse or slow it. Keep large videos outside the vault, or use another
Git host. A vault that stays on this computer, or syncs elsewhere, is never warned.

# Settle a sync conflict
words: conflict, conflicto, both versions, ambas versiones, keep mine, keep theirs, conflict copy, copia en conflicto
When a note changed on this computer and on another one, a banner over the notes names it. Review shows both versions
side by side: Keep mine uses this computer's, Keep theirs the other one's, and Keep both saves yours as "Note
(mine)" beside it. Nothing is lost while you decide: your version waits as "Note (conflict)".


# Documents: PDF, Word, Excel and more
words: pdf, mis pdf, my pdfs, word, docx, excel, xlsx, powerpoint, pptx, epub, document, documento, documentos, libro, ebook, spreadsheet, hoja de cálculo, buscar en mis pdf, search my pdfs
Put PDFs, Word (.docx), OpenDocument (.odt), EPUB, PowerPoint (.pptx), Excel (.xlsx), HTML and text files anywhere in
the vault: Notekea reads them, never changes them, and finds them in search, Related notes and the chat beside your
notes. Click one in the Files sidebar, or a citation of it, to read it in a tab: a PDF shows its real pages, the
others their text, with the cited passage marked. Open in the system's app opens it with the app your computer uses.
A scanned PDF without text cannot be read yet.

# Clip a web page
words: clip, recortar, web page, página web, article, artículo, save a page, guardar una página, link, url, enlace, documentation, documentación, linked pages, enlaces internos, section, sección
try: clip …
Click Clip a web page where no note is open (the start of the editor), or ask the chat "clip https://…" with the
page's address. Notekea downloads the page, keeps its article in the Web sources folder with the properties
source and clipped, writes notes about it with the AI model and keeps only the sentences the article backs, and puts
the article itself under Original text. When the page has other pages under its own path, such as a documentation's
section, Notekea asks first: only this page, or this page and those, saying in a sentence what they offer. It finds
them in the site's llms.txt (its pages written for machines, clean of menus), else its sitemap, else the page's
links; news and blog pages are never a section. The 30 closest to the page are the default; All N pages clips every
one. With them, each becomes a note in a folder named after the page, and the page's note lists them under Pages with
what each is for. A page the site also serves as Markdown (page.md) is read from it. Say "with its linked pages",
"all its pages" or "only" in the chat to answer before it asks. A page's YouTube videos are kept under Videos, as
cards that play. Pages that need a login or JavaScript cannot be clipped.

# Study cards
words: study cards, flashcards, tarjetas, tarjetas de estudio, repaso, review, memorize, memorizar, spaced repetition
try: make study cards from this note
Ask the chat "make study cards from this note": it proposes cards whose answers the note backs and adds them at the
note's end as Question::Answer lines under #flashcards, the format of Obsidian's Spaced Repetition plugin. Review them
in Study cards, in the tool rail on the left: Show answer (Space), then Hard, Good or Easy (1, 2, 3); each card comes
back when it is due.


# Agent mode
words: agent, agente, modo agente, agent mode, step by step, paso a paso, multi-step, varios pasos, do a task, haz una tarea
try: find my unfinished books and tag them with to-read
With the Medium or Large model, choose Agent in the mode menu at the bottom right of the chat's field (Auto also uses it when
a task names a note it must look for first). It works step by step, listing each step (searched, read a note, listed
notes, looked at the tasks), and ends on the usual card: nothing changes until you press Do it. Help, what the notes
are about and talk are answered as usual. It reads a web page only when your request writes its address, and stops
after 30 steps or 10 minutes.

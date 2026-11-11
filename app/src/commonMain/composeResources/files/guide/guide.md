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

# Color theme and appearance
words: theme, tema, dark mode, modo oscuro, light mode, colors, colores, appearance, apariencia
try: use the … theme
Open Settings (the gear at the bottom of the tool rail) and then Appearance: light or dark mode and a color theme. The
chat can change the theme too.

# AI model
words: model, modelo, download, descargar, ai model, modelo de ia, llm
Open Settings, then AI model: Notekea recommends a size for your computer and downloads it. The model runs on your
computer; your notes never leave it.

# Open another vault
words: vault, bóveda, boveda, open folder, abrir carpeta, another vault, otra bóveda, switch vault
Press {openFolder} to open a folder as a vault, or click the vaults button at the bottom of the tool rail to switch to a
recent one.

# Deep answer
words: deep answer, respuesta profunda, more precise, más preciso, slower, drafts
Turn on Deep answer under the chat's question field when precision matters more than speed: it writes several drafts
and keeps the best supported one.

# Copy the chat
words: copy chat, copiar chat, clear history, borrar historial, report, reportar
Click ⋯ at the top right of the chat: Copy chat puts the conversation on the clipboard, and Clear history empties it.

# Sync with Git
words: sync, sincronizar, sincronización, git, github, gitlab, backup, copia de seguridad, another computer, otro computador, otra computadora, repository, repositorio
Open Settings (the gear at the bottom of the tool rail), then Sync: paste the HTTPS address of a Git repository (for
example on GitHub) and an access token that can write to it, then Connect. Notekea saves your changes to it 10
seconds after you stop typing and brings the changes made on your other computers; the status bar says Up to date,
Syncing… or how many changes are pending, and a click on it opens Settings → Sync. Sync now sends everything at once,
and Stop syncing forgets the repository and the token. The token is kept in your system's keychain.

# Settle a sync conflict
words: conflict, conflicto, both versions, ambas versiones, keep mine, keep theirs, conflict copy, copia en conflicto
When a note changed on this computer and on another one, a banner over the notes names it. Review shows both versions
side by side: Keep mine uses this computer's, Keep theirs the other one's, and Keep both saves yours as "Note
(mine)" beside it. Nothing is lost while you decide: your version waits as "Note (conflict)".


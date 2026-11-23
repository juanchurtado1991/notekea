package app.notekea.core.notes.domain

/** A [NoteTextCache] over [store], as the app builds one: what most tests of a reader of notes start from. */
fun noteTextCache(store: NoteContentStore): NoteTextCache = NoteTextCache(
    contentStore = store,
    textFolder = TextFolder
)

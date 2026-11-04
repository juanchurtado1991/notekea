package app.notekea.notes.domain

/** Remembers what it was asked to carry over and calls [onCopy], as the disk would be changed at that moment. */
class FakeNoteFileAttributes : NoteFileAttributes {
    val copies = mutableListOf<Pair<String, String>>()
    var onCopy: () -> Unit = {}

    override fun copy(
        sourcePath: String,
        targetPath: String
    ) {
        copies += sourcePath to targetPath
        onCopy()
    }
}

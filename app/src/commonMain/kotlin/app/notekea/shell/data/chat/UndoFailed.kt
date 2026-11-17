package app.notekea.shell.data.chat

/** A step of Undo that could not be taken back (a move or a trash that failed): counted, never swallowed. */
internal class UndoFailed : RuntimeException()

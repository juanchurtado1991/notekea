package app.notekea.chat.domain.loop

/** What settled a task's frame, or left it open (`docs/agent-frame.md`): the code, or which of the model's answers. */
enum class FrameReason {
    /** The code: only one note of the set has a word of its name the task writes ("Laura Castro's email"). */
    NAMES,

    /** The code: a line of the person's memory names one note of the set ("Laura" is [[Laura Castro]]). */
    MEMORY,

    /** The code: only one note of the set writes a word of the task, or none does and the set stays open. */
    WORDS,

    /** The model, which marked each note of a set yes or no, and the code, which counted the marks. */
    MARKS,

    /** The model, asked which shown note the task is about, when no set fits its words. */
    PICK,

    /** The model, which marked each entry of the note yes or no, and the code, which counted the marks. */
    ENTRY,

    /** Nothing was settled: the task means several things, every one, or none. */
    NONE
}

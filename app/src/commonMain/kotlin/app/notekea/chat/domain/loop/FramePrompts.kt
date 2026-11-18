package app.notekea.chat.domain.loop

/** What the frame's model is told for each of its questions (`docs/agent-frame.md`, `FrameQuestions`). */
internal object FramePrompts {
    const val NOT_SAID = "not said"
    const val WHOLE = "whole note"

    const val COUNT =
        """You read a request made to a notes app and say how many things in the person's notes it is about. You
are not shown the notes: judge by the request's words. Reply with JSON:
- count: "one" when it is about one thing: one note, one person, one entry ("Laura's phone", "the budget", "my
  goals", "move the appointment to 11:00", "this note", "what did Ana lend me"). "several" when it names more
  than one thing ("both budgets", "my two budgets", "Ana and Luis", "compare the Lisbon and Porto trips").
  "every" when it is about every note or entry a condition picks out ("all clients with an unpaid invoice", "my
  recipes", "the notes tagged travel", "every open task"). "none" when it is about no particular note (write a
  new note from nothing, a question about the app or the world).
- words: the request's own words for what it is about, copied as written ("Laura", "the budget")."""

    const val SAID =
        """A request made to a notes app names something that fits each of the notes below (N1, N2…): they share a
name. Say whether the request, or the person's memory, says which one it means. Reply with JSON, pick: the note,
only when a word of the request or the memory points to it and not to the others (a surname, a folder, a detail
only that note's lines have); else "$NOT_SAID". Do not guess: "$NOT_SAID" is right whenever both still fit."""

    const val PICK =
        """A request made to a notes app is about one thing in the person's notes. Reply with JSON, pick: the note
below (N1, N2…) it is about, or "$NOT_SAID" when none of them is."""

    const val ENTRY =
        """A request made to a notes app changes something in the note below. Its entries are listed (E1, E2…: an
appointment, an order, a row). Reply with JSON, pick: the one entry the request changes, when its words point to
that entry and not to the others (a date, a time, a name, an amount); "$NOT_SAID" when it is about one entry and
its words fit several equally ("the appointment" with three appointments listed); "$WHOLE" when it changes the
note as a whole or adds something new to it."""

    const val SEVERAL =
        """A request made to a notes app is about several things it names. You are shown the notes it may be about
(N1, N2…). Reply with JSON: picks, every shown note the request names or means, and no other."""
}

package app.notekea.chat.domain.loop

/** What the frame's model is told for each of its questions (`docs/agent-frame.md`, `FrameQuestions`). */
internal object FramePrompts {
    const val NOT_SAID = "not said"
    const val YES = "yes"
    const val NO = "no"
    const val ITEM = "item"
    const val NOTE = "note"
    const val CHANGES = "changes"
    const val ADDS = "adds"
    const val ASKS = "asks"

    const val COUNT =
        """You read a request made to a notes app and say how many things in the person's notes it is about. You
are not shown the notes: judge by the request's words. Reply with JSON:
- count: "one" when it is about one thing: one note, one person, one entry ("Laura's phone", "the budget", "my
  goals", "move the appointment to 11:00", "this note", "what did Ana lend me"). "several" when it names more
  than one thing ("both budgets", "my two budgets", "Ana and Luis", "compare the Lisbon and Porto trips").
  "every" when it is about every note or entry a condition picks out ("all clients with an unpaid invoice", "my
  recipes", "the notes tagged travel", "every open task"). "none" when it is about no particular note (write a
  new note from nothing, a question about the app or the world).
- words: the request's own words for what it is about, copied as written ("Laura", "the budget"). For a request
  that adds, the words for where it goes or whom it is about ("my recipe", "Contactos", "Laura" in "add a task to
  call Laura"), never what it adds.
- thing: "item" when the one thing is an item already inside a note that the request changes or asks about (an
  appointment, an order, a row, a task, a payment); "note" when it is a whole note, a person or a document, and when
  the request adds something new to a note ("add a task to my recipe", "add Ana to Contactos").
- does: what the request does. "changes" when it changes, moves, cancels, marks or removes something already
  written inside a note (an appointment, an order, a row, a task, a payment). "adds" when it adds something new to a
  note, a list or a log, or writes a new note. "note" when it renames, moves, tags or deletes a whole note. "asks"
  when it asks about something."""

    const val MARK_NOTES =
        """A request made to a notes app is about one thing in the person's notes, and each note below (N1, N2…)
could be it. For each note, say "yes" when the request could be about that note: everything the request says about
its thing matches the note, as far as its name and lines show. Say "no" when something the request says does not
match it: another name, another place, another date, another kind of thing. The person's memory, when shown, says
what their words mean. Reply with JSON: every note's id with "yes" or "no"."""

    const val PICK =
        """A request made to a notes app is about one thing in the person's notes. Reply with JSON, pick: the note
below (N1, N2…) it is about, or "$NOT_SAID" when none of them is."""

    const val MARK_ENTRIES =
        """A request made to a notes app is about one entry of the note below: an appointment, an order, a row, a
task. For each entry (E1, E2…), say "yes" when the request could be about it: everything the request says about its
thing matches the entry, and what the request asks is still possible for it (an order already delivered cannot be
marked delivered, a task done cannot be marked done). Say "no" when something the request says does not match it. A
new value the request gives, such as a new time, date or amount to set, says nothing about which entry it is. Reply
with JSON: every entry's id with "yes" or "no"."""

    const val SEVERAL =
        """A request made to a notes app is about several things it names. You are shown the notes it may be about
(N1, N2…). Reply with JSON: picks, every shown note the request names or means, and no other."""
}

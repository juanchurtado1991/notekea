package app.notekea.chat.domain.loop

/** What the frame's model is told for each of its questions (`docs/agent-frame.md`, `FrameQuestions`). */
internal object FramePrompts {
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

    const val ONE =
        """A request made to a notes app is about one thing in the person's notes. You are shown the notes it may be
about (N1, N2…) and entries inside them (E1, E2…: an appointment, an order, a row). Reply with JSON:
- note: the shown note the request means. Answer "unclear" ONLY when two or more shown notes fit its words
  equally (two notes with the same name, two people with the same first name and no surname given) and nothing
  in the request or the person's memory tells them apart; then list in fit every note that fits. Otherwise fit
  is empty.
- entry, when entries are shown: the one entry of that note the request is about, when it is about one entry
  and not the whole note; "unclear" when several entries of the note fit equally and the request does not say
  which ("the appointment" in a note of three appointments), listing them in fit_entries; "none" when the
  request is about the whole note, a question about it, or something to add to it.
Never guess between equal fits: say unclear, and the app asks the person."""

    const val SEVERAL =
        """A request made to a notes app is about several things it names. You are shown the notes it may be about
(N1, N2…). Reply with JSON: notes, every shown note the request names or means, and no other."""
}

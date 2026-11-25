package app.notekea.chat.domain.loop

/** What the frame's model is told for each of its questions (`docs/agent-frame.md`, `FrameQuestions`). */
internal object FramePrompts {
    const val NOT_SAID = "not said"
    const val YES = "yes"
    const val NO = "no"
    const val ITEM = "item"
    const val NOTE = "note"
    const val NEW = "new"

    const val COUNT =
        """You read a request made to a notes app and say how many things in the person's notes it is about. You
are not shown the notes: judge by the request's words. Reply with JSON:
- count: "one" when it is about one thing: one note, one person, one entry ("Laura's phone", "the budget", "my
  goals", "move the appointment to 11:00", "this note", "what did Ana lend me"). "several" when it names more
  than one thing ("both budgets", "my two budgets", "Ana and Luis", "compare the Lisbon and Porto trips").
  "every" when it is about every note or entry a condition picks out ("all clients with an unpaid invoice", "my
  recipes", "the notes tagged travel", "every open task"). "none" when it is about no particular note (write a
  new note from nothing, a question about the app or the world).
- words: the shortest words of the request that name the one thing, copied as written: a person, a note, a place, an
  order. Never the verb, what is asked about it, or a value it is given. "what's Laura's phone" -> "Laura". "how much
  do I pay Ana a day" -> "Ana". "how much of the garden budget is left" -> "the garden budget". "move the appointment
  with Pau to 11:00" -> "the appointment with Pau".
- which: what else the request says to tell it from another of the same name, copied as written, or "": "what did
  Tomás, the one from the chess club, say about the hall" -> "the one from the chess club". Never what is asked about
  it: "what's Laura's phone" -> "".
- thing: "new" when the request adds to a note something not yet written in it: a task, a reminder, a line, a row,
  an entry ("add a task to my recipe", "add Ana to Contactos", "log today's run", "add 45 for bags to the budget").
  "item" when it changes or asks about one line already written inside a note (an appointment moved, an order
  cancelled, a row or a payment marked, a task done). "note" when it is a whole note, a person or a document, and
  always when the request tags, links, renames or moves the note or sets a property of it ("tag my sleep log",
  "link the trip with its budget")."""

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

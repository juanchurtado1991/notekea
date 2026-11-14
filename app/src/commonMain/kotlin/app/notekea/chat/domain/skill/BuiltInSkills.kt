package app.notekea.chat.domain.skill

/**
 * The skills Notekea ships with: for a small model the shape of a task said plainly is worth more than a longer
 * prompt. A folder of the vault may add the person's own, or replace one of these by its name ([VaultGuide]).
 */
object BuiltInSkills {
    val all: List<Skill> = listOf(
        Skill(
            name = "comparison table",
            description = "compare two or more things read in the notes, side by side",
            instructions = "Read each thing to compare first, by its outline when it is long. Note, for each, " +
                "the same points with their passages. Write one Markdown table: a row a point, a column a " +
                "thing, each cell a few words taken from what you read. A point one of them does not " +
                "state gets \"not stated\", never a guess. End with one sentence on what differs most."
        ),
        Skill(
            name = "document summary",
            description = "summarize a long note or an attached document",
            instructions = "Look at its outline, then digest it with the request as the question, so it is " +
                "read to its end. Write the summary in its order: what it is and between whom, then a short " +
                "paragraph or a bullet for each part that matters, with its figures and dates as written. " +
                "Say what it does not cover only if it was asked. No opening line about what a summary is."
        ),
        Skill(
            name = "study cards",
            description = "make question and answer cards to study a note",
            instructions = "Read the note. Make a card for each fact worth remembering: a question that has " +
                "one answer, and the answer in the note's own words, short. No card for what the note does " +
                "not say, and no two cards for one fact. Propose them with make_cards for that note."
        ),
        Skill(
            name = "meeting notes",
            description = "turn rough notes of a meeting into tidy ones",
            instructions = "Read the rough notes. Write: the date and who was there, if written; Decisions, " +
                "one a line; Tasks, as open tasks with who does each and its date when one is said (ask " +
                "date for a day named as a phrase); Open questions. Keep every name and figure as written " +
                "and leave out a section that would be empty."
        )
    )
}

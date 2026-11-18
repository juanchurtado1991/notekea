package app.notekea.chat.domain.loop

/**
 * The set of shown notes a task's words fit alike (`docs/agent-frame.md`): those whose names, not folders, write
 * every word of the task any shown name writes (two Budget 2026, two Lauras). Found by the vault's own names, never a
 * list of words; a set the names tell apart is settled by them ([settledOf]), the model is asked of the rest.
 */
internal object FrameGroups {
    /**
     * The one set, or none. Notes sharing one word of several the names write are no set: "the food in the Azores"
     * was open between Food bank shifts and Vocabulary - Food while it meant the Azores trip (the frame alone,
     * 2026-11-18). One name writing them all, among others writing some, is a set of one: that note.
     */
    fun of(
        task: String,
        candidates: List<FrameCandidates.Candidate>
    ): List<List<FrameCandidates.Candidate>> {
        val named = FrameWords
            .of(text = task)
            .filter { word ->
                word.any { it.isLetter() } && candidates.any { isNamed(it, word) }
            }

        val holding = candidates.filter { candidate ->
            named.any { isNamed(candidate, it) }
        }

        val full = holding.filter { candidate ->
            named.all { isNamed(candidate, it) }
        }

        // One name writing them all among others writing some is that note, as its name settles a set.
        return listOf(full).filter {
            holding.size >= 2 && it.size in 1..MOST
        }
    }

    /**
     * The one note of [group] whose name has a word the others' names do not and the task writes ("Laura Castro's
     * email", "the work budget" for Work/Budget 2026), else the one whose whole name the task writes ("Declan Moran"
     * beside "Transcript - Declan Moran"); null when the names leave it to what the notes say.
     */
    fun settledOf(
        task: String,
        group: List<FrameCandidates.Candidate>
    ): FrameCandidates.Candidate? {
        // A number of a name is a word of it too: "ADR-015", "sprint 47", "chapter 2" (the frame alone, 2026-11-18).
        val words = FrameWords.of(text = task) + FrameWords.numbersOf(text = task)

        val named = group.filter { candidate ->
            val others = group
                .filter {
                    it != candidate
                }
                .flatMap { titleOf(candidate = it) }
                .toSet()

            (titleOf(candidate = candidate) - others).any { own ->
                words.any {
                    FrameWords.isSame(
                        one = it,
                        other = own
                    )
                }
            }
        }

        // A note named by the task's words alone is the thing; the others are about it (the frame alone, 2026-11-18:
        // "pon Jaén 9 como reservado" was open between Pisos/Jaén 9 and Visitas/Visitas Jaén 9).
        val whole = group.filter { candidate ->
            val name = candidate.path.nameWithoutExtension
            val own = FrameWords.of(text = name) + FrameWords.numbersOf(text = name)

            own.isNotEmpty() && own.all { word ->
                words.any {
                    FrameWords.isSame(
                        one = it,
                        other = word
                    )
                }
            }
        }

        return named.singleOrNull() ?: whole.singleOrNull()
    }

    /**
     * The notes of [group] that write a word of [task] no other note of the group writes (`docs/loop-goal-plan.md`,
     * step 2.2): "Tomás from the chess club" for the Tomás whose note says chess. One is the note meant, none says
     * nothing tells them apart, several leave it to the model.
     */
    fun toldOf(
        task: String,
        group: List<FrameCandidates.Candidate>
    ): List<FrameCandidates.Candidate> {
        // What the task quotes is what it writes, not what it is about: "add 'get a first-aid certificate' to my
        // 2027 goals" was told apart by the Goals 2027 that writes certificate (the loop, 2026-11-18).
        val words = FrameWords.of(
            text = task.replace(
                regex = QUOTED,
                replacement = " "
            )
        )

        return group.filter { candidate ->
            val others = group
                .filter {
                    it != candidate
                }
                .flatMap { wordsOf(candidate = it) }

            words.any { word ->
                val isOwn = wordsOf(candidate = candidate).any {
                    FrameWords.isSame(
                        one = it,
                        other = word
                    )
                }

                isOwn && others.none {
                    FrameWords.isSame(
                        one = it,
                        other = word
                    )
                }
            }
        }
    }

    private fun isNamed(
        candidate: FrameCandidates.Candidate,
        word: String
    ): Boolean = nameOf(candidate = candidate).any {
        FrameWords.isSame(
            one = it,
            other = word
        )
    }

    // Every word a note writes, with those of its path.
    private fun wordsOf(candidate: FrameCandidates.Candidate): Set<String> =
        candidate.words + titleOf(candidate)

    // The words of a note's name: what a set is made by. A folder's words made "the books budget" a set of every note
    // in Books (the loop's run of 2026-11-17).
    private fun nameOf(candidate: FrameCandidates.Candidate): Set<String> =
        FrameWords.of(text = candidate.path.nameWithoutExtension)

    // The words and numbers of a note's path: its name and its folders, which tell Home/Budget from Work/Budget.
    private fun titleOf(candidate: FrameCandidates.Candidate): Set<String> {
        val title = candidate
            .path
            .value
            .removeSuffix(suffix = ".md")

        return FrameWords.of(text = title) + FrameWords.numbersOf(text = title)
    }

    private const val MOST = 6

    // A quote opens where no letter is before it, so "Laura's" opens none.
    private val QUOTED =
        Regex(pattern = "(?<!\\p{L})'[^']*'(?!\\p{L})|\"[^\"]*\"|‘[^’]*’|“[^”]*”|«[^»]*»")
}

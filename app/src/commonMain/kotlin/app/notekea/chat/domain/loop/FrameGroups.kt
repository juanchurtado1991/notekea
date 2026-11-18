package app.notekea.chat.domain.loop

/**
 * The sets of shown notes a task's words fit alike (`docs/agent-frame.md`): those whose names share a word the task
 * writes (two Budget 2026, two Lauras), else whose folder does. Found by the vault's own names, never a word list;
 * a set the names alone tell apart is settled by them ([settledOf]), and the model is asked of the rest.
 */
internal object FrameGroups {
    fun of(
        task: String,
        candidates: List<FrameCandidates.Candidate>
    ): List<List<FrameCandidates.Candidate>> = groupsOf(task, candidates, ::nameOf).ifEmpty {
        // A word no name has but a folder does names a kind: "the project" with Projects/Greenhouse and
        // Projects/Orchard (the loop's run of 2026-11-17). Only then: in "the books budget" the names say budget.
        groupsOf(task, candidates, ::folderOf)
    }

    private fun groupsOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        wordsOf: (FrameCandidates.Candidate) -> Set<String>
    ): List<List<FrameCandidates.Candidate>> = FrameWords
        .of(text = task)
        .filter { word ->
            word.any { it.isLetter() }
        }
        .map { word ->
            candidates.filter { candidate ->
                wordsOf(candidate).any {
                    FrameWords.isSame(
                        one = it,
                        other = word
                    )
                }
            }
        }
        .filter {
            it.size in 2..MOST
        }
        .distinct()

    /**
     * The one note of [group] whose name has a word the others' names do not and the task writes ("Laura Castro's
     * email", "the work budget" for Work/Budget 2026); null when the names leave it to what the notes say.
     */
    fun settledOf(
        task: String,
        group: List<FrameCandidates.Candidate>
    ): FrameCandidates.Candidate? {
        val words = FrameWords.of(text = task)

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

        return named.singleOrNull()
    }

    // The words of a note's name: what a set is made by. A folder's words made "the books budget" a set of every note
    // in Books (the loop's run of 2026-11-17).
    private fun nameOf(candidate: FrameCandidates.Candidate): Set<String> =
        FrameWords.of(text = candidate.path.nameWithoutExtension)

    // The words of a note's folders, the kind of thing it is.
    private fun folderOf(candidate: FrameCandidates.Candidate): Set<String> = FrameWords.of(
        text = candidate
            .path
            .value
            .substringBeforeLast(
                delimiter = '/',
                missingDelimiterValue = ""
            )
    )

    // The words of a note's path: its name and its folders, which tell Home/Budget from Work/Budget.
    private fun titleOf(candidate: FrameCandidates.Candidate): Set<String> = FrameWords.of(
        text = candidate
            .path
            .value
            .removeSuffix(suffix = ".md")
    )

    private const val MOST = 6
}

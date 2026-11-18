package app.notekea.chat.domain.loop

/**
 * The sets of shown notes a task's name for its thing fits alike (`docs/agent-frame.md`): notes of one name in two
 * folders, and people who share a first name, when the task writes that name. Found by the vault's own names; the
 * model then says only whether the task, or the memory, says which of a set it means.
 */
internal object FrameGroups {
    fun of(
        task: String,
        candidates: List<FrameCandidates.Candidate>
    ): List<List<FrameCandidates.Candidate>> {
        val words = FrameWords.of(text = task)

        fun isSaid(word: String): Boolean = words.any {
            FrameWords.isSame(
                one = it,
                other = word
            )
        }

        val namesakes = candidates
            .groupBy {
                it
                    .path
                    .nameWithoutExtension
                    .lowercase()
            }
            .values
            .filter { group ->
                val title = FrameWords.of(
                    text = group
                        .first()
                        .path
                        .nameWithoutExtension
                )

                group.size > 1 && title.any { isSaid(word = it) }
            }

        val firstNames = candidates
            .filter {
                titleOf(candidate = it).size > 1
            }
            .groupBy { titleOf(candidate = it).first() }
            .filter { (first, group) ->
                group.size > 1 && isSaid(word = first)
            }
            .values

        return (namesakes + firstNames)
            .distinct()
            .filter {
                it.size <= MOST
            }
    }

    private fun titleOf(candidate: FrameCandidates.Candidate): List<String> =
        FrameWords.of(text = candidate.path.nameWithoutExtension).toList()

    private const val MOST = 6
}

package app.notekea.chat.domain.loop

import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.core.domain.Outcome
import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemoryLines
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The frame of a turn (`docs/agent-frame.md`): the notes the task may be about, found by the code, and which of them
 * it means, said by the model ([TaskFramer]). The notes the memory names for the task's words and the open note come
 * first, then those the search finds for the task.
 */
class TurnFrames(
    private val framer: TaskFramer,
    private val answerFromNotes: AnswerFromNotesUseCase,
    private val vaultGuide: VaultGuide
) : FrameSource {
    override suspend fun of(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        memory: List<MemoryLine>,
        openNote: RelativePath?,
        isChange: Boolean
    ): TaskFrame? {
        val found = (answerFromNotes.excerpts(vaultPath, notes, task) as? Outcome.Success)
            ?.value
            .orEmpty()
            .map { it.note }
            .distinct()

        val first = rememberedOf(
            task = task,
            notes = notes,
            memory = memory
        ) + listOfNotNull(openNote)

        val paths = FrameCandidates.pathsOf(task, notes, found, first)
        val byPath = notes.associateBy { it.file.path }

        val texts = vaultGuide.textsOf(
            vaultPath,
            paths.mapNotNull {
                byPath[it]
            }
        )

        return framer.frameOf(
            task = task,
            candidates = FrameCandidates.of(
                task = task,
                paths = paths,
                texts = texts
            ),
            memory = memory,
            isChange = isChange
        )
    }

    // The notes the memory's lines link, for the lines whose words the task says: "my goals" is [[Work/Goals 2027]].
    private fun rememberedOf(
        task: String,
        notes: Collection<NoteEntry>,
        memory: List<MemoryLine>
    ): List<RelativePath> = memory
        .flatMap { it.names }
        .filter {
            MemoryLines.says(
                text = task,
                said = it.said
            )
        }
        .mapNotNull { named ->
            val link = named
                .note
                .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                .lowercase()

            notes
                .map { it.file.path }
                .firstOrNull {
                    val path = it
                        .value
                        .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                        .lowercase()

                    if ('/' in link) path == link else it.nameWithoutExtension.lowercase() == link
                }
        }
}

package app.notekea.chat.domain.plan

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The vault's notes and folders by the names a person gives them: a note by its name without `.md` (or its path),
 * ignoring case, "this note" the [openNote]; a folder by its path or its last name. Folders a plan creates count for
 * its later steps, so "create Archive and move Retro into it" checks.
 */
internal class VaultNames(
    private val notes: Collection<NoteEntry>,
    private val openNote: RelativePath?
) {
    private val paths = notes.map { it.file.path }.toMutableList()
    private val folders = paths.flatMap { ancestorsOf(path = it) }.toMutableSet()

    fun targetsOf(step: PlannedStep): Outcome<StepTargets, ActionProblem> = try {
        Outcome.Success(value = resolve(step = step))
    } catch (problem: ProblemFound) {
        Outcome.Failure(error = problem.problem)
    }

    private fun resolve(step: PlannedStep): StepTargets {
        val kind = step.kind
        // A small model may write a step's one note in its list: the same note.
        val named = step[ActionField.NOTE] ?: step.notes.firstOrNull()
        val folderName = step[ActionField.FOLDER]

        return when (kind) {
            ActionKind.CREATE_NOTE, ActionKind.NOTES_FROM_TEXT -> StepTargets(
                folder = folderName?.let {
                    newOrExisting(
                        name = it,
                        isNew = kind == ActionKind.NOTES_FROM_TEXT
                    )
                },
                template = step[ActionField.TEMPLATE]?.let { noteOf(name = it) }
            ).also {
                created(
                    step = step,
                    folder = it.folder
                )
            }

            ActionKind.CREATE_FOLDER -> StepTargets(folder = newFolder(name = folderName.orEmpty()))

            ActionKind.WRITE_FROM_NOTES -> StepTargets(
                notes = selected(
                    step = step,
                    isOpenByDefault = true
                ),
                folder = folderName?.let { folderOf(name = it) }
            )

            ActionKind.APPEND_TEXT, ActionKind.ADD_TASK -> StepTargets(
                notes = listOfNotNull(named?.let { noteOf(name = it) })
            )

            ActionKind.LINK_NOTES -> StepTargets(
                notes = listOf(noteOf(name = named.orEmpty())),
                target = noteOf(name = step[ActionField.TARGET].orEmpty())
            )

            ActionKind.MOVE -> StepTargets(
                notes = selected(
                    step = step,
                    isOpenByDefault = true
                ),
                folder = folderName?.let { folderOf(name = it) },
                target = movedTo(name = step[ActionField.TARGET].orEmpty())
            )

            ActionKind.RENAME, ActionKind.BOOKMARK, ActionKind.TRASH -> StepTargets(
                notes = notesUnlessFolder(
                    step = step,
                    folderName = folderName
                ),
                folder = folderName?.let { folderOf(name = it) }
            )

            ActionKind.SHOW_TASKS -> StepTargets(folder = folderName?.let { folderOf(name = it) })

            else -> StepTargets(
                notes = selected(
                    step = step,
                    isOpenByDefault = kind.isWriting
                )
            )
        }
    }

    /** The step's notes, unless it names a folder, which it then acts on whole. */
    private fun notesUnlessFolder(
        step: PlannedStep,
        folderName: String?
    ): List<RelativePath> = folderName?.let { emptyList() } ?: selected(
        step = step,
        isOpenByDefault = true
    )

    /** The notes a step acts on: those named, a folder's or a tag's, or the open one when it names none. */

    private fun selected(
        step: PlannedStep,
        isOpenByDefault: Boolean
    ): List<RelativePath> {
        val named = (listOfNotNull(step[ActionField.NOTE]) + step.notes).map { noteOf(name = it) }
        val folder = step[ActionField.FOLDER]?.let { folderOf(name = it) }

        val inFolder = folder?.let { found ->
            paths.filter { it.isWithin(folder = found) }
        }.orEmpty()

        val tagged = step[ActionField.TAG]
            ?.takeIf {
                step.kind != ActionKind.ADD_TAG
            }
            ?.let { tag ->
                notes.filter {
                    tag.removePrefix(prefix = "#") in it.metadata.tags
                }.map { it.file.path }
            }
            .orEmpty()

        val all = (named + inFolder + tagged).distinct()
        if (all.isNotEmpty() || !isOpenByDefault) return all

        return listOfNotNull(openNote)
    }

    fun noteOf(name: String): RelativePath {
        val wanted = name
            .trim()
            .removeSuffix(suffix = MARKDOWN)
            .removeSurrounding(
                prefix = "[[",
                suffix = "]]"
            )

        val isOpen = wanted.lowercase() in THIS_NOTE
        if (isOpen) return openNote ?: throw ProblemFound(ActionProblem.UnknownNote(name = name))

        return paths.firstOrNull {
            it
                .value
                .removeSuffix(suffix = MARKDOWN)
                .equals(wanted, ignoreCase = true)
        }
            ?: paths.firstOrNull { it.nameWithoutExtension.equals(wanted, ignoreCase = true) }
            ?: throw ProblemFound(problem = ActionProblem.UnknownNote(name = name))
    }

    private fun folderOf(name: String): RelativePath {
        val wanted = name.trim().trim('/')

        return folders.firstOrNull { it.value.equals(wanted, ignoreCase = true) }
            ?: folders.firstOrNull { it.name.equals(wanted, ignoreCase = true) }
            ?: throw ProblemFound(problem = ActionProblem.UnknownFolder(name = name))
    }

    /** Where a move goes: a folder of the vault, or a new one the move makes ("move Pancakes to Desayunos"). */
    private fun movedTo(name: String): RelativePath = runCatching { folderOf(name = name) }
        .getOrElse { newFolder(name = name) }

    private fun newOrExisting(
        name: String,
        isNew: Boolean
    ): RelativePath = if (isNew) newFolder(name = name) else folderOf(name = name)

    /** A folder a step creates: an existing one is fine, and later steps may use it. */
    private fun newFolder(name: String): RelativePath {
        val wanted = name.trim().trim('/')
        if (wanted.isEmpty()) throw ProblemFound(problem = ActionProblem.UnknownFolder(name = name))

        val existing = folders.firstOrNull { it.value.equals(wanted, ignoreCase = true) }
        if (existing != null) return existing

        // "Recipes/Desserts" lands in the vault's Recipes, as the person sees it, whatever its case.
        val parent = wanted.substringBeforeLast(
            delimiter = '/',
            missingDelimiterValue = ""
        )

        val base = if (parent.isEmpty()) RelativePath.ROOT else folderOf(name = parent)
        val created = base.child(name = wanted.substringAfterLast(delimiter = '/'))

        folders += ancestorsOf(path = created.child(name = "x")) + created

        return created
    }

    /** The note a new-note step makes, so a later step may name it: "create Budget, status todo". */
    private fun created(
        step: PlannedStep,
        folder: RelativePath?
    ) {
        val title = step[ActionField.TITLE]
        val isNote = step.kind == ActionKind.CREATE_NOTE && title != null
        if (!isNote) return

        paths += (folder ?: RelativePath.ROOT).child(name = "$title$MARKDOWN")
    }

    private fun ancestorsOf(path: RelativePath): List<RelativePath> =
        generateSequence(path.parent) { it.parent }.filterNot { it.isRoot }.toList()

    private class ProblemFound(val problem: ActionProblem) : RuntimeException()

    private companion object {
        const val MARKDOWN = ".md"
        val THIS_NOTE =
            setOf("this note", "esta nota", "la nota abierta", "the open note", "current note")
    }
}

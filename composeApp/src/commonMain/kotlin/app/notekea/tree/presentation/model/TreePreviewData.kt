package app.notekea.tree.presentation.model

import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.tree.domain.FlattenTreeUseCase
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.TreeSort
import app.notekea.core.notes.domain.VaultTree
import kotlin.time.Instant

/** A believable vault for the sidebar previews: the shape of the `Personal Notes` smoke vault. */
internal object TreePreviewData {
    const val VAULT_NAME = "Personal Notes"
    const val VAULT_PATH = "/home/ana/Notes"
    private const val MODIFIED_MILLIS = 1_800_000_000_000L
    private const val SIZE = 512L
    private val GARDEN = RelativePath(value = "Projects/Garden")
    private val PROJECTS = RelativePath(value = "Projects")
    private val TOMATO_PLAN = RelativePath(value = "Projects/Garden/Tomato plan.md")
    private val PATHS = listOf(
        "Welcome.md",
        "Ideas/Reading list.md",
        "Journal/2026-10-20.md",
        "Journal/2026-10-21.md",
        "Projects/Garden/Tomato plan.md",
        "Recipes/Arepas.md",
        "Recipes/Café con leche.md",
        "Travel/Lisbon trip.md"
    )
    private val TOMATO_METADATA =
        NoteMetadata(tags = listOf("garden", "plans"), aliases = listOf("Tomatoes"), title = "Tomato plan 2026")

    val tree: VaultTree = PATHS.fold(initial = VaultTree.EMPTY) { tree, path ->
        val file = NoteFile(
            path = RelativePath(value = path),
            size = SIZE,
            modifiedAt = Instant.fromEpochMilliseconds(epochMilliseconds = MODIFIED_MILLIS)
        )
        tree.withNote(
            note = NoteEntry(
                file = file,
                metadata = if (file.path ==
                    TOMATO_PLAN
                ) {
                    TOMATO_METADATA
                } else {
                    NoteMetadata.NONE
                }
            )
        )
    }

    fun state(
        expanded: Set<RelativePath> = setOf(PROJECTS, GARDEN),
        selected: RelativePath? = TOMATO_PLAN
    ): VaultTreeState = VaultTreeState(
        vaultPath = VAULT_PATH,
        platformConventions = PreviewPlatformConventions.linux,
        isLoading = false,
        tree = tree,
        rows = FlattenTreeUseCase(
            naturalNameComparator = NaturalNameComparator()
        )(tree = tree, expandedFolders = expanded, sort = TreeSort.NAME),
        expandedFolders = expanded,
        selectedPath = selected
    )

    fun renamingState(): VaultTreeState = state().copy(
        renaming = RenameDraft(path = TOMATO_PLAN, text = "Tomato plan 2027")
    )
}

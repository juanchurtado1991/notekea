package app.notekea.core.presentation

/**
 * Marks on the parts of the window whose text the Self-Test must find in one place only: the same note names show in
 * the tree, the palette's results and the inspector's sections (Related, Connections), so a case that means the tree
 * looks inside [TREE]. Invisible to people and screen readers.
 */
object TestTags {
    const val PALETTE_RESULTS = "palette-results"
    const val TREE = "tree"
}

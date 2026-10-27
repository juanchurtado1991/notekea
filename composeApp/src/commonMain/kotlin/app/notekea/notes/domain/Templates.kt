package app.notekea.notes.domain

/**
 * Which notes are templates (`ROADMAP.md` 1.12), as Obsidian's Templates plugin sees them: every note in the
 * [TemplateSettings.folder] and its subfolders, named by its path inside that folder without `.md`.
 */
class Templates {
    private val comparator = NaturalNameComparator()

    fun listing(
        tree: VaultTree,
        settings: TemplateSettings
    ): TemplateListing {
        val folder = RelativePath(value = settings.folder)
        if (!folder.isRoot && folder !in tree.folders) return TemplateListing.NoFolder
        val templates = tree.notes.keys.filter { it.isWithin(folder = folder) && it != folder }
        return TemplateListing.Found(templates = templates.sortedWith { a, b -> comparator.compare(a.value, b.value) })
    }

    /** How [template] is named in the picker: `Meeting notes`, or `Work/Weekly review` in a subfolder. */
    fun nameOf(
        template: RelativePath,
        settings: TemplateSettings
    ): String {
        val inside = if (settings.folder.isEmpty()) {
            template.value
        } else {
            template.value.removePrefix(prefix = settings.folder + RelativePath.SEPARATOR)
        }
        return inside.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
    }
}

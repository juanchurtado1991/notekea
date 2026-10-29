package app.notekea.selftest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText

/**
 * The scenarios of `smoke-data/smoke.sh`, in Kotlin, so the Self-Test builds them on any machine: [prepare] makes in
 * [scratch] what `smoke.sh prepare` makes in `smoke-data/scratch` from [smokeData] (the repository's `smoke-data`),
 * and the other functions change it as the script's commands do. A test checks both build the same files.
 */
@OptIn(ExperimentalPathApi::class)
class SmokeScenarios(
    private val smokeData: Path,
    private val scratch: Path
) {
    private val playground = scratch.resolve(PLAYGROUND)

    fun prepare() {
        scratch.deleteRecursively()
        Files.createDirectories(scratch.resolve("New Vault Location/My Notes"))
        Files.createDirectories(scratch.resolve("Locked Vault"))
        copy(from = "vaults/Personal Notes", to = "Movable Vault")
        copy(from = "vaults/Personal Notes", to = PLAYGROUND)
        datedRecipes()
        scratch.resolve("New Vault Location/My Notes/Existing note.md").writeText("# Existing note\n")
        scratch.resolve("Locked Vault/Locked note.md").writeText("# Locked note\n")
        EditorNotes.write(folder = playground.resolve(EDITOR))
        EDITOR_NOTES.forEach { copy(from = "notes/$it", to = "$PLAYGROUND/$EDITOR/$it") }
        copy(from = "notes/embeds", to = "$PLAYGROUND/$EDITOR/Embeds")
        copy(from = "daily", to = "Daily Vault")
        copy(from = "templates", to = "Templates Vault")
        copyContents(from = "attachments", to = "$PLAYGROUND/attachments")
        copyContents(from = "outside", to = "Outside files")
    }

    /** `smoke.sh outside`: another app creates, edits and deletes a note of the Playground Vault. */
    fun outside() {
        Files.createDirectories(playground.resolve("Inbox"))
        playground.resolve("Inbox/Created outside.md").writeText("---\ntags: [inbox]\n---\n\n# Created outside\n")
        playground.resolve("Travel/Lisbon trip.md")
            .writeText("---\ntags: [travel, edited]\ndates: 2026-11-14 to 2026-11-20\n---\n\n# Lisbon trip\n")
        Files.deleteIfExists(playground.resolve("Ideas/Reading list.md"))
    }

    /** `smoke.sh move`: the Movable Vault's folder moves away. */
    fun move() {
        Files.move(scratch.resolve("Movable Vault"), scratch.resolve("Moved Vault"))
    }

    /** `smoke.sh edit-note`: another app adds a line to Editor/Shared note. */
    fun editNote(time: String) {
        val note = playground.resolve("$EDITOR/Shared note.md")
        Files.writeString(note, Files.readString(note) + "\nEdited outside Notekea at $time.\n")
    }

    /** `smoke.sh delete-note`: another app deletes Editor/Shared note. */
    fun deleteNote() {
        Files.deleteIfExists(playground.resolve("$EDITOR/Shared note.md"))
    }

    // Dates that differ from the name order, so sorting Recipes by date modified visibly changes it.
    private fun datedRecipes() {
        RECIPE_DATES.forEach { (name, day) ->
            val time = LocalDateTime.of(2026, 10, day, 9, 0).atZone(ZoneId.systemDefault()).toInstant()
            Files.setLastModifiedTime(playground.resolve("Recipes/$name"), FileTime.from(time))
        }
    }

    private fun copy(
        from: String,
        to: String
    ) {
        val target = scratch.resolve(to)
        Files.createDirectories(target.parent)
        smokeData.resolve(from).copyToRecursively(target = target, followLinks = false, overwrite = true)
    }

    private fun copyContents(
        from: String,
        to: String
    ) {
        Files.createDirectories(scratch.resolve(to))
        Files.list(smokeData.resolve(from)).use { files ->
            files.forEach { copy(from = "$from/${it.fileName}", to = "$to/${it.fileName}") }
        }
    }

    private companion object {
        const val PLAYGROUND = "Playground Vault"
        const val EDITOR = "Editor"
        val EDITOR_NOTES = listOf(
            "Tasks and callouts.md",
            "Formatting practice.md",
            "Images and files.md",
            "Shopping basics.md",
            "Outline practice.md",
            "Properties practice.md",
            "Broken properties.md"
        )
        val RECIPE_DATES = listOf("Pasta e fagioli.md" to 20, "Arepas.md" to 10, "Café con leche.md" to 1)
    }
}

package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteScanner
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultListing
import kotlinx.coroutines.withContext
import okio.FileMetadata
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import kotlin.time.Instant

/**
 * [NoteScanner] on Okio, listing notes and every other visible file. The vault folder must be listable; a subfolder that cannot be listed is shown empty instead
 * of failing the whole scan. Metadata is read without following symbolic links, so links are neither folders nor
 * notes.
 */
class OkioNoteScanner(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : NoteScanner {
    override suspend fun scan(
        vaultPath: String
    ): Outcome<VaultListing, NotesError> = withContext(context = dispatchers.io) {
        when (val rootEntries = listRoot(vaultPath = vaultPath)) {
            is Outcome.Failure -> rootEntries
            is Outcome.Success -> Outcome.Success(value = listingOf(rootEntries = rootEntries.value))
        }
    }

    override suspend fun stat(
        vaultPath: String,
        path: RelativePath
    ): NoteFile? = withContext(context = dispatchers.io) {
        val isVisibleNote = path.isMarkdown && path.value.split(RelativePath.SEPARATOR).none { isHidden(name = it) }
        val metadata = if (isVisibleNote) metadataOrNull(path = vaultPath.toPath() / path.value) else null
        metadata?.takeIf { it.isRegularFile }?.let { noteFileOf(path = path, metadata = it) }
    }

    override suspend fun statAttachment(
        vaultPath: String,
        path: RelativePath
    ): NoteFile? = withContext(context = dispatchers.io) {
        val isVisible = !path.isMarkdown && path.value.split(RelativePath.SEPARATOR).none { isHidden(name = it) }
        val metadata = if (isVisible) metadataOrNull(path = vaultPath.toPath() / path.value) else null
        metadata?.takeIf { it.isRegularFile }?.let { noteFileOf(path = path, metadata = it) }
    }

    private fun collect(
        folder: RelativePath,
        entries: List<Path>,
        folders: MutableSet<RelativePath>,
        files: MutableList<NoteFile>,
        attachments: MutableList<NoteFile>
    ) {
        entries.filterNot { isHidden(name = it.name) }.forEach { entry ->
            val path = folder.child(name = entry.name)
            val metadata = metadataOrNull(path = entry) ?: return@forEach
            if (metadata.isDirectory) {
                folders += path
                collect(
                    folder = path,
                    entries = listOrEmpty(folder = entry),
                    folders = folders,
                    files = files,
                    attachments = attachments
                )
            } else if (metadata.isRegularFile) {
                val target = if (path.isMarkdown) files else attachments
                target += noteFileOf(path = path, metadata = metadata)
            }
        }
    }

    private fun isHidden(
        name: String
    ): Boolean = name.startsWith(prefix = HIDDEN_PREFIX)

    private fun listingOf(
        rootEntries: List<Path>
    ): VaultListing {
        val folders = mutableSetOf<RelativePath>()
        val files = mutableListOf<NoteFile>()
        val attachments = mutableListOf<NoteFile>()
        collect(
            folder = RelativePath.ROOT,
            entries = rootEntries,
            folders = folders,
            files = files,
            attachments = attachments
        )
        return VaultListing(folders = folders, files = files, attachments = attachments)
    }

    private fun listOrEmpty(
        folder: Path
    ): List<Path> = fileSystem.listOrNull(dir = folder).orEmpty()

    private fun listRoot(
        vaultPath: String
    ): Outcome<List<Path>, NotesError> = try {
        val root = resolvedRoot(vaultPath = vaultPath)
        if (root != null && metadataOrNull(path = root)?.isDirectory == true) {
            Outcome.Success(value = fileSystem.list(dir = root))
        } else {
            Outcome.Failure(error = NotesError.VaultMissing(path = vaultPath))
        }
    } catch (_: IOException) {
        Outcome.Failure(error = NotesError.PermissionDenied(path = vaultPath))
    }

    /** The vault folder itself may be a symbolic link (only links inside it are skipped); null when it is missing. */
    private fun resolvedRoot(
        vaultPath: String
    ): Path? {
        val root = vaultPath.toPath()
        val metadata = fileSystem.metadataOrNull(path = root) ?: return null
        if (metadata.symlinkTarget == null) return root
        return try {
            fileSystem.canonicalize(path = root)
        } catch (_: FileNotFoundException) {
            null
        }
    }

    private fun metadataOrNull(
        path: Path
    ): FileMetadata? = try {
        fileSystem.metadataOrNull(path = path)?.takeIf { it.symlinkTarget == null }
    } catch (_: IOException) {
        null
    }

    private fun noteFileOf(
        path: RelativePath,
        metadata: FileMetadata
    ): NoteFile = NoteFile(
        path = path,
        size = metadata.size,
        modifiedAt = metadata.lastModifiedAtMillis?.let { Instant.fromEpochMilliseconds(epochMilliseconds = it) }
    )

    companion object {
        private const val HIDDEN_PREFIX = "."
    }
}

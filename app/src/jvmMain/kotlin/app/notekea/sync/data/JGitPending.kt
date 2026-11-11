package app.notekea.sync.data

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator

/** The files changed here and not on the remote: uncommitted ones, and those of the commits not pushed yet. */
internal object JGitPending {
    private const val REMOTE_PREFIX = "refs/remotes/origin/"

    fun of(git: Git): Int {
        val status = git.status().call()

        val uncommitted = listOf(
            status.added,
            status.changed,
            status.modified,
            status.missing,
            status.removed,
            status.untracked
        ).flatten().toSet()

        return (uncommitted + unpushed(git = git)).size
    }

    private fun unpushed(git: Git): Set<String> {
        val repository = git.repository
        val head = repository.resolve(Constants.HEAD) ?: return emptySet()
        val remote = repository.resolve("$REMOTE_PREFIX${repository.branch}")

        val base = remote?.let {
            RevWalk(repository).use { walk ->
                walk.revFilter = RevFilter.MERGE_BASE
                walk.markStart(walk.parseCommit(head))
                walk.markStart(walk.parseCommit(it))
                walk.next()
            }
        }

        if (base != null && base.id == head) return emptySet()

        return repository.newObjectReader().use { reader ->
            val old = base?.let {
                CanonicalTreeParser().apply {
                    reset(reader, RevWalk(repository).use { walk -> walk.parseCommit(it).tree })
                }
            } ?: EmptyTreeIterator()

            val new = CanonicalTreeParser().apply {
                reset(reader, RevWalk(repository).use { walk -> walk.parseCommit(head).tree })
            }

            git
                .diff()
                .setOldTree(old)
                .setNewTree(new)
                .call()
                .map {
                    it.newPath.takeUnless { path ->
                        path == "/dev/null"
                    } ?: it.oldPath
                }
                .toSet()
        }
    }
}

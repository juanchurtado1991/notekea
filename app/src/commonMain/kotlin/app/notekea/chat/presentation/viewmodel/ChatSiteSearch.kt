package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatStep
import kotlinx.coroutines.Job

/**
 * "I do not have it, shall I look for it?" (the developer's request of 2026-11-13): a question the notes do not
 * answer is offered the sites of the vault's web sources; their pages closest to it are listed, and the ones the
 * person ticks are clipped into their section and the question answered from them. Nothing is clipped unasked.
 */
internal class ChatSiteSearch(
    private val vaultPath: String,
    sources: ChatSources,
    private val host: ChatHost,
    private val onAnswered: suspend () -> Unit
) {
    private val answerFromPages = sources.answerFromPages
    private val sourceSites = sources.sourceSites

    /** What [intent] asks of the search; the work it starts, which Stop then ends, when it writes an answer. */
    fun on(intent: ChatIntent): Job? = when (intent) {
        is ChatIntent.OnSiteSearch -> {
            host.launch { search(id = intent.id) }
            null
        }

        is ChatIntent.OnSitePageToggle -> {
            toggle(intent = intent)
            null
        }

        is ChatIntent.OnSiteClip -> host.launch { clipAndAnswer(id = intent.id) }

        else -> null
    }

    /** Offers the sites to turn [id] when it ended on not found. */
    suspend fun offer(id: Long) {
        val sites = sourceSites ?: return
        if (entryOf(id = id)?.outcome != TurnOutcome.NOT_FOUND) return
        val found = sites.sitesOf(vaultPath = vaultPath)
        host.update(id) { it.copy(sites = found) }
    }

    private suspend fun search(id: Long) {
        val sites = sourceSites ?: return

        val entry = entryOf(id = id)?.takeIf {
            !it.isLookingInSites
        } ?: return

        host.update(id) { it.copy(isLookingInSites = true) }

        val pages = try {
            sites.pagesFor(
                vaultPath = vaultPath,
                question = entry.question,
                onProgress = { progress ->
                    host.update(id) { it.copy(progress = progress) }
                }
            )
        } finally {
            host
                .update(id) {
                    it.copy(
                        isLookingInSites = false,
                        progress = null
                    )
                }
        }

        host
            .update(id) { shown ->
                shown.copy(
                    sitePages = pages,
                    pickedPages = pages.map { it.url }.toSet()
                )
            }
    }

    private fun toggle(intent: ChatIntent.OnSitePageToggle) = host
        .update(intent.id) {
            val url = intent.url
            val picked = if (url in it.pickedPages) it.pickedPages - url else it.pickedPages + url
            it.copy(pickedPages = picked)
        }

    /** The ticked pages clipped, and turn [id] answered again from them; not found again when they do not say. */
    private suspend fun clipAndAnswer(id: Long) {
        val sites = sourceSites ?: return
        val entry = entryOf(id = id) ?: return

        val picked = entry
            .sitePages
            .orEmpty()
            .filter {
                it.url in entry.pickedPages
            }

        val isReady = picked.isNotEmpty() && !entry.isAnswering
        if (!isReady) return

        host
            .update(id) {
                it.copy(
                    outcome = null,
                    step = ChatStep.COMPOSING,
                    sitePages = null,
                    sites = emptyList()
                )
            }

        var answer = ""

        try {
            val clipped = sites.clip(
                vaultPath = vaultPath,
                pages = picked,
                onProgress = { progress ->
                    host.update(id) { it.copy(progress = progress) }
                }
            )

            host
                .update(id) {
                    it.copy(
                        progress = null,
                        step = ChatStep.WRITING
                    )
                }

            if (clipped.isNotEmpty()) {
                answerFromPages(
                    question = entry.question,
                    pages = clipped
                ).collect { text ->
                    answer = text
                    host.update(id) { it.copy(answer = text) }
                }
            }
        } finally {
            val isNotFound = AnswerPrompt.isNotFound(text = answer)

            host
                .update(id) {
                    it.copy(
                        step = ChatStep.DONE,
                        progress = null,
                        answer = if (isNotFound) "" else answer,
                        outcome = if (isNotFound) TurnOutcome.NOT_FOUND else TurnOutcome.ANSWERED
                    )
                }
        }

        onAnswered()
    }

    private fun entryOf(id: Long): ChatEntry? = host
        .state
        .value
        .entries
        .firstOrNull {
            it.id == id
        }
}

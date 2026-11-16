package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.core.memory.domain.MemorySection

/**
 * The chat's memory and the sources found on the web, as the chat acts on them (`docs/agent-memory.md`,
 * `docs/agent-web.md`): a line kept or forgotten at once, the click its approval; a source ticked, trusted or not;
 * the ticked ones read by asking the request again with their addresses ([onAsk]).
 */
internal class ChatRemembering(
    private val vaultPath: String,
    private val actionRunner: ActionRunner,
    private val today: () -> String,
    private val host: ChatHost,
    private val onAsk: (String) -> Unit
) {
    fun on(intent: ChatIntent.MemoryIntent) {
        when (intent) {
            is ChatIntent.OnRemember -> write(
                kind = ActionKind.REMEMBER,
                line = intent.line
            )

            is ChatIntent.OnMemoryForget -> write(
                kind = ActionKind.FORGET,
                line = intent.line
            )

            is ChatIntent.OnSourceTrust -> write(
                kind = ActionKind.REMEMBER,
                line = if (intent.isTrusted) "Trusted: ${intent.domain}" else "Not trusted: ${intent.domain}",
                section = MemorySection.SOURCES
            )

            is ChatIntent.OnSourceToggle -> host.update(intent.id) { it.toggled(url = intent.url) }

            is ChatIntent.OnSourcesRead -> read(id = intent.id)
        }
    }

    private fun write(
        kind: ActionKind,
        line: String,
        section: MemorySection = MemorySection.PEOPLE
    ) {
        val step = ActionStep(
            kind = kind,
            notes = listOf(VaultGuide.MEMORY),
            text = line,
            property = section.heading,
            value = "${today()}, chat: you chose"
        )

        host.launch {
            actionRunner.run(
                vaultPath = vaultPath,
                steps = listOf(step),
                notes = host.notes
            )
        }
    }

    private fun read(id: Long) {
        val entry = host
            .state
            .value
            .entries
            .firstOrNull {
                it.id == id
            } ?: return

        val picked = entry.foundSources.filter {
            it.result.url in entry.pickedSources
        }

        if (picked.isEmpty()) return

        onAsk("${entry.question}\n\n${picked.joinToString(separator = "\n") { it.result.url }}")
    }

    private fun ChatEntry.toggled(url: String): ChatEntry {
        val isPicked = url in pickedSources

        return copy(pickedSources = if (isPicked) pickedSources - url else pickedSources + url)
    }
}

package app.notekea.clips.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException

/**
 * What a section's pages offer, in a sentence, for the question that asks whether to clip them (the developer's idea
 * of 2026-11-11): "30 guides to send OpenLLMetry traces to observability platforms, such as Datadog and Grafana".
 * Null without a model or when it fails: the question is asked without it.
 */
class DescribeSectionUseCase(private val llmEngine: LlmEngine) {
    suspend operator fun invoke(
        links: SectionLinks,
        language: String? = null
    ): String? {
        val listed = links.urls.joinToString(separator = "\n") { "- ${links.aboutOf(url = it)}" }
        val written = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.SYSTEM,
                            text = INSTRUCTIONS + (language?.let { " Write it in $it." } ?: "")
                        ),
                        ChatMessage(
                            role = ChatRole.USER,
                            text = "Pages under ${links.prefix}:\n$listed"
                        )
                    ),
                    options = GenerationOptions(
                        maxTokens = MAX_TOKENS,
                        temperature = 0f
                    )
                )
                .collect { written.append(it) }
        } catch (_: LlmException) {
            return null
        }

        return written
            .toString()
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    private companion object {
        const val MAX_TOKENS = 90
        const val INSTRUCTIONS =
            "Say in one sentence of at most 30 words what this set of web pages offers a reader, naming two or three " +
                "of them. Only what their names and descriptions say. Write only the sentence."
    }
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** An EN-PT Lisbon translator. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindTranslatorCases {
    const val VAULT = "Blind Translator"

    val all: List<AgentWideCase> =
        BlindTranslatorAnswers.all + BlindTranslatorChanges.all + BlindTranslatorAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A history PhD student in Edinburgh writing on 19th-century Leith–Baltic trade.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindHistoryThesisCases {
    const val VAULT = "Blind History Thesis"

    val all: List<AgentWideCase> =
        BlindHistoryThesisAnswers.all + BlindHistoryThesisChanges.all + BlindHistoryThesisAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A careful Melbourne investor's journal of index funds and savings.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindInvestorJournalCases {
    const val VAULT = "Blind Investor Journal"

    val all: List<AgentWideCase> = BlindInvestorJournalAnswers.all +
        BlindInvestorJournalChanges.all +
        BlindInvestorJournalAsks.all
}

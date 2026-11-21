package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * Host of a weekly Toronto interview podcast.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindPodcastCases {
    const val VAULT = "Blind Podcast"

    val all: List<AgentWideCase> =
        BlindPodcastAnswers.all + BlindPodcastChanges.all + BlindPodcastAsks.all
}

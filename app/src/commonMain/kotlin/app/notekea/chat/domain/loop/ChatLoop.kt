package app.notekea.chat.domain.loop

import app.notekea.core.preferences.domain.AppPreferencesStore
import kotlinx.coroutines.flow.first

/**
 * The one loop as agent mode reaches it while it is a preview (`docs/agent-loop.md`, step 5): [runTurn], used when
 * Settings → General turns it on ([isOn]); today's agent otherwise.
 */
class ChatLoop(
    val runTurn: RunTurnUseCase,
    private val preferencesStore: AppPreferencesStore
) {
    suspend fun isOn(): Boolean = preferencesStore
        .observe()
        .first()
        .chatLoop
}

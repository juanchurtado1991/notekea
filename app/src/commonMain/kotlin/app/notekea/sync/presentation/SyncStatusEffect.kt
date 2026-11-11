package app.notekea.sync.presentation

sealed interface SyncStatusEffect {
    /** Opens Settings → Sync. */
    data object OpenSettings : SyncStatusEffect
}

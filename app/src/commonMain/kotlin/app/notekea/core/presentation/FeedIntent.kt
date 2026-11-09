package app.notekea.core.presentation

/**
 * An intent the app sends a ViewModel itself to keep it fed (the vault's notes changed, a card needs its picture, a
 * save before the window closes), not something the person did: the [IntentTrail] leaves it out, so a report's last
 * steps are the person's.
 */
interface FeedIntent

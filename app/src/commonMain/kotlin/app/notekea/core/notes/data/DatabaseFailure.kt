package app.notekea.core.notes.data

/**
 * Whether [exception] is the database refusing or failing. Common code cannot name the driver's exception, and a bug
 * must not pass for one: it reaches `launchGuarded` (`ARCHITECTURE.md` §3).
 */
internal expect fun isDatabaseFailure(exception: Exception): Boolean

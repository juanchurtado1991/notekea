package app.notekea.core.domain

/**
 * Result of an operation that can fail in expected ways. [E] is the feature's sealed error type
 * (see `ARCHITECTURE.md` §3); unexpected exceptions are not modeled here.
 */
sealed interface Outcome<out T, out E> {
    data class Failure<out E>(val error: E) : Outcome<Nothing, E>

    data class Success<out T>(val value: T) : Outcome<T, Nothing>
}

package app.notekea.core.domain

/**
 * Result of an operation that can fail in expected ways. [E] is the feature's sealed error type
 * (see `ARCHITECTURE.md` §3); unexpected exceptions are not modeled here.
 */
sealed class Outcome<out T, out E> {
    data class Failure<out E>(val error: E) : Outcome<Nothing, E>()

    data class Success<out T>(val value: T) : Outcome<T, Nothing>()

    /** The value of a [Success], or `null` for a failure: for callers that treat every failure alike. */
    fun valueOrNull(): T? = (this as? Success)?.value

    /** The error of a [Failure], or `null` for a success. */
    fun errorOrNull(): E? = (this as? Failure)?.error

    /** This outcome with [transform] applied to its value; a failure stays as it is. */
    inline fun <R> map(transform: (T) -> R): Outcome<R, E> = when (this) {
        is Success -> Success(value = transform(value))
        is Failure -> this
    }
}

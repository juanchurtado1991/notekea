package app.notekea.ktlint.rules

/**
 * Parameter categories in their required order (by ordinal): mandatory, nullable, then with a default value.
 */
internal enum class ParameterKind {
    MANDATORY,
    NULLABLE,
    DEFAULT
}

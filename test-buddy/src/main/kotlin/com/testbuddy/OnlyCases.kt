package com.testbuddy

/**
 * The suite with only the cases whose ID starts with one of [prefixes] (`"1.6"`, `"1.6-03"`), for a feature's smoke
 * test; no prefix keeps them all.
 */
public fun Suite.only(prefixes: List<String>): Suite {
    if (prefixes.isEmpty()) return this

    fun isKept(id: String) = prefixes.any { prefix ->
        id == prefix ||
            id.startsWith("$prefix-") ||
            id.startsWith("$prefix.")
    }

    // A selection that names nothing (a typo, "9.99") ran no case and passed: it is refused instead.
    val named = (automated.map { it.id } + manual.map { it.id })

    prefixes.forEach { prefix ->
        val isKnown = named.any { id ->
            id == prefix ||
                id.startsWith("$prefix-") ||
                id.startsWith("$prefix.")
        }

        require(isKnown) { "No case is named $prefix" }
    }

    return withCases(
        automated = automated.filter { isKept(id = it.id) },
        manual = manual.filter { isKept(id = it.id) }
    )
}

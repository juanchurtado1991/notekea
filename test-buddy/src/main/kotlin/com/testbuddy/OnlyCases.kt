package com.testbuddy

/**
 * The suite with only the cases whose ID starts with one of [prefixes] (`"1.6"`, `"1.6-03"`), for a feature's smoke
 * test; no prefix keeps them all.
 */
public fun Suite.only(prefixes: List<String>): Suite {
    if (prefixes.isEmpty()) return this
    fun isKept(id: String) = prefixes.any { prefix ->
        id == prefix || id.startsWith("$prefix-") ||
            id.startsWith("$prefix.")
    }
    return Suite(
        appName = appName,
        version = version,
        automated = automated.filter { isKept(id = it.id) },
        manual = manual.filter { isKept(id = it.id) },
        window = window,
        dataSources = dataSources,
        backUp = backUp,
        content = content
    )
}

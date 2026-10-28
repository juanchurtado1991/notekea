package com.testbuddy

/** A [CaseText] written in place: [english], and its [translations] by language code (`"es" to "…"`). */
public fun plainText(
    english: String,
    vararg translations: Pair<String, String>
): CaseText {
    val byLanguage = translations.toMap()
    return CaseText { language -> byLanguage[language] ?: english }
}

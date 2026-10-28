---
tags: [ghost-serialization, serialization, yaml, writer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/yaml/GhostYamlWriterHelpers.kt
kind: object
---

# GhostYamlWriterHelpers

The stateless kernels behind [[GhostYamlWriter]]: layout, key quoting, string escaping and integer formatting.

## What it is about

The writer class owns the mutable state (depth, counters, flags), while this internal object owns the rules. Splitting them keeps the class short and makes each rule easy to read and test. Most functions are `inline` and take the actual output as a lambda such as `writeByte`, so the same code works for either sink without a call through an interface or a lambda object.

## How it works

- **Layout:** `prepareNameLayout` checks that a name is written inside an object (otherwise `ERR_NAME_OUTSIDE_OBJECT`) and writes a newline and indentation. `prepareValue` writes the `- ` of an array item, or the single space after a key's colon. It returns three flags packed in an `Int` (`PREPARE_JUST_WROTE_DASH`, `PREPARE_PENDING_SPACE`, `PREPARE_INCREMENT_ITEM`), so there is no result object to allocate.
- **Empty collections:** `writeEmptyPlaceholderIfNeeded` writes `{}` or `[]` (preceded by a space when the parent is an object) if nothing was written inside.
- **Keys:** `extractKey` strips the quotes and colon from a compiler-made header; `keyNeedsQuoting` returns true for keys that would be misread, such as those starting with `&`, `*`, `!`, quotes, brackets, `%` or a space, a bare `?`, a trailing space, an embedded newline or an embedded `: `.
- **Strings:** `writeEscaped` handles `\"`, `\\`, `\n`, `\r`, `\t`, `\b`, `\f`, other control characters as `\uXXXX`, ASCII bytes as they are, and keeps surrogate pairs together when it writes UTF-8.
- **Numbers:** `writeLong` writes digits into a scratch array, and handles `0` and `Long.MIN_VALUE` as special cases.

## The code

The doc comment of `keyNeedsQuoting` records that its rules came from fuzzing: a key like `" ?xup"` once re-read wrongly. A fragment of the check:

```kotlin
val isExplicitKeyIndicator = first == TOK.QUESTION_BYTE.toInt() &&
    (length == 1 || key[1].code == TOK.SPACE_INT || key[1].code == TOK.CHAR_TAB_INT)
if (isExplicitKeyIndicator) {
    return true
}
```

The negative-number branch of `writeLong` avoids overflow of `-Long.MIN_VALUE`:

```kotlin
if (isNegative) {
    writeByte(TOK.DASH_INT)
    if (remaining == Long.MIN_VALUE) {
        writeUtf8(WR.STR_MIN_LONG_ABS)
        return
    }
    remaining = -remaining
}
```

Indentation is two spaces per level (`writeIndentation`), and `writeUnicodeHex` emits four lowercase hex digits using the table in [[GhostYamlWriterConstants]].

## Related

- [[GhostYamlWriter]]: the only user of these kernels.
- [[GhostYamlWriterConstants]]: indentation, hex and header constants.
- [[GhostYamlTokens]]: byte values for punctuation.
- [[GhostYamlException]]: thrown when a name is written outside an object.
- [[GhostWriterLongDigits]]: writes the decimal digits of a positive long.
- [[GhostYamlFlatReader]]: the reader whose round-trip behavior drives the quoting rules.

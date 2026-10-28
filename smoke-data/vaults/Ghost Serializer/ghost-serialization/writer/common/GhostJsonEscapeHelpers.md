---
tags: [ghost-serializer, serialization, writer, escaping, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/common/GhostJsonEscapeHelpers.kt
kind: object
---

# GhostJsonEscapeHelpers

Shared inline helpers that decide which characters in a JSON string need escaping and write the escaped form.

## What it is about

Both byte writers and the char writer must escape control characters, quotes and backslashes, and the byte writers must also UTF-8 encode non-ASCII text. This object holds that logic once. Its functions are `inline` and take the actual sink operations as lambdas, so each writer keeps its own monomorphic buffer writes.

## How it works

Safety checks use a bitmask table, `ESCAPE_MASKS`: `isPlainAsciiSafe` is true for ASCII codes with no escape bit, `isSafeUnescapedChar` also accepts everything at or above `ASCII_LIMIT` (the char channel never has to escape those), and `isVerbatimStringChar` additionally excludes lone surrogates.

The byte versions (`writeEscapedBytes`, `writeEscapedIntoByteScratch`) copy plain ASCII into a scratch buffer and flush it whenever they meet a character that needs special treatment. They then emit a known two-byte replacement, a `\uXXXX` escape, a UTF-8 range for normal non-ASCII characters and surrogate pairs, or the UTF-8 replacement character for a lone surrogate. The char versions are simpler: an escaped character is always a backslash plus one char, or `\uXXXX`, and a `getEscapeSecondChar` lambda supplies the letter. `writeUnicodeEscapeBytes` and `writeUnicodeEscapeChars` format the six-character escape using hex tables and shifts.

## The code

```kotlin
inline fun isPlainAsciiSafe(code: Int): Boolean =
    code < ASCII_LIMIT &&
            (ESCAPE_MASKS[code shr BITMASK_SHIFT] shr (code and BITMASK_INDEX_MASK)) and BITMASK_UNIT == 0L

inline fun isSafeUnescapedChar(code: Int): Boolean =
    code >= ASCII_LIMIT ||
            (ESCAPE_MASKS[code shr BITMASK_SHIFT] shr (code and BITMASK_INDEX_MASK)) and BITMASK_UNIT == 0L
```

The `...IntoByteScratch` and `...IntoCharScratch` variants assume the opening quote is already at index 0, and finish by appending the closing quote, or by calling a `writeQuote` lambda when the scratch is full.

## Related

- [[GhostJsonWriter]]: byte writer that uses these helpers
- [[GhostJsonStringWriter]]: char writer that uses the char variants
- [[GhostJsonWriterHelpers]]: sibling kernels that call `isPlainAsciiSafe`
- [[GhostJsonWriterConstants]]: defines `ESCAPE_MASKS` and the replacement tables
- [[GhostJsonTokens]]: defines the hex tables and character constants

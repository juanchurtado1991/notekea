---
tags: [ghost-serialization, serialization, parser, escapes]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonEscapeValidation.kt
kind: functions
---

# GhostJsonEscapeValidation

A one-predicate file that says whether a character after a backslash is one of JSON's simple escape letters.

## What it is about

In a JSON string, a backslash can be followed by `u` (a four-hex-digit unicode escape) or by one of eight simple letters: `"`, `\`, `/`, `b`, `f`, `n`, `r`, `t`. The unicode case needs extra digits, so readers treat it separately. `isSimpleEscapeCode` answers the simple question for the other eight, and is shared by the byte readers and the string reader so they cannot disagree about which escapes are legal.

## How it works

`isSimpleEscapeCode(escaped: Int)` is `internal inline`, so it expands at each call site into a chain of integer comparisons against named constants (`QUOTE_INT`, `BACKSLASH_INT`, `SLASH_INT`, and the byte codes of `b`, `f`, `n`, `r`, `t` from `GhostJsonTokens`). There is no table lookup and no allocation. It returns `true` for any of the eight and `false` for everything else, including `u`; callers handle `u` before or after asking.

A reader typically checks this while skipping or validating a string whose escapes it does not need to decode, for instance when scanning for the end of a quoted value, and raises "Invalid escape sequence" otherwise. Decoding such a value (turning `\n` into a line feed) is done elsewhere, by the slow-path kernel.

## The code

```kotlin
internal inline fun isSimpleEscapeCode(
    escaped: Int
): Boolean = escaped == TOK.QUOTE_INT ||
    escaped == TOK.BACKSLASH_INT ||
    escaped == TOK.SLASH_INT ||
    escaped == TOK.B_BYTE_INT ||
    escaped == TOK.F_BYTE_INT ||
    escaped == TOK.N_BYTE_INT ||
    escaped == TOK.R_BYTE_INT ||
    escaped == TOK.T_BYTE_INT
```

The parameter is an `Int` so the same function serves byte values and, via `.code`, char values.

## Related

- [[GhostJsonTokens]]: defines the character codes compared.
- [[GhostJsonFlatReaderStrings]]: byte-reader string code that calls it.
- [[GhostJsonReader]]: streaming reader that calls it.
- [[GhostJsonStringReaderQuotedStrings]]: char-based reader that calls it.
- [[GhostJsonQuotedStringSlow]]: decodes the escapes this predicate accepts.

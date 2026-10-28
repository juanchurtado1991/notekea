---
tags: [ghost-serialization, serialization, yaml, parser, strings]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlQuotedScalarDecoding.kt
kind: functions
---

# GhostYamlQuotedScalarDecoding

Reads double-quoted (`"..."`) and single-quoted (`'...'`) YAML scalars, with their escape rules and line folding.

## What it is about

Quoted scalars are how YAML writes text that would otherwise be ambiguous (`"123"`, `"a: b"`) or that needs escapes. The two kinds follow different rules: double quotes support backslash escapes, single quotes only the doubled `''`. Both can span lines. This file contains `readDoubleQuotedString` and `readSingleQuotedString`, which are called from `readValue` and from key reading.

## How it works

Both functions first take a **fast path**: they scan to the closing quote, and if no escape and no line break was seen, they decode the bytes between the quotes directly into a string with no scratch memory.

Otherwise they take a **slow path** into a pooled scratch byte buffer (from `acquireScratchBuffer`, released in `finally`) that doubles when it is full:

- In double quotes, a backslash starts an escape handled by `processEscapeSequence`: the usual `\n \t \r \b \f \" \\ \/`, `\0 \a \v \e`, `\N \_ \L \P`, `\xXX`, `\uXXXX` and `\UXXXXXXXX`. A `\` before a line break joins lines. The resulting code point is encoded to UTF-8 by hand in one of four length cases. Surrogate pairs written as two `\u` escapes are combined into one code point; a lone surrogate becomes U+FFFD, because it cannot be written validly in UTF-8.
- In single quotes, `''` becomes one `'`.
- A line break inside either style is folded by `skipQuotedLineBreaks`: one break becomes a space, N breaks become N-1 newlines, leading blanks on following lines are dropped, and trailing blanks before the fold are trimmed. In double quotes, trailing blanks that came from an escape (`trimFloor`) are protected from trimming. A `---` or `...` at the start of a line inside a quoted scalar is an error.

An unterminated quote raises "Unterminated double-quoted string" or the single-quote version.

## The code

The fast-path decision for double quotes:

```kotlin
if (byteVal == TOK.BACKSLASH_BYTE || isLineBreakByte(byte = byteVal)) {
    hasEscape = true
    break
}
// …
if (!hasEscape && scanPos < localLimit) {
    position = scanPos + 1 // consume string and closing quote
    return localRawData.decodeToString(startPosition, scanPos)
}
```

Encoding a two-byte code point:

```kotlin
outBuffer[outPos++] =
    (SC.UTF8_2BYTE_PREFIX or (code shr SC.SHIFT_6_BITS)).toByte()
outBuffer[outPos++] =
    (SC.UTF8_CONT_PREFIX or (code and SC.UTF8_CONT_MASK)).toByte()
```

## Related

- [[GhostYamlValueReading]]: dispatches `"` and `'` here.
- [[GhostYamlKeyReading]]: reads quoted keys through these functions.
- [[GhostYamlScanConstants]]: UTF-8, surrogate and escape-code constants.
- [[GhostYamlTokens]]: quote, backslash and letter bytes.
- [[GhostYamlErrorMessages]]: the quote and escape error texts.
- [[GhostYamlWhitespace]]: `isDocumentMarker` and the byte predicates.
- [[GhostPools]]: the pooled scratch buffers come from this pooling layer.
- [[GhostYamlWriterHelpers]]: the writer-side counterpart that produces these escapes.

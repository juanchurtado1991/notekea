---
tags: [ghost-serializer, serialization, parser, strings, pooling, string-channel]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/strings/GhostJsonStringReaderQuotedStrings.kt
kind: functions
---

# GhostJsonStringReaderQuotedStrings

String value reading for the `String` reader: `readQuotedString`, `skipQuotedString`, and the escape-decoding slow path.

## What it is about

JSON strings are the most frequent token, and on this channel the document is already UTF-16, so decoding is mostly slicing. This file implements the three operations that touch string content: read into a Kotlin `String`, skip, and decode escapes, together with a small string-interning pool.

## How it works

`readQuotedString` uses `findClosingQuote` to look for the closing quote. If there are no escapes, it returns a result immediately. Strings up to the heuristic pool length are hashed over every char (so URLs or dates with a common prefix do not collide), the length is mixed into the bucket key, and the bucket is checked: when the hash matches and `poolContentEquals` confirms, the cached `String` is returned with zero allocation. Otherwise it creates a substring and stores it. Unlike the byte reader, no "7-bit only" restriction is needed, because the data is already UTF-16.

When the quote finder reports an escape, `readQuotedStringSlow` decodes into the reader's reusable `slowPathChars` buffer, growing it by doubling when needed. It handles `\n`, `\r`, `\t`, `\b`, `\f`, `\"`, `\\`, `\/` and `\uXXXX`, validates surrogate pairs (a lone low surrogate or an unmatched high one is an error), and rejects raw control characters.

`parseUnicodeHex` bounds-checks the lookup table, because a UTF-16 unit can exceed 255. The comment records that fuzzing found a raw high code point that used to throw `ArrayIndexOutOfBoundsException` instead of a parse error.

## The code

The pooled fast path:

```kotlin
val poolKey = hash xor (length * SCN.STR_POOL_HASH_MULTIPLIER)
val bucketIndex = poolKey and (SCN.STR_POOL_SIZE - 1)
if (stringPoolHashes[bucketIndex] == poolKey) {
    val cached = stringPool[bucketIndex]
    if (cached != null && poolContentEquals(start = start, length = length, cached = cached)) {
        return cached // zero allocation — reuse existing String object
    }
}
```

The bounds-checked hex digit lookup:

```kotlin
val digitValue0 = if (hexByte0 < lutSize) hexLookupTable[hexByte0] else -1
val digitValue1 = if (hexByte1 < lutSize) hexLookupTable[hexByte1] else -1
```

## Related

- [[GhostJsonStringReader]]: owns `rawChars`, `stringPool` and `slowPathChars`.
- [[GhostJsonStringReaderSubsystem]]: defines `findClosingQuote` on this reader.
- [[GhostHeuristics]]: sets the pooling length limit.
- [[GhostJsonFlatReaderStrings]]: the byte-reader counterpart.
- [[GhostJsonEscapeValidation]]: shared escape-code checks such as `isSimpleEscapeCode`.
- [[GhostJsonTokens]]: the character constants and the hex lookup table.

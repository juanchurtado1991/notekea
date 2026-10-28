---
tags: [ghost-serializer, serialization, parser, strings, pooling]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/extensions/GhostJsonFlatReaderStrings.kt
kind: functions
---

# GhostJsonFlatReaderStrings

String reading for the byte reader: `readQuotedString` with a string pool, `skipQuotedString`, and the helper that decodes `\uXXXX` escapes.

## What it is about

Object keys and string values are the most common thing a JSON reader materializes, so this is one of the most tuned files in the byte channel. It extends [[GhostJsonFlatReader]] with three operations: read a string into a `String`, skip one without decoding, and parse the four hex digits of a unicode escape.

## How it works

`readQuotedString` first runs a SWAR scan (`scanStringSwarNoHash`) over the bytes after the opening quote. The scan returns a packed result with the content length and a "7-bit only" flag, or a sentinel if it meets an escape or control byte. The sentinel sends control to `readQuotedStringSlow`, which decodes escapes through the shared slow-path helper.

On the fast path there are three outcomes. An empty string returns `""`. A value longer than the heuristic pool limit is decoded straight through the source and never pooled. A short value is hashed one word at a time, mapped to a bucket of the reader's `stringPool`, and compared with the cached string using `contentEqualsStringImpl`; on a hit no new `String` is allocated. Only 7-bit values are cached, since a pooled string must equal the bytes exactly. The comment in the code explains why the hash is computed after the scan rather than during it: long values dominate the byte volume and would pay for a hash they never use.

`skipQuotedString` finds the closing quote with a fast scan and, if escapes are present, walks the bytes validating escapes, unicode hex and control characters, throwing specific errors for an unterminated string or escape.

## The code

The pooled lookup:

```kotlin
val poolBucketIndex = poolHash and (SCN.STR_POOL_SIZE - 1)
if (stringPoolHashes[poolBucketIndex] == poolHash) {
    val cachedString = stringPool[poolBucketIndex]
    val canReuseCachedString = only7Bit &&
        cachedString != null &&
        contentEqualsStringImpl(start = start, length = length, targetString = cachedString) { localData[it].toInt() and TOK.BYTE_MASK }
    // …
}
```

Unicode escapes decode through a lookup table, and any negative digit means an invalid hex character:

```kotlin
if ((digitValue0 or digitValue1 or digitValue2 or digitValue3) < 0) {
    throwError(EM.ERR_INVALID_UNICODE_AT + currentPosition)
}
```

## Related

- [[GhostJsonFlatReader]]: the reader that owns the pool arrays.
- [[ByteArrayGhostSource]]: provides `decodeJsonStringRange` and `decodeToString`.
- [[GhostHeuristics]]: defines `maxStringPoolLength`.
- [[GhostJsonQuotedStringSlow]]: the shared escape-decoding slow path.
- [[GhostSourceCommon]]: holds the scan, hash and compare helpers.
- [[GhostJsonStringReaderQuotedStrings]]: the same job for the `String` reader.

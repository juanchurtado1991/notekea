---
tags: [ghost-serializer, serialization, parser, field-select, streaming]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/streaming/GhostJsonReaderSubsystem.kt
kind: functions
---

# GhostJsonReaderSubsystem

The structural operations of the general reader: objects, arrays, separators, null, booleans, strings, collections, field selection, `skipValue` and resilient decoding.

## What it is about

[[GhostJsonReader]] keeps only its state and low-level token handling. Everything a generated serializer calls to walk a document lives here as extension functions. The code is the streaming-aware twin of [[GhostJsonFlatReaderSubsystem]] and the flat reader's own members: every operation behaves the same, but must work on either an in-memory array or a stream, and must keep the JSONPath tracker up to date.

## How it works

Most structure functions (`beginObject`, `endObject`, `beginArray`, `endArray`, `hasNext`, `nextKey`) delegate to shared Core helpers, passing lambdas for the reader state, and then update the path tracker (`pushObject`, `pushKey`, `finishScalarValue` and so on). `nextBoolean`, `nextString`, `nextChar`, `consumeNull` and the `...OrNull` family do the same. `consumeNull` has a streaming branch that validates with `skipAndValidateLiteral`, and a fast array branch that checks the four bytes of `null` directly.

`internalSelect`, the field matcher, mirrors the byte version: try the predicted key first, then a hash dispatch with `verifyKeyMatch`, then `handleSelectNoMatch`. Its streaming branch cannot use a masked 8-byte read or trust `limit`, since a stream's limit is `Int.MAX_VALUE`; it checks the byte after the candidate with `byteOrEof` and compares byte by byte. The array branch uses `ghostReadLong8` and the length masks.

`readList`, `readSet` and `readMap` are inline, enforce `maxCollectionSize`, and add path updates. `decodeResilient` saves the position, token, depth, comma masks and a path-tracker mark, pins the stream so the bytes stay available, and on a `GhostJsonException` restores state, skips the value and returns `null`.

## The code

The streaming-only predicted-key comparison:

```kotlin
if (source.byteOrEof(index = keyEnd) != TOK.QUOTE_INT) {
    false
} else {
    var matchedOffset = 0
    while (matchedOffset < candidateLength &&
        getByte(start + matchedOffset) ==
        (candidate[matchedOffset].toInt() and TOK.BYTE_MASK)
    ) {
        matchedOffset++
    }
    matchedOffset == candidateLength
}
```

Resilient rollback:

```kotlin
val streaming = source as? StreamingGhostSource
streaming?.pin(absoluteIndex = savedPos)
try {
    return block()
} catch (_: GhostJsonException) {
    position = savedPos
    // …
    skipValue()
    return null
} finally {
    streaming?.unpin()
}
```

## Related

- [[GhostJsonReader]]: the reader extended.
- [[StreamingGhostSource]]: pinned during resilient decoding.
- [[JsonReaderOptions]]: the field options for the selector.
- [[GhostSwar]]: masks and reads for the array branch.
- [[GhostJsonPathTracker]]: updated by almost every function.
- [[GhostJsonStructureHelpers]]: the shared Core helpers.
- [[GhostDiscriminatorPeeker]]: used by `peekStringField`.
- [[GhostJsonFlatReaderSubsystem]]: the flat-reader equivalent.

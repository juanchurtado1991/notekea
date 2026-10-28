---
tags: [ghost-serializer, serialization, parser, json, string-channel]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/strings/GhostJsonStringReader.kt
kind: class
---

# GhostJsonStringReader

The JSON reader for in-memory `String` payloads, the default `textChannel` path: it parses UTF-16 characters directly instead of encoding the text to bytes.

## What it is about

Ghost has three JSON readers: the byte reader ([[GhostJsonFlatReader]]), the streaming reader ([[GhostJsonReader]]) and this one. When a caller passes a `String`, encoding it to UTF-8 only to parse it again would waste time, so this reader indexes the text itself. It copies the string into a reusable `CharArray` (`rawChars`) and does all structure, field selection and number work on those code units.

## How it works

The class stores the state the other readers have too: `position`, `limit`, `depth`, the cached `nextTokenByte`, the two comma bit masks, a `pathTracker`, `predictedFieldIndex`, and a string pool. `reset(newData)` rebinds it to a new string, resizing `rawChars` only when it is too small, so a pooled reader allocates nothing per call. A leading U+FEFF byte-order mark is skipped, as the other channels skip a UTF-8 BOM.

Bridges exist for code that needs bytes. `ensureUtf8Bytes` encodes the whole document at most once per `reset`; `sliceUtf8Bytes` copies or encodes only a range; `charPositionToBytePosition` builds a char-to-byte offset table, and `bytePositionToCharPosition` goes the other way. They let a byte-based custom decoder run and then sync the reader's position.

Errors are built lazily: `throwError` reconstructs the JSONPath by re-scanning only when an exception is created, and computes line and column on demand.

## The code

Construction copies the text and skips a BOM:

```kotlin
init {
    val len = rawData.length
    val chars = CharArray(len)
    rawData.copyRangeToCharArray(dest = chars, destOffset = 0, startIndex = 0, endIndex = len)
    rawChars = chars
    documentStart = byteOrderMarkLength(
        data = rawData,
        limit = limit
    )
    position = documentStart
}
```

The lazy UTF-8 cache:

```kotlin
fun ensureUtf8Bytes(): ByteArray {
    var bytes = utf8CacheBytes
    if (bytes == null) {
        bytes = rawData.encodeToByteArray()
        utf8CacheBytes = bytes
    }
    return bytes
}
```

Most other operations are extension functions in sibling files.

## Related

- [[GhostJsonStringReaderSubsystem]]: structure, null, boolean and field selection.
- [[GhostJsonStringReaderNumbers]]: number reading.
- [[GhostJsonStringReaderQuotedStrings]]: string reading and pooling.
- [[GhostJsonStringReaderCapture]]: raw JSON capture.
- [[GhostCharSwar]]: packed-char comparison used by the selector.
- [[GhostJsonPathReconstruction]]: rebuilds the error path.
- [[GhostJsonFlatReader]]: the byte-channel sibling.

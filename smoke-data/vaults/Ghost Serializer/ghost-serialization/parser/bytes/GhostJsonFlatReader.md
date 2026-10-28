---
tags: [ghost-serializer, serialization, parser, json, byte-channel]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/GhostJsonFlatReader.kt
kind: class
---

# GhostJsonFlatReader

The main JSON reader for in-memory byte arrays: it reads a flat `ByteArray` directly, with no interface dispatch on the hot path.

## What it is about

This is the workhorse of Ghost's byte channel. Generated serializers call it to open objects, pick field names, read numbers and strings, and close structures. It is "flat" because it indexes `rawData` itself instead of asking a `GhostSource` for every byte, which is what the streaming reader does. The class is `open` so the proto reader can subclass it, and many of its operations are extension functions in the `extensions` folder.

## How it works

State is a handful of fields: `position`, `limit`, the cached `nextTokenByte`, the current `depth`, and two bit masks (`needsCommaMask`, `commaConsumedMask`) that track, per depth up to a maximum, whether a comma is required or was already eaten. In strict mode those masks make comma syntax exact; in lenient mode only trailing commas are rejected.

Several tricks keep it fast. `peekNextToken` caches the next token and has a shortcut for "at most one space then a token". `skipWhitespace` swallows eight spaces at a time with `ghostReadLong8`, since pretty-printed indentation dominates many documents. `predictedFieldIndex` lets the field selector guess that keys come in declaration order. Nothing is tracked for error paths during parsing: `throwError` rebuilds the JSONPath by re-scanning the input only when an exception is actually built.

`resetSlice` rebinds the reader to a sub-range of a buffer without copying, which is how pooled readers and raw-JSON captures reuse it.

## The code

Opening an object enforces depth and clears the comma bits for the new level:

```kotlin
fun beginObject() {
    if (nextNonWhitespace() != TOK.OPEN_OBJ_INT) throwError(EM.ERR_EXPECTED_BEGIN_OBJ)
    predictedFieldIndex = SCN.FIELD_PREDICTION_START
    depth++

    if (depth > maxDepth) throwError(EM.ERR_DEPTH_EXCEEDED)

    if (depth < SCN.MAX_BITMASK_DEPTH) {
        val bit = SCN.BITMASK_UNIT shl depth
        needsCommaMask = needsCommaMask and bit.inv()
        commaConsumedMask = commaConsumedMask and bit.inv()
    }
}
```

The SWAR whitespace skip inside `skipWhitespace`:

```kotlin
while (cursor + SCN.LONG_BYTES <= byteLimit &&
    ghostReadLong8(data = data, index = cursor) == SCN.SPACE_RUN_LONG
) {
    cursor += SCN.LONG_BYTES
}
```

The numeric methods are `open` and defer to extension functions (`nextInt()` calls `nextIntExtension()`), so [[GhostProtoJsonFlatReader]] can override them. `hasNext`, `nextKey`, `consumeNull`, boolean coercion and `skipValue` also live in this class.

## Related

- [[ByteArrayGhostSource]]: the `source` used for string decoding and scanning.
- [[GhostSwar]]: supplies `ghostReadLong8` for whitespace skipping.
- [[GhostJsonFlatReaderSubsystem]]: the field selector (`selectNameAndConsume`).
- [[GhostJsonFlatReaderNumbers]]: number parsing extensions.
- [[GhostJsonFlatReaderStrings]]: quoted string reading and pooling.
- [[GhostJsonPathTracker]]: error path breadcrumbs.
- [[GhostProtoJsonFlatReader]]: the proto3 JSON subclass.
- [[JsonReaderOptions]]: the perfect-hash options passed to the selector.

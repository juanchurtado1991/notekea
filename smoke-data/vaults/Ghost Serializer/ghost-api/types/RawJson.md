---
tags: [ghost-serializer, api, types, rawjson, zero-copy]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/types/RawJson.kt
kind: class
---

# RawJson

A value class-like holder for a piece of JSON kept exactly as the bytes that arrived on the wire.

## What it is about

Sometimes you do not want to parse a part of a document: an opaque payload, an event body whose type is decided later, a block you forward unchanged. `RawJson` stores that part as UTF-8 bytes without building any tree. It belongs to `ghost-api`, so models can use it as a field type, and it is what the envelope and wrapped-keys features hand back.

## How it works

The class holds three values: a `storage` array, an offset and a length. When the reader captures from a flat byte buffer, `RawJson` points into that same buffer, so capturing is zero-copy. Only when you ask for `bytes` and the slice is not the whole array does it copy an exact-length array. All the typed accessors (`asIntOrNull`, `asStringOrNull`, `kind()`) delegate to [[RawJsonValueScanner]] and work directly on the slice.

Equality is by content, not identity. `contentEquals` compares lengths first, then bytes, with a fast path when both are full arrays. `contentHashCode` uses seed 1 and multiplier 31, the same formula as `Arrays.hashCode`, so it agrees with `String.hashCode` for the same bytes. The trade-off of aliasing is that a small `RawJson` can keep a large input buffer alive.

## The code

```kotlin
class RawJson internal constructor(
    val storage: ByteArray,
    val storageOffset: Int,
    val storageLength: Int
) {
    val bytes: ByteArray
        get() = if (isFullStorageSpan()) {
            storage
        } else {
            storage.copyOfRange(
                fromIndex = storageOffset,
                toIndex = storageOffset + storageLength
            )
        }
```

The constructor is internal; instances come from the companion factories `fromBufferSlice` (wraps a slice, no copy), `fromString` (encodes to UTF-8) and `fromUtf8Bytes`. Accessors return `null` instead of throwing when the payload has another type, for example `asBooleanOrNull()` on a number. `asDisplayString()` unquotes strings and returns wire text for everything else. `toString()` shows `RawJson(` plus the decoded text.

## Related

- [[RawJsonKind]]: the result type of `kind()`.
- [[RawJsonValueScanner]]: implements classification and scalar coercion.
- [[RawJsonSerializer]]: reads and writes `RawJson` fields in models.
- [[GhostJsonFlatReaderCapture]]: captures slices from the flat byte reader.
- [[GhostJsonValueCaptureScan]]: finds the end of a value so a slice can be taken.
- [[GhostJsonWriter]]: writes a `RawJson` back out verbatim.
- [[GhostJsonEnvelope]]: its generated routing returns payloads as `RawJson`.

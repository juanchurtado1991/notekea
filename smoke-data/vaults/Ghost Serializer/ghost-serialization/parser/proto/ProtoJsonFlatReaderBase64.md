---
tags: [ghost-serializer, serialization, parser, proto, base64]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/proto/ProtoJsonFlatReaderBase64.kt
kind: functions
---

# ProtoJsonFlatReaderBase64

Decodes a proto3 `bytes` field, which JSON carries as a base64 string, into a `ByteArray`.

## What it is about

In proto3 JSON, a `bytes` value is a quoted base64 string. `readProtoBytes`, an internal extension on [[GhostProtoJsonFlatReader]], reads that string and returns the decoded bytes, and is the target of the reader's public `nextProtoBytes`.

## How it works

The function first reads the quoted text with `readQuotedString`. It then decodes four characters at a time into up to three output bytes, using a lookup table (`BASE64_LUT`) and small inline helpers (`lookupBase64Code`, `combineFirstByte`, `combineSecondByte`, `combineThirdByte`) that live in the common base64 file. Whitespace characters (code at or below a space) are skipped, so line-wrapped base64 works.

The output goes into a scratch buffer acquired from the reader's pool (`acquireScratchBuffer`) and released in a `finally` block; the function returns `outputBuffer.copyOf(outputPosition)`, which is the only allocation proper. Padding is handled in the middle (`=` codes stop the group) and at the end: a trailing group of one char is invalid, and shorter groups are padded with `=` before the final lookup. Invalid input throws a reader error (`ERR_INVALID_BASE64`) with position and path.

A comment in the source states a deliberate trade-off. The loop duplicates the generic `decodeBase64String` algorithm, byte for byte, even though that breaks DRY: this version decodes straight from the scratch buffer without building an output array first, and unifying the two was measured to cost throughput.

## The code

Setup and scratch buffer handling:

```kotlin
internal fun GhostProtoJsonFlatReader.readProtoBytes(): ByteArray {
    val decodedString = readQuotedString()
    val lut = PC.BASE64_LUT
    val length = decodedString.length

    val outputBuffer = acquireScratchBuffer(minSize = length)
    var outputPosition = 0
    val chunk = IntArray(PC.B64_PAD_MULTIPLIER)
    var chunkIndex = 0
    // …
}
```

The full-group step:

```kotlin
outputBuffer[outputPosition++] = combineFirstByte(value0 = value0, value1 = value1)
if (value2 != PC.B64_PADDING_CODE) {
    outputBuffer[outputPosition++] = combineSecondByte(value1 = value1, value2 = value2)
    if (value3 != PC.B64_PADDING_CODE) {
        outputBuffer[outputPosition++] = combineThirdByte(value2 = value2, value3 = value3)
    }
}
```

## Related

- [[GhostProtoJsonFlatReader]]: exposes this as `nextProtoBytes`.
- [[GhostBase64]]: the shared lookup helpers and the generic decoder.
- [[GhostJsonFlatReaderStrings]]: provides `readQuotedString`.
- [[ProtoBytesValueSerializer]]: a well-known-type serializer for bytes values.
- [[GhostProtoConstants]]: supplies the `PC` constants such as the lookup table and error text.

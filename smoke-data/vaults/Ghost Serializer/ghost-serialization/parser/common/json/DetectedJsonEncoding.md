---
tags: [ghost-serialization, serialization, parser, encoding]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/DetectedJsonEncoding.kt
kind: class
---

# DetectedJsonEncoding

A tiny internal result type that says which text encoding a JSON document uses and how many leading bytes are a byte-order mark.

## What it is about

RFC 8259 allows JSON text in UTF-8, UTF-16 or UTF-32, with or without a byte-order mark (BOM). Ghost's parsers read UTF-8 only, so before parsing it sniffs the first bytes. The sniffing function needs to return two facts at once, the encoding and the BOM length, and this class carries them.

## How it works

It is an `internal class` with two `val` constructor properties:

- `kind: JsonEncodingKind`, one of UTF-8, UTF-16 little or big endian, or UTF-32 little or big endian;
- `bomSize: Int`, the number of bytes to skip at the start (0 when there is no BOM, 2 for a UTF-16 BOM, 3 for the UTF-8 BOM, 4 for a UTF-32 BOM).

It is not a data class and has no behaviour, so it has no `equals` or `copy`. `detectJsonByteEncoding` in [[GhostJsonUtf8Input]] creates one per call. Because detection happens once per document (and the common UTF-8 fast path skips detection altogether), the small allocation is not a concern.

## The code

```kotlin
internal class DetectedJsonEncoding(
    val kind: JsonEncodingKind,
    val bomSize: Int
)
```

Consumers use `bomSize` to compute the payload offset and length (`limit - bomSize`) and `kind` to decide whether to transcode. For example, a UTF-16 LE document with a BOM yields `kind = UTF16_LE` and `bomSize = 2`, and the payload after the BOM is converted to UTF-8 before parsing.

## Related

- [[JsonEncodingKind]]: the enum stored in `kind`.
- [[GhostJsonUtf8Input]]: creates instances in `detectJsonByteEncoding` and consumes them.
- [[GhostJsonTokens]]: provides the BOM sizes and byte constants used to fill it.

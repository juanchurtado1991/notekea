---
tags: [ghost-serialization, serialization, parser, encoding, utf8]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonUtf8Input.kt
kind: functions
---

# GhostJsonUtf8Input

Normalizes any RFC 8259 JSON input (UTF-8, UTF-16 or UTF-32, with or without BOM) to UTF-8 before it reaches the parsers.

## What it is about

Ghost's parsers read UTF-8 only, but the JSON standard also allows UTF-16 and UTF-32. This file is the gate at the byte entry points that makes both true: byte arrays go through `withPreparedUtf8Json`, and streaming Okio sources through `prepareUtf8JsonSource`. Ordinary UTF-8 documents pass through with no cost.

## How it works

`withPreparedUtf8Json(bytes, limit, block)` is an `inline` function that calls `block(data, offset, length)`:

1. For empty input it passes length 0.
2. Fast path: if the first byte is not a BOM lead (0xEF, 0xFE, 0xFF) nor NUL, and the second byte is not NUL, the data must be BOM-less UTF-8, because UTF-16 and UTF-32 put NULs next to ASCII characters. It calls the block with the same array: no allocation, no copy.
3. Otherwise it calls `detectJsonByteEncoding`. UTF-8 with a BOM just offsets by 3. UTF-16 or UTF-32 is converted to a new UTF-8 array.

`detectJsonByteEncoding` checks BOMs longest first (UTF-32 BE, UTF-32 LE, UTF-16 BE, UTF-16 LE, UTF-8), then the BOM-less NUL patterns, then defaults to UTF-8.

`prepareUtf8JsonSource(source)` probes up to four bytes through `source.buffer`. For UTF-8 it skips the BOM and stays streaming; for other encodings it reads everything with `readByteArray` and returns a `Buffer` of the converted bytes, trading streaming for correctness.

`utf16ToUtf8` and `utf32ToUtf8` validate unit alignment, combine surrogate pairs, reject lone surrogates and out-of-range code points with `GhostJsonException(ERR_INVALID_JSON_ENCODING)`, and write UTF-8 through `writeUtf8CodePoint`, trimming the output buffer at the end.

## The code

```kotlin
val isBomlessUtf8 = firstByte != TOK.UTF8_BOM_0 &&
    firstByte != TOK.UTF16_BE_BOM_0 &&
    firstByte != TOK.UTF16_LE_BOM_0 &&
    firstByte != TOK.NUL_BYTE &&
    (limit < TOK.UTF16_UNIT_SIZE || (bytes[1].toInt() and TOK.BYTE_MASK) != TOK.NUL_BYTE)
if (isBomlessUtf8) {
    return block(bytes, 0, limit)
}
```

## Related

- [[DetectedJsonEncoding]]: result type of the detection.
- [[JsonEncodingKind]]: the five encodings.
- [[GhostJsonTokens]]: BOM and NUL constants.
- [[GhostJsonWriterConstants]]: UTF-8 prefix and mask constants used when re-encoding.
- [[GhostJsonException]]: thrown for invalid encodings.
- [[AbstractGhostSerializer]]: a caller of `withPreparedUtf8Json`.
- [[Ghost.jvmAndroid]]: a platform entry point that prepares input through this file.

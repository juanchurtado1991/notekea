---
tags: [ghost-serializer, serialization, writer, utf8, bytes]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/GhostJsonWriterBmpChar.kt
kind: functions
---

# GhostJsonWriterBmpChar

An extension on Okio's `Buffer` that writes a quoted single character, UTF-8 encoded.

## What it is about

When a serializer writes a `Char`, Ghost emits a JSON string with that one character. For the streaming channel the work is done directly on the Okio `Buffer`, without creating a `String` for the character.

## How it works

`writeQuotedBmpCodeUnit(codePoint)` writes a quote, then the code unit as one, two or three UTF-8 bytes depending on its value (`UTF8_1BYTE_LIMIT` and `UTF8_2BYTE_LIMIT` choose the branch), then the closing quote. Characters in the Basic Multilingual Plane never need four bytes. The flat byte writer has its own implementation for the in-memory channel.

## The code

```kotlin
internal fun Buffer.writeQuotedBmpCodeUnit(codePoint: Int) {
    writeByte(TOK.QUOTE_INT)
    when {
        codePoint < WR.UTF8_1BYTE_LIMIT -> writeByte(codePoint)
        codePoint < WR.UTF8_2BYTE_LIMIT -> {
            writeByte(WR.UTF8_2BYTE_PREFIX or (codePoint shr WR.UTF8_SHIFT_6))
            writeByte(WR.UTF8_CONT_PREFIX or (codePoint and WR.UTF8_CONT_MASK))
        }
        else -> { /* three-byte form */ }
    }
    writeByte(TOK.QUOTE_INT)
}
```

## Related

- [[BufferGhostByteSink]]: calls it for `writeQuotedBmpCodeUnit`
- [[GhostByteSink]]: declares the operation this implements for the Okio backend
- [[FlatByteArrayWriter]]: has its own version for the in-memory backend
- [[GhostJsonWriterConstants]]: supplies the UTF-8 limits and prefixes

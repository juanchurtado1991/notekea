---
tags: [ghost-serializer, serialization, parser, strings]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/extensions/GhostJsonFlatReaderChars.kt
kind: functions
---

# GhostJsonFlatReaderChars

One extension function, `nextChar`, which reads a JSON string that must contain exactly one character.

## What it is about

Kotlin's `Char` has no direct JSON type, so Ghost serializes it as a one-character string. This file reads it back for the byte-channel [[GhostJsonFlatReader]], avoiding a `String` allocation in the common case of a single ASCII character.

## How it works

After checking for the opening quote, the function runs the shared string scanner `scanStringImpl` over the bytes. The result is a packed `Long` that encodes the content length and a flag saying whether every byte was 7-bit ASCII. If the length is one and the content is 7-bit, the character is the byte itself: the reader advances past the closing quote and returns `toChar()` directly.

An empty string is reported at once with a specific error. Anything else (escapes, multi-byte UTF-8, longer text, or a failed fast scan) takes the slow path: it moves back to the opening quote, calls `readQuotedString`, and requires the decoded text to be exactly one UTF-16 unit, otherwise it throws an error that includes the actual length. That slow path is what makes a value such as `"\n"` or a Latin-1 letter work.

## The code

The fast path:

```kotlin
if (scanResult != -1L) {
    val length = ((scanResult and SCN.SCAN_LENGTH_MASK) ushr SCN.SCAN_LENGTH_SHIFT).toInt()
    val only7Bit = (scanResult and SCN.SCAN_7BIT_BIT) != 0L
    val end = start + length
    if (length == NUM.SINGLE_CHAR_JSON_LENGTH && only7Bit) {
        position = end + 1
        nextTokenByte = SCN.RESET_TOKEN_BYTE
        return (localData[start].toInt() and TOK.BYTE_MASK).toChar()
    }
    // …
}
```

The fallback:

```kotlin
position = start - 1
val decoded = readQuotedString()
if (decoded.length != NUM.SINGLE_CHAR_JSON_LENGTH) {
    throwError(message = EM.ERR_SINGLE_CHAR_STRING_WRONG_LENGTH + decoded.length)
}
return decoded[0]
```

## Related

- [[GhostJsonFlatReader]]: the reader this extends.
- [[GhostJsonFlatReaderStrings]]: provides `readQuotedString`, used by the slow path.
- [[GhostSourceCommon]]: holds `scanStringImpl`.
- [[GhostJsonErrorMessages]]: the error texts used.
- [[CharSerializer]]: the serializer that relies on single-character reads.

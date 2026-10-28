---
tags: [ghost-serialization, serialization, parser, utf8]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostUtf8PositionBridge.kt
kind: functions
---

# GhostUtf8PositionBridge

Converts positions in a Kotlin `String` (counted in chars) to and from positions in its UTF-8 encoding (counted in bytes), without building the byte array.

## What it is about

Ghost has two parser flavours: byte-oriented readers and `GhostJsonStringReader`, which walks a `String`/`CharArray`. Generated custom decoders are written against byte positions, so when they run on the string reader their positions have to be translated into the reader's own char indices and back. This file does that translation arithmetically.

## How it works

Two public functions, both `@InternalGhostApi`, loop over the string:

- `charToBytePosition(s, charPos)` starts at byte 0 and, for each char index below `charPos`, adds the UTF-8 width of the character at that index, then advances by one char or by two if a surrogate pair starts there.
- `byteToCharPosition(s, targetBytePos)` does the inverse: it keeps adding widths until the running byte count reaches the target, returning the char index it stopped at.

Two internal inline helpers carry the rules. `isSurrogatePairStart` is true when a high surrogate (U+D800 to U+DBFF) is followed by a low surrogate (U+DC00 to U+DFFF). `utf8ByteSizeAt` returns 1 for code units up to 0x7F, 2 up to 0x7FF, 4 for a valid surrogate pair start, and otherwise 3. A surrogate pair thus counts as two chars but four bytes. A lone surrogate is counted as three bytes, which is how it would appear in an encoder that substitutes it.

The cost is linear in the position, with no allocation.

## The code

```kotlin
internal inline fun utf8ByteSizeAt(s: String, index: Int): Int {
    val code = s[index].code
    return when {
        code <= WR.UTF8_1BYTE_MAX -> WR.UTF8_1BYTE_SIZE
        code <= WR.UTF8_2BYTE_MAX -> WR.UTF8_2BYTE_SIZE
        isSurrogatePairStart(s = s, index = index) -> WR.UTF8_4BYTE_SIZE
        else -> WR.UTF8_3BYTE_SIZE
    }
}
```

## Related

- [[GhostJsonStringReader]]: the string reader that calls these functions for the bridge.
- [[GhostJsonWriterConstants]]: defines the UTF-8 size and limit constants used.
- [[GhostJsonTokens]]: defines the surrogate range constants.
- [[GhostJsonUtf8Input]]: handles whole-document encoding conversion, a different layer of UTF-8 handling.

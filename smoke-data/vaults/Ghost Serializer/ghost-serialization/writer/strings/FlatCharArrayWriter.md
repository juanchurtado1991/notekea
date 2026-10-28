---
tags: [ghost-serializer, serialization, writer, buffer, strings]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/strings/FlatCharArrayWriter.kt
kind: class
---

# FlatCharArrayWriter

A growing `CharArray` used as the in-memory output of `GhostJsonStringWriter`.

## What it is about

When the result of an encode is a `String`, Ghost writes characters into a flat array and builds the string from it at the end. `FlatCharArrayWriter` is that array. It is a concrete final class with no interface, which lets the JIT or AOT compiler inline its calls.

## How it works

Like its byte sibling, it keeps `array` and `size`, grows geometrically by about 1.5x through `ensureCapacity` and `grow` (with overflow checks), and has a fast path in `writeChar` that only grows when the array is full. `reset()` zeroes `size` and calls `contractCapacity()`, which replaces the array with a fresh one of the initial size when it has grown past `maxWarmCharWriteBufferCapacity`.

Special methods avoid temporary objects: `appendUtf8` decodes a UTF-8 byte range directly into chars (including the surrogate pair for four-byte sequences), `writeQuotedAscii` copies a verified ASCII string between quotes using the platform's `copyRangeToCharArray`, `writeString` copies a range of a string the same way, and `writeTrue`, `writeFalse`, `writeNull` and `writeDotZero` write literals. `toString()` builds the result with `concatToString`, `toCharArray()` makes a copy.

## The code

```kotlin
fun writeQuotedAscii(text: String, length: Int) {
    ensureCapacity(length + STRING_QUOTE_PAIR_BYTES)
    val backingArray = array
    val writeIndex = size
    backingArray[writeIndex] = CHAR_QUOTE
    text.copyRangeToCharArray(dest = backingArray, destOffset = writeIndex + 1, startIndex = 0, endIndex = length)
    backingArray[writeIndex + 1 + length] = CHAR_QUOTE
    size = writeIndex + length + STRING_QUOTE_PAIR_BYTES
}
```

## Related

- [[GhostJsonStringWriter]]: the writer that targets it
- [[WriterStringPair]]: pools it with the writer
- [[CharArrayCopy]]: supplies `copyRangeToCharArray`
- [[FlatByteArrayWriter]]: byte-array counterpart
- [[GhostHeuristics]]: supplies the retained-capacity limit

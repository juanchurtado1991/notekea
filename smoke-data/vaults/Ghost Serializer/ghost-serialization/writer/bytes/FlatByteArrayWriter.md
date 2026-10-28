---
tags: [ghost-serializer, serialization, writer, buffer, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/FlatByteArrayWriter.kt
kind: class
---

# FlatByteArrayWriter

A growing byte array used as the in-memory output of `GhostJsonWriter`.

## What it is about

For encodes that end up as a `ByteArray` or a `String`, writing through Okio segments is slower than writing straight into one array. `FlatByteArrayWriter` is that array. The source says this trades the ability to drain bytes incrementally for roughly two to three times the small-encode throughput. It is a final class implementing `GhostByteSink`, so calls made inside it are monomorphic and easy for the JIT or AOT compiler to inline.

## How it works

The buffer is `array` with a `size` marking the live part. `ensureCapacity` checks whether `size + extra` fits (and guards against `Int` overflow); when it does not, `grow` multiplies the capacity by about 1.5 (`capacity + capacity >> CAPACITY_GROWTH_SHIFT`) until it is large enough, capped at `Int.MAX_VALUE`, and copies the array. Overflow raises an `IllegalStateException`.

The write functions are tuned. `writeByte` has an in-bounds fast path and a separate growth path. `write2Bytes` does one bounds check for two bytes. `writeQuotedAscii` copies a verified plain ASCII string between quotes with a loop unrolled four times. `writeUtf8` encodes directly into the array without a temporary `ByteArray`: it checks four characters at a time for ASCII, then handles one-, two-, three- and four-byte sequences, and writes the replacement character for a lone surrogate. `reset()` sets `size` to 0 and only drops the array if it exceeds `GhostHeuristics.maxWarmWriteBufferCapacity`, so big one-off encodes do not keep memory forever. `toByteArray()` copies the live bytes and `toStringUtf8()` decodes them.

## The code

```kotlin
override fun writeByte(byteAsInt: Int) {
    val currentSize = size
    val backingArray = array
    if (currentSize < backingArray.size) {
        backingArray[currentSize] = byteAsInt.toByte()
        size = currentSize + 1
    } else {
        growAndWrite(byteAsInt)
    }
}
```

```kotlin
fun reset() {
    size = 0
    if (array.size > GhostHeuristics.maxWarmWriteBufferCapacity) {
        array = ByteArray(initialCapacity)
    }
}
```

`writeTrue`, `writeFalse` and `writeNull` write the literal letters directly after one capacity check.

## Related

- [[GhostByteSink]]: the interface it implements
- [[GhostJsonWriter]]: the writer that targets it
- [[WriterSinkPair]]: pools it together with the writer
- [[GhostHeuristics]]: supplies the retained-capacity limit
- [[FlatCharArrayWriter]]: char-array counterpart
- [[GhostJsonWriterConstants]]: holds capacity and UTF-8 constants

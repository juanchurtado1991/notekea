---
tags: [ghost-serializer, serialization, writer, json, strings]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/strings/GhostJsonStringWriter.kt
kind: class
---

# GhostJsonStringWriter

The char-channel JSON writer: it builds JSON text in a `CharArray` and is used by `Ghost.encodeToString`.

## What it is about

This is the string twin of `GhostJsonWriter`. Generated serializers get it through the `serialize(writer: GhostJsonStringWriter, ...)` overload and use the same method names, so the generated code is almost identical. It writes into a `FlatCharArrayWriter` and has no streaming backend. Structure and integer logic is shared with the byte writer through `GhostJsonWriterHelpers`; the source reports an improvement of the String encode benchmark from about 243 to 220 microseconds after that sharing.

## How it works

State is the same as the byte writer: `needsComma`, `depth`, and a scratch `CharArray` that is created on first use. The structural methods use the shared kernels with `buffer.writeChar` as the byte write. Names are written with escaping, or as an ASCII `ByteString` for generated code. `writeField(header, value)` has `ByteString` and `String` header overloads for each of the main types.

Numbers: longs are written by `GhostWriterLongDigits.writeDigitsChars`. Doubles and floats cannot use the shared kernels, which are typed to a `ByteArray`. So `writeDoubleValueRaw` borrows a pooled byte scratch, calls `GhostDoubleFormatter`, widens the ASCII bytes to chars (`widenAsciiBytesToChars`) and returns the scratch in a `finally`. Fallbacks write `toString()`.

Strings: short plain ASCII goes straight to `writeQuotedAscii`. Everything else is escaped by `GhostJsonEscapeHelpers.writeEscapedChars`, using `getEscapeSecondChar` to map the control characters to `\n`, `\t` and so on. Non-ASCII characters are written as they are.

## The code

```kotlin
@InternalGhostApi
fun writeDoubleValueRaw(number: Double) {
    // integral values: write the long, then ".0"
    val byteScratch = acquireScratchBuffer(minSize = WRITER_SCRATCH_SIZE)
    try {
        val bytesWrittenLength = GhostDoubleFormatter.writeDoubleDirect(
            value = number,
            scratch = byteScratch,
            offset = 0,
        )
        // …fallback to toString(), or widenAsciiBytesToChars(...)
    } finally {
        releaseScratchBuffer(buffer = byteScratch)
    }
}
```

`rawValue(bytes)` first decodes the UTF-8 bytes into the char buffer with `appendUtf8`.

## Related

- [[FlatCharArrayWriter]]: the buffer it writes into
- [[WriterStringPair]]: pools it with the buffer
- [[GhostJsonWriter]]: byte-channel counterpart
- [[GhostJsonWriterHelpers]]: shared structural and integer kernels
- [[GhostJsonEscapeHelpers]]: string escaping
- [[GhostDoubleFormatter]]: number formatting
- [[GhostWriterLongDigits]]: digit emission for integers
- [[GhostPools]]: source of the byte scratch used for doubles

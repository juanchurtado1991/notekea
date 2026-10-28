---
tags: [ghost-serializer, serialization, writer, json, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/GhostJsonWriter.kt
kind: class
---

# GhostJsonWriter

The main low-allocation JSON writer: it turns Kotlin values into JSON bytes, either streaming to Okio or into an in-memory buffer.

## What it is about

Generated serializers call this class to write objects, arrays, field names and values. It is the byte channel of Ghost's writing side (the char channel is `GhostJsonStringWriter`). It is built over a `GhostByteSink`, so the same code serves streaming `BufferedSink` output and the pooled `FlatByteArrayWriter` used for `encodeToBytes`.

## How it works

**Monomorphic dispatch.** A single writer serving two backends would make each sink call bimorphic. The class keeps `flatSink`, which is the sink cast to `FlatByteArrayWriter` or `null`, and every byte goes through an `inline emit*` helper that calls the flat writer directly when it exists. The source notes that this measured about 3% faster on a Twitter encode benchmark.

**State.** `needsComma` and `depth` track separators and nesting; `beginArray` and `beginObject` fail with an error once the maximum depth is reached. `scratch` is a lazily acquired buffer from the pool: `release()` returns it, while `reset()` (used by pooled pairs) keeps it warm.

**Values.** Strings take a fast path for short plain ASCII (`emitQuotedAscii`); otherwise the first character that needs attention is found and the slow path escapes into scratch. Numbers use the shared kernels: integers via lookup tables, doubles via `GhostDoubleFormatter` with a fallback. `writeField(header, value)` writes a comma, a pre-encoded `ByteString` header (`"name":`) and the value in one call; generated code uses it to skip runtime key escaping.

## The code

```kotlin
@PublishedApi
internal val flatSink: FlatByteArrayWriter? = sink as? FlatByteArrayWriter

@PublishedApi
internal inline fun emitByte(byteAsInt: Int) {
    val flat = flatSink
    if (flat != null) flat.writeByte(byteAsInt = byteAsInt) else sink.writeByte(byteAsInt = byteAsInt)
}
```

```kotlin
fun writeField(header: ByteString, value: Int): GhostJsonWriter {
    appendSeparator()
    emit(header)
    writeIntValueRaw(value = value)
    needsComma = true
    return this
}
```

- `rawValue(bytes)` and `rawValue(raw: RawJson)` copy pre-encoded JSON in without re-escaping.
- `value(char)` uses `emitQuotedBmpCodeUnit` for plain characters.
- Non-finite doubles throw a `GhostJsonException` with the non-finite message.

## Related

- [[GhostByteSink]]: the backend interface
- [[FlatByteArrayWriter]]: the in-memory backend
- [[BufferGhostByteSink]]: the Okio backend
- [[GhostJsonWriterHelpers]]: shared kernels it calls
- [[GhostJsonEscapeHelpers]]: escaping logic for strings
- [[GhostDoubleFormatter]]: number formatting
- [[WriterSinkPair]]: pooled pairing of this writer and a flat buffer
- [[GhostJsonStringWriter]]: char-channel counterpart

---
tags: [ghost-serializer, serialization, parser, source]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/ByteArrayGhostSource.kt
kind: class
---

# ByteArrayGhostSource

A `GhostSource` backed by an in-memory `ByteArray`, with the scanning logic shared by every platform.

## What it is about

Ghost readers do not touch bytes directly through one concrete type; they go through the `GhostSource` abstraction, which answers questions such as "where is the closing quote?" or "does this range equal that string?". This class is the in-memory implementation. The JVM and Android source sets subclass it to override string decoding with a faster ASCII path, while other targets use it as it is.

## How it works

The class is open and its `data` is a `var`, so a reader can rebind the buffer for a new payload without allocating a new source (the flat reader's `resetSlice` relies on this). Every scanning method follows the same pattern: copy `data` into a local, then call a shared `...Impl` function from the common source helpers, passing a tiny lambda that reads one byte as an unsigned int (`and BYTE_MASK`). Because the `Impl` functions are inline, the lambda is expanded in place, so the loop-unrolled scanners run over the array with no virtual call per byte.

Only `contentEquals` takes a different route: it asks Okio's `ByteString.rangeEquals` to compare against the array after checking that the range fits. Decoding a range to a `String` simply delegates to Kotlin's `decodeToString`.

## The code

The declaration and the unsigned byte accessor:

```kotlin
open class ByteArrayGhostSource(var data: ByteArray) : AbstractGhostSource() {

    override val size: Int get() = data.size

    override fun get(
        index: Int
    ): Int = data[index].toInt() and BYTE_MASK
```

A representative scanner, which stays the same for the other four (`contentEqualsString`, `findNextNonWhitespace`, `scanString`):

```kotlin
    override fun findClosingQuote(position: Int, limit: Int): Int {
        val localData = data
        return findClosingQuoteImpl(position = position, limit = limit) {
            localData[it].toInt() and BYTE_MASK
        }
    }
```

`rawSourceData` exposes the array itself, so readers can cache it and index it directly on their hot paths.

## Related

- [[AbstractGhostSource]]: the base class this extends.
- [[GhostSource]]: the interface it implements.
- [[GhostSourceCommon]]: holds the shared `...Impl` scanning functions.
- [[GhostSource.jvmAndroid]]: the JVM and Android specialization that overrides string decoding.
- [[GhostJsonFlatReader]]: owns one of these as its `source`.
- [[StreamingGhostSource]]: the streaming sibling used for Okio sources.

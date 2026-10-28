---
tags: [ghost-serialization, serialization, parser, io]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/AbstractGhostSource.kt
kind: class
---

# AbstractGhostSource

A skeletal base class that gives `GhostSource` implementations sensible defaults for three members most of them do not need to customize.

## What it is about

`GhostSource` declares many abstract members, and several of them have an obvious default answer. Rather than copying that boilerplate into every implementation, Ghost provides this abstract class. Both `ByteArrayGhostSource` and `StreamingGhostSource` extend it, and the JVM-specific byte-array subclass overrides one default for speed. It is `@InternalGhostApi`.

## How it works

Three members have defaults:

1. `byteOrEof(index)`: returns the byte at `index` if `index < size`, otherwise the `MATCH_END` sentinel (-1). This relies on `size` and `get` that the subclass provides, so for a stream whose `size` is `Int.MAX_VALUE` the bounds check is always true and the subclass must make `get` itself safe or override this.
2. `decodeJsonStringRange(start, end, isKnown7BitContent)`: ignores the 7-bit hint and delegates to `decodeToString(start, end)`. Implementations that can exploit the hint (the JVM one) override it.
3. `rawSourceData`: returns the shared empty array `GhostJsonTokens.EMPTY_BYTES`. Only sources backed by a real array override it; consumers that see the empty array know no direct array access is available.

Everything else (`get`, `size`, `contentEquals`, `findNextNonWhitespace`, `findClosingQuote`, `scanString`, `contentEqualsString`, `decodeToString`) stays abstract.

## The code

```kotlin
@InternalGhostApi
abstract class AbstractGhostSource : GhostSource {

    override fun byteOrEof(
        index: Int
    ): Int = if (index < size) get(index = index) else GhostJsonScanConstants.MATCH_END

    override fun decodeJsonStringRange(
        start: Int,
        end: Int,
        isKnown7BitContent: Boolean
    ): String = decodeToString(start = start, end = end)

    override val rawSourceData: ByteArray get() = GhostJsonTokens.EMPTY_BYTES
}
```

## Related

- [[GhostSource]]: the interface this class partly implements.
- [[ByteArrayGhostSource]]: extends it and overrides `rawSourceData`.
- [[StreamingGhostSource]]: extends it for Okio streams.
- [[GhostSource.jvmAndroid]]: its subclass overrides `decodeJsonStringRange` through `ByteArrayGhostSource`.
- [[GhostJsonScanConstants]]: supplies `MATCH_END`.
- [[GhostJsonTokens]]: supplies `EMPTY_BYTES`.

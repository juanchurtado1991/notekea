---
tags: [ghost-serialization, serialization, parser, io]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostSource.kt
kind: interface
---

# GhostSource

The interface through which Ghost's byte readers see their input, whether it is an in-memory array or an Okio stream.

## What it is about

A JSON parser needs a handful of primitive operations: read byte N, find the next non-blank byte, find the closing quote of a string, compare a slice with a known key, decode a slice to a `String`. `GhostSource` names those operations so the same reader logic can run over a `ByteArray` or over a streaming `okio.BufferedSource`. It is `@InternalGhostApi`: public for generated code, not for applications.

## How it works

Implementations must return bytes as `Int` in 0..255 so there is no sign extension and the values compare directly against the token constants. Most members are abstract; implementers are told to extend [[AbstractGhostSource]] for defaults.

Notable members:

- `size`: the length, or `Int.MAX_VALUE` for streams where it is unknown.
- `byteOrEof(index)`: like `get` but returns the `MATCH_END` sentinel (-1) past the data. Speculative read-ahead (such as in-order field prediction) uses it because it cannot bounds-check against an unknown size.
- `findNextNonWhitespace` and `findClosingQuote`: fast scans; the latter returns -1 on a backslash or control character so the parser falls to the slow escape path.
- `scanString`: finds the closing quote while computing a rolling hash, returning hash, length and a 7-bit flag packed into one `Long`, or -1 on an escape.
- `contentEqualsString`: compares bytes to a cached string without allocating, for the string pool.
- `decodeJsonStringRange(start, end, isKnown7BitContent)`: UTF-8 decode, with a hint that the JVM can exploit.

Two top-level functions complete the file: the `expect` factory `createByteArraySource` and `createSourceBridge(source)`, which wraps an Okio `BufferedSource` in a `StreamingGhostSource`.

## The code

```kotlin
@InternalGhostApi
expect fun createByteArraySource(
    data: ByteArray
): GhostSource

@InternalGhostApi
fun createSourceBridge(
    source: BufferedSource
): GhostSource = StreamingGhostSource(okioSource = source)
```

The byte-array factory is `expect` so each platform can choose its implementation; the streaming bridge is common code.

## Related

- [[AbstractGhostSource]]: skeletal base class with default implementations.
- [[ByteArrayGhostSource]]: the in-memory implementation.
- [[StreamingGhostSource]]: the Okio streaming implementation.
- [[GhostSource.jvmAndroid]]: JVM and Android actual returning an ASCII-optimized subclass.
- [[GhostSource.wasmJs]]: Wasm actual.
- [[GhostSource.apple]]: Apple actual.
- [[GhostSourceCommon]]: shared scan kernels the implementations call.
- [[GhostJsonScanConstants]]: defines `MATCH_END` and the scan-result packing.

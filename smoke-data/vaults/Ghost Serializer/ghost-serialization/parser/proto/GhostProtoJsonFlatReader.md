---
tags: [ghost-serializer, serialization, parser, proto]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/proto/GhostProtoJsonFlatReader.kt
kind: class
---

# GhostProtoJsonFlatReader

A byte reader specialized for proto3 JSON, the canonical JSON form of Protocol Buffers messages.

## What it is about

Proto3 JSON differs from plain JSON in small but important ways: 64-bit integers are written as quoted strings, `NaN` and infinities are quoted, bytes are base64, enums may be names or numbers, and `1.0` is a valid integer. Instead of forking the parser, Ghost subclasses [[GhostJsonFlatReader]] and overrides only the number-reading methods. This class is the entry point; the real logic is in the extension files beside it.

## How it works

The constructor passes `rawData`, `maxDepth` and `maxCollectionSize` to the base class. The base class declares `nextInt`, `nextLong`, `nextDouble`, `nextFloat` and `nextProtoUInt64` as `open`, so this subclass overrides them to delegate to proto-aware functions: `nextProtoInt32`, `nextProtoInt64`, `nextProtoDouble`, `nextProtoFloat` and `readProtoUInt64`. Generated code that targets proto messages therefore calls the normal reader API and gets proto semantics automatically.

It also adds methods that exist only for proto: `nextProtoBytes` (base64 to bytes), `nextProtoEnum(options)` (name or number) and `nextProtoUInt32`, which returns a `Long` so the full unsigned 32-bit range fits. Everything else, such as objects, strings and collections, is inherited unchanged.

## The code

```kotlin
class GhostProtoJsonFlatReader(
    rawData: ByteArray,
    // …
) : GhostJsonFlatReader(
    rawData = rawData,
    // …
) {

    override fun nextDouble(): Double = nextProtoDouble()
    override fun nextFloat(): Float = nextProtoFloat()
    // …
}
```

## Related

- [[GhostJsonFlatReader]]: the open base class.
- [[ProtoJsonFlatReaderNumbers]]: floating-point, enum and uint32 reading.
- [[ProtoJsonFlatReaderIntegers]]: exact integer reading with quoted and fractional forms.
- [[ProtoJsonFlatReaderBase64]]: the `bytes` field decoder.
- [[GhostHeuristics]]: provides the default collection size.
- [[GhostProtoPools]]: pools instances of this reader for reuse.
- [[GhostProtoContentConverter]]: the Ktor converter that decodes proto JSON with it.

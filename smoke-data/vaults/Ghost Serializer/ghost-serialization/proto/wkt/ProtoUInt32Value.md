---
tags: [ghost-serializer, serialization, proto, wkt, value-class]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoUInt32Value.kt
kind: class
---

# ProtoUInt32Value

A Kotlin value class for the protobuf `google.protobuf.UInt32Value` wrapper message, holding the value in a `Long`.

## What it is about

Proto3 wrapper messages give scalars presence semantics, and `UInt32Value` wraps an unsigned 32-bit integer. On the wire it is a plain JSON number. Kotlin's `UInt` would work, but Ghost stores the value as a `Long` so the full range 0 to 4294967295 fits without sign tricks and so the reader can report out-of-range input with an ordinary comparison.

## How it works

The type is a `@JvmInline value class`, so there is no extra allocation on the JVM. It deliberately contains no validation: [[ProtoUInt32ValueSerializer]] rejects negative numbers and values above `PROTO_UINT32_MAX` (4294967295) with the error messages from [[GhostProtoConstants]] and [[GhostJsonErrorMessages]]. A caller that constructs the class by hand is therefore responsible for staying in range.

[[GhostSerializerLookup]] maps this class to its serializer in the table of built-in well-known types.

In practice the generated or hand-written code that builds this wrapper from a Kotlin `UInt` can use `uint.toLong()`, and read it back with `value.toUInt()`. Because the class is a value class, these conversions are the only cost; no wrapper object is created on the JVM when the compiler can keep the `Long` unboxed. The serializer, not this class, is what enforces the unsigned range at decode time.

## The code

The declaration is two lines:

```kotlin
@JvmInline
value class ProtoUInt32Value(val value: Long)
```

The `Long` property is the notable design decision; compare it with [[ProtoUInt64Value]], which needs `ULong` because 64 unsigned bits do not fit in any signed type.

## Related

- [[ProtoUInt32ValueSerializer]]: reads, validates and writes this type
- [[ProtoUInt64Value]]: the 64-bit sibling, stored as `ULong`
- [[GhostSerializerLookup]]: maps this class to its built-in serializer
- [[GhostProtoConstants]]: holds the maximum value and the overflow message

---
tags: [ghost-serializer, serialization, proto, wkt, value-class]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoDoubleValue.kt
kind: class
---

# ProtoDoubleValue

A Kotlin value class for the protobuf `google.protobuf.DoubleValue` wrapper message, holding a single `Double`.

## What it is about

Proto3 has no nullable scalars, so the well-known wrapper types such as `DoubleValue` exist to give a scalar field "presence": a missing wrapper means unset, a present one carries a value. `ProtoDoubleValue` is Ghost's Kotlin model of that message, and on the wire it is represented as a JSON number. The twist is non-finite values: NaN and the infinities cannot be JSON numbers, so the serializer writes them as strings.

## How it works

The type is a `@JvmInline value class`, so on the JVM it is erased to the underlying `Double` wherever the compiler can, and the wrapper costs no extra object. It contains no logic: all parsing and printing lives in [[ProtoDoubleValueSerializer]], which [[GhostSerializerLookup]] returns when code asks for a serializer of this class. The non-finite handling comes from [[GhostProtoFloatingValues]], used by the serializer.

The file opts in to `InternalGhostApi` at file level because the surrounding proto machinery is internal, but the class itself is public so applications can use it in their models.

## The code

The whole declaration is two lines:

```kotlin
@JvmInline
value class ProtoDoubleValue(val value: Double)
```

The single property `value` is the only state. Equality, hashing and `toString` come from the value-class rules, so `ProtoDoubleValue(value = x)` compares equal when the contents do.

## Related

- [[ProtoDoubleValueSerializer]]: reads and writes this type as proto3 JSON
- [[GhostSerializerLookup]]: registers the serializer for this class among the built-in well-known types
- [[GhostProtoConstants]]: defines the `google.protobuf.DoubleValue` type name used by the serializer
- [[GhostProto]]: the facade through which messages containing this type are decoded

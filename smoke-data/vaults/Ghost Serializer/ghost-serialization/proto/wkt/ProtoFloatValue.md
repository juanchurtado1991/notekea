---
tags: [ghost-serializer, serialization, proto, wkt, value-class]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoFloatValue.kt
kind: class
---

# ProtoFloatValue

A Kotlin value class for the protobuf `google.protobuf.FloatValue` wrapper message, holding a single `Float`.

## What it is about

Proto3 has no nullable scalars, so the well-known wrapper types such as `FloatValue` exist to give a scalar field "presence": a missing wrapper means unset, a present one carries a value. `ProtoFloatValue` is Ghost's Kotlin model of that message, and on the wire it is represented as a JSON number. Using a real `Float` keeps single-precision semantics, so a value is formatted as a float and not widened to a double with extra digits.

## How it works

The type is a `@JvmInline value class`, so on the JVM it is erased to the underlying `Float` wherever the compiler can, and the wrapper costs no extra object. It contains no logic: all parsing and printing lives in [[ProtoFloatValueSerializer]], which [[GhostSerializerLookup]] returns when code asks for a serializer of this class. As with doubles, NaN and the infinities are written as strings by helpers in [[GhostProtoFloatingValues]].

The file opts in to `InternalGhostApi` at file level because the surrounding proto machinery is internal, but the class itself is public so applications can use it in their models.

## The code

The whole declaration is two lines:

```kotlin
@JvmInline
value class ProtoFloatValue(val value: Float)
```

The single property `value` is the only state. Equality, hashing and `toString` come from the value-class rules, so `ProtoFloatValue(value = x)` compares equal when the contents do.

## Related

- [[ProtoFloatValueSerializer]]: reads and writes this type as proto3 JSON
- [[GhostSerializerLookup]]: registers the serializer for this class among the built-in well-known types
- [[GhostProtoConstants]]: defines the `google.protobuf.FloatValue` type name used by the serializer
- [[GhostProto]]: the facade through which messages containing this type are decoded

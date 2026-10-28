---
tags: [ghost-serializer, serialization, proto, wkt, value-class]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoBoolValue.kt
kind: class
---

# ProtoBoolValue

A Kotlin value class for the protobuf `google.protobuf.BoolValue` wrapper message, holding a single `Boolean`.

## What it is about

Proto3 has no nullable scalars, so the well-known wrapper types such as `BoolValue` exist to give a scalar field "presence": a missing wrapper means unset, a present one carries a value. `ProtoBoolValue` is Ghost's Kotlin model of that message, and on the wire it is represented as a plain JSON boolean. It is the simplest of the wrappers, because no escaping or range handling is needed.

## How it works

The type is a `@JvmInline value class`, so on the JVM it is erased to the underlying `Boolean` wherever the compiler can, and the wrapper costs no extra object. It contains no logic: all parsing and printing lives in [[ProtoBoolValueSerializer]], which [[GhostSerializerLookup]] returns when code asks for a serializer of this class. Reading and writing use the boolean methods of the reader and writer directly.

The file opts in to `InternalGhostApi` at file level because the surrounding proto machinery is internal, but the class itself is public so applications can use it in their models.

## The code

The whole declaration is two lines:

```kotlin
@JvmInline
value class ProtoBoolValue(val value: Boolean)
```

The single property `value` is the only state. Equality, hashing and `toString` come from the value-class rules, so `ProtoBoolValue(value = x)` compares equal when the contents do.

## Related

- [[ProtoBoolValueSerializer]]: reads and writes this type as proto3 JSON
- [[GhostSerializerLookup]]: registers the serializer for this class among the built-in well-known types
- [[GhostProtoConstants]]: defines the `google.protobuf.BoolValue` type name used by the serializer
- [[GhostProto]]: the facade through which messages containing this type are decoded

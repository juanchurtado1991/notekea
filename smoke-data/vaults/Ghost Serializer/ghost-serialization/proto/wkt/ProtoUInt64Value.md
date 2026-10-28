---
tags: [ghost-serializer, serialization, proto, wkt, value-class]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoUInt64Value.kt
kind: class
---

# ProtoUInt64Value

A Kotlin value class for the protobuf `google.protobuf.UInt64Value` wrapper message, holding a single `ULong`.

## What it is about

Proto3 has no nullable scalars, so the well-known wrapper types such as `UInt64Value` exist to give a scalar field "presence": a missing wrapper means unset, a present one carries a value. `ProtoUInt64Value` is Ghost's Kotlin model of that message, and on the wire it is represented as a JSON string. The Kotlin type is `ULong` because a signed `Long` only covers half of the unsigned 64-bit range (up to 18446744073709551615).

## How it works

The type is a `@JvmInline value class`, so on the JVM it is erased to the underlying `ULong` wherever the compiler can, and the wrapper costs no extra object. It contains no logic: all parsing and printing lives in [[ProtoUInt64ValueSerializer]], which [[GhostSerializerLookup]] returns when code asks for a serializer of this class. The serializer writes the decimal text of the `ULong` and reads it back with `nextULong`.

The file opts in to `InternalGhostApi` at file level because the surrounding proto machinery is internal, but the class itself is public so applications can use it in their models.

## The code

The whole declaration is two lines:

```kotlin
@JvmInline
value class ProtoUInt64Value(val value: ULong)
```

The single property `value` is the only state. Equality, hashing and `toString` come from the value-class rules, so `ProtoUInt64Value(value = x)` compares equal when the contents do.

## Related

- [[ProtoUInt64ValueSerializer]]: reads and writes this type as proto3 JSON
- [[GhostSerializerLookup]]: registers the serializer for this class among the built-in well-known types
- [[GhostProtoConstants]]: defines the `google.protobuf.UInt64Value` type name used by the serializer
- [[GhostProto]]: the facade through which messages containing this type are decoded

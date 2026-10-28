---
tags: [ghost-serializer, serialization, proto, wkt, value-class]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoInt64Value.kt
kind: class
---

# ProtoInt64Value

A Kotlin value class for the protobuf `google.protobuf.Int64Value` wrapper message, holding a single `Long`.

## What it is about

Proto3 has no nullable scalars, so the well-known wrapper types such as `Int64Value` exist to give a scalar field "presence": a missing wrapper means unset, a present one carries a value. `ProtoInt64Value` is Ghost's Kotlin model of that message, and on the wire it is represented as a JSON string. A 64-bit integer can exceed the 2^53 range where JSON numbers stay exact in many parsers, which is why proto3 JSON quotes it.

## How it works

The type is a `@JvmInline value class`, so on the JVM it is erased to the underlying `Long` wherever the compiler can, and the wrapper costs no extra object. It contains no logic: all parsing and printing lives in [[ProtoInt64ValueSerializer]], which [[GhostSerializerLookup]] returns when code asks for a serializer of this class. Writing goes through `formatLong` from [[ProtoLongDigits]], which handles `Long.MIN_VALUE` correctly.

The file opts in to `InternalGhostApi` at file level because the surrounding proto machinery is internal, but the class itself is public so applications can use it in their models.

## The code

The whole declaration is two lines:

```kotlin
@JvmInline
value class ProtoInt64Value(val value: Long)
```

The single property `value` is the only state. Equality, hashing and `toString` come from the value-class rules, so `ProtoInt64Value(value = x)` compares equal when the contents do.

## Related

- [[ProtoInt64ValueSerializer]]: reads and writes this type as proto3 JSON
- [[GhostSerializerLookup]]: registers the serializer for this class among the built-in well-known types
- [[GhostProtoConstants]]: defines the `google.protobuf.Int64Value` type name used by the serializer
- [[GhostProto]]: the facade through which messages containing this type are decoded

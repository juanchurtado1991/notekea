---
tags: [ghost-serializer, serialization, proto, wkt, value-class]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoInt32Value.kt
kind: class
---

# ProtoInt32Value

A Kotlin value class for the protobuf `google.protobuf.Int32Value` wrapper message, holding a single `Int`.

## What it is about

Proto3 has no nullable scalars, so the well-known wrapper types such as `Int32Value` exist to give a scalar field "presence": a missing wrapper means unset, a present one carries a value. `ProtoInt32Value` is Ghost's Kotlin model of that message, and on the wire it is represented as a JSON number. A 32-bit integer fits exactly in a JSON number, so unlike its 64-bit sibling it is not quoted.

## How it works

The type is a `@JvmInline value class`, so on the JVM it is erased to the underlying `Int` wherever the compiler can, and the wrapper costs no extra object. It contains no logic: all parsing and printing lives in [[ProtoInt32ValueSerializer]], which [[GhostSerializerLookup]] returns when code asks for a serializer of this class. The serializer reads it through the reader's `nextInt`, which in the proto reader applies the proto-specific integer rules.

The file opts in to `InternalGhostApi` at file level because the surrounding proto machinery is internal, but the class itself is public so applications can use it in their models.

## The code

The whole declaration is two lines:

```kotlin
@JvmInline
value class ProtoInt32Value(val value: Int)
```

The single property `value` is the only state. Equality, hashing and `toString` come from the value-class rules, so `ProtoInt32Value(value = x)` compares equal when the contents do.

## Related

- [[ProtoInt32ValueSerializer]]: reads and writes this type as proto3 JSON
- [[GhostSerializerLookup]]: registers the serializer for this class among the built-in well-known types
- [[GhostProtoConstants]]: defines the `google.protobuf.Int32Value` type name used by the serializer
- [[GhostProto]]: the facade through which messages containing this type are decoded

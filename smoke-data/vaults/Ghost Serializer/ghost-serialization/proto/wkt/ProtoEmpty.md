---
tags: [ghost-serializer, serialization, proto, wkt, empty]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoEmpty.kt
kind: object
---

# ProtoEmpty

A singleton object that stands for `google.protobuf.Empty`, the message with no fields.

## What it is about

Protobuf APIs often need a request or response with nothing in it, and `Empty` is the shared type for that. Ghost models it as a Kotlin `object`, since there is no state to store. In JSON it is `{}`.

## How it works

Because it is an `object` there is exactly one value, so equality is identity and no allocation happens when decoding: [[ProtoEmptySerializer]] always returns this same instance. The serializer reads an object, ignores any keys it finds and writes `{}` back out.

[[GhostSerializerLookup]] includes `ProtoEmpty::class` in the table of built-in well-known types, so the serializer is found automatically.

As an `object`, `ProtoEmpty` has no constructor, no properties and no `copy`. That also means it needs no `equals` override. Using it instead of defining an application-specific empty class keeps API signatures uniform: a service that returns nothing can say so with a well-known type that every protobuf toolchain recognises, and the JSON on the wire is the same `{}` in all of them.

## The code

The whole file is the declaration:

```kotlin
object ProtoEmpty
```

Its value to applications is mainly as a type argument, for example a generic RPC return type where no payload is expected.

## Related

- [[ProtoEmptySerializer]]: reads and writes the empty object
- [[GhostSerializerLookup]]: registers the serializer for this class
- [[GhostProtoConstants]]: defines the `google.protobuf.Empty` type name

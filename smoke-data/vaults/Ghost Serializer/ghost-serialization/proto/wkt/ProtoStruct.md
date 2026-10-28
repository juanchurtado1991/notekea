---
tags: [ghost-serializer, serialization, proto, wkt, struct]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoStruct.kt
kind: typealiases
---

# ProtoStruct

A type alias that names `google.protobuf.Struct` as a map from strings to [[ProtoValue]].

## What it is about

Struct is protobuf's way of carrying an arbitrary JSON object, and its JSON form is simply an object. Rather than create a wrapper class, Ghost aliases it to `Map<String, ProtoValue>`, which is the natural Kotlin shape. [[ProtoStructSerializer]] reads and writes that map.

## How it works

A type alias is only a name: at runtime a `ProtoStruct` is a regular `Map`, and `KClass` information is erased to `Map`. This is why [[GhostSerializerLookup]] has no entry keyed on `ProtoStruct::class` (a comment there says so), while `ProtoValue::class` has one. [[ProtoStructSerializer]] is used when the target type is known statically.

The values are [[ProtoValue]] instances, the sealed hierarchy that mirrors the JSON value types (null, number, string, boolean, nested struct and list).

Because there is no wrapper class, there is no extra allocation per struct and no unwrapping step in user code: a `ProtoStruct` can be passed to anything that accepts a `Map`. The cost is that two different meanings of `Map<String, ProtoValue>` cannot be told apart by type, which is acceptable since the proto mapping for that shape is always the same object form. Declaring the alias in its own file keeps the model files one per type, like the other well-known types in this package.

## The code

The file is the alias and its KDoc:

```kotlin
typealias ProtoStruct = Map<String, ProtoValue>
```

## Related

- [[ProtoValue]]: the value type held in the map
- [[ProtoStructSerializer]]: the serializer that reads and writes the map
- [[GhostSerializerLookup]]: its comment explains why no `ProtoStruct` entry exists

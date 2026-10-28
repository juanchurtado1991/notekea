---
tags: [ghost-serializer, serialization, proto, wkt, struct, sealed]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoValue.kt
kind: class
---

# ProtoValue

A sealed class for `google.protobuf.Value`: a dynamically typed JSON-like value with six variants.

## What it is about

`Value` represents any JSON value inside a `Struct`: null, number, string, boolean, object or array. Ghost models it as a sealed hierarchy, so Kotlin code can handle every case with an exhaustive `when`. [[ProtoValueSerializer]] maps it to and from JSON, and [[ProtoStruct]] is a map of these values.

## How it works

The variants are `Null` (an `object`), `Number` (a `Double`), `Str` (a `String`), `Bool`, `Struct` (a map of string to `ProtoValue`) and `List` (a list of `ProtoValue`). Their order follows the `oneof` field order of the protobuf definition rather than alphabetical order. Nesting is recursive: a `Struct` or `List` contains more `ProtoValue`s.

The `List` case is declared with the fully qualified `kotlin.collections.List` for its payload, because inside the class `List` would otherwise refer to the nested variant. All numbers are doubles, as in JSON, so integers above 2^53 lose precision.

## The code

```kotlin
sealed class ProtoValue {
    object Null : ProtoValue()
    data class Number(val value: Double) : ProtoValue()
    data class Str(val value: String) : ProtoValue()
    data class Bool(val value: Boolean) : ProtoValue()
    data class Struct(val value: Map<String, ProtoValue>) : ProtoValue()
    data class List(val value: kotlin.collections.List<ProtoValue>) : ProtoValue()
}
```

## Related

- [[ProtoValueSerializer]]: reads and writes this hierarchy as JSON
- [[ProtoStruct]]: the type alias for a map of these values
- [[ProtoStructSerializer]]: serializes a whole struct by delegating to the value serializer
- [[GhostSerializerLookup]]: maps this class to its built-in serializer

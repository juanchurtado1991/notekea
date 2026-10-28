---
tags: [ghost-serializer, serialization, proto, wkt, field-mask]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoFieldMask.kt
kind: class
---

# ProtoFieldMask

A data class for `google.protobuf.FieldMask`: a list of field paths, kept in proto's snake_case form.

## What it is about

A FieldMask names which fields of a message an operation should read or update, for example `"user.display_name,photo"`. On the wire in proto3 JSON it is one comma-separated string in camelCase (`"user.displayName,photo"`). This class holds the paths, and [[ProtoFieldMaskCodec]] does the conversion.

## How it works

The `paths` list stores each path in snake_case, as written in `.proto` files. Keeping that canonical form in the model means application code works with the names it sees in its schema, while the case conversion is applied only when writing to or reading from JSON. [[ProtoFieldMaskSerializer]] ties the two together.

The class has no validation; an empty list is a valid mask and is written as an empty string.

Typical uses are partial updates (`update_mask`) and projections (`read_mask`) in Google-style APIs. The class stays a plain `data class` so masks can be compared, copied and used as map keys in tests. Nested fields are expressed as dotted paths inside the single string, and the codec treats a dot as the start of a new segment when it converts case, so nested names such as `user.display_name` survive the round-trip.

## The code

```kotlin
data class ProtoFieldMask(val paths: List<String>)
```

## Related

- [[ProtoFieldMaskCodec]]: converts the paths to and from the camelCase wire text
- [[ProtoFieldMaskSerializer]]: reads and writes this type as a JSON string
- [[GhostSerializerLookup]]: maps this class to its built-in serializer

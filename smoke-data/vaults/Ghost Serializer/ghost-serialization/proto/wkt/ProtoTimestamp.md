---
tags: [ghost-serializer, serialization, proto, wkt, timestamp]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoTimestamp.kt
kind: class
---

# ProtoTimestamp

A data class for the protobuf `google.protobuf.Timestamp`: a point in time as seconds since the Unix epoch plus nanoseconds.

## What it is about

Timestamp is the best-known protobuf well-known type. It is independent of time zone and calendar: `seconds` counts from 1970-01-01T00:00:00Z and `nanos` adds a sub-second part. In proto3 JSON it is written as an RFC 3339 string such as `"2024-05-01T12:30:00.250Z"`. This class is Ghost's model for it, with the string conversion kept in [[ProtoTimestampCodec]].

## How it works

The class is a plain `data class` with a `Long` and an `Int`, so equality, `copy` and `toString` come for free. It performs no validation in its constructor, unlike [[ProtoDuration]], which checks that the two components have matching signs. Range checking happens when text is parsed, in the codec.

When reading or writing JSON, [[ProtoTimestampSerializer]] is returned for this class by [[GhostSerializerLookup]]. The file opts in to `InternalGhostApi` at file level; the class itself is public so applications can embed it in their models.

## The code

The declaration is one line:

```kotlin
data class ProtoTimestamp(val seconds: Long, val nanos: Int)
```

The split into whole seconds and nanoseconds mirrors the protobuf wire format and avoids floating point entirely, which is why the type can represent any instant in 0001 to 9999 exactly.

## Related

- [[ProtoTimestampSerializer]]: reads and writes this type as an RFC 3339 string
- [[ProtoTimestampCodec]]: the parse and format functions behind the serializer
- [[ProtoDuration]]: the sibling type for spans of time, with sign validation
- [[GhostSerializerLookup]]: maps this class to its built-in serializer

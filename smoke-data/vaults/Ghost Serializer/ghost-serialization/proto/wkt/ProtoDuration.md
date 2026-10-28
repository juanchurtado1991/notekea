---
tags: [ghost-serializer, serialization, proto, wkt, duration]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoDuration.kt
kind: class
---

# ProtoDuration

A data class for `google.protobuf.Duration`: a signed span of time as seconds plus nanoseconds, with a sign-consistency check.

## What it is about

A Duration is a fixed-length span of time with no notion of days or months. In proto3 JSON it is a string like `"1.500s"` or `"-0.000000001s"`. This class models it, and [[ProtoDurationCodec]] and [[ProtoDurationSerializer]] convert it to and from that text.

## How it works

The class is a `data class` with `seconds: Long` and `nanos: Int`. Its `init` block enforces a rule from the protobuf specification: seconds and nanos must not have opposite signs (one positive and the other negative). A violation throws `IllegalArgumentException` with a message from [[GhostProtoConstants]]. Either component may be zero, which is why a duration under one second can carry its sign entirely in `nanos`.

The constructor does not check the overall range of plus or minus 315,576,000,000 seconds; that limit is enforced while parsing text.

## The code

```kotlin
data class ProtoDuration(val seconds: Long, val nanos: Int) {
    init {
        val hasMismatchedSign = (seconds > 0 && nanos < 0) || (seconds < 0 && nanos > 0)
        if (hasMismatchedSign) {
            throw IllegalArgumentException(PC.ERR_DURATION_SIGN)
        }
    }
```

The check runs on every construction, including `copy`, so an invalid instance cannot exist.

## Related

- [[ProtoDurationSerializer]]: reads and writes this type as a JSON string
- [[ProtoDurationCodec]]: parses and formats the `"<seconds>s"` text
- [[GhostProtoConstants]]: holds the sign-mismatch error message
- [[ProtoTimestamp]]: the sibling point-in-time type
- [[GhostSerializerLookup]]: maps this class to its built-in serializer

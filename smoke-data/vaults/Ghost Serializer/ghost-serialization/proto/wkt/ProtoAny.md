---
tags: [ghost-serializer, serialization, proto, wkt, any]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoAny.kt
kind: class
---

# ProtoAny

A data class for `google.protobuf.Any`: a type URL plus the raw JSON of the packed message.

## What it is about

`Any` lets a message carry another message of a type that is only named at runtime. Its JSON form is an object with an `@type` key and the packed message's fields. Ghost keeps a `typeUrl` string and the packed payload as raw bytes in `value`, so an `Any` can be forwarded or stored without knowing the concrete type.

## How it works

Storing the `value` bytes verbatim is what keeps round-trips lossless without a type registry. [[ProtoAnyRegistry]] adds the optional layer on top: it can `pack` a typed message into a `ProtoAny` and `unpack` it again once a type URL has been registered. [[ProtoAnySerializer]] captures the raw bytes while parsing and writes them back unchanged.

Because the class holds a `ByteArray`, the generated `equals` and `hashCode` of a data class would compare by reference. The class therefore overrides `equals`, `hashCode` and `toString` so that two instances with the same type URL and equal byte contents are equal. The hash multiplier comes from the scan constants shared with the parser.

## The code

```kotlin
    override fun equals(
        other: Any?
    ): Boolean {
        if (this === other) return true
        if (other !is ProtoAny) return false
        return typeUrl == other.typeUrl && value.contentEquals(other.value)
```

```kotlin
    override fun hashCode(): Int =
        SCN.COLLISION_HASH_MULTIPLIER * typeUrl.hashCode() + value.contentHashCode()

    override fun toString(): String =
        "ProtoAny(typeUrl=$typeUrl, value=${value.decodeToString()})"
```

## Related

- [[ProtoAnySerializer]]: captures and writes the raw payload
- [[ProtoAnyRegistry]]: packs and unpacks typed messages using the type URL
- [[GhostJsonScanConstants]]: supplies the hash multiplier
- [[GhostSerializerLookup]]: maps this class to its built-in serializer
- [[GhostProtoConstants]]: defines the `@type` and `value` key names

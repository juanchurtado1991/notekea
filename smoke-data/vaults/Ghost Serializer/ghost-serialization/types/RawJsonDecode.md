---
tags: [ghost-serializer, serialization, types, raw-json]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/types/RawJsonDecode.kt
kind: object
---

# RawJsonDecode

Typed decoding for `RawJson`, the opaque JSON value, with zero-copy parsing when possible.

## What it is about

A `RawJson` field keeps a piece of JSON unparsed. Later the application may want to turn it into a real object. `RawJsonDecode` does that, and `decodeAs` extension functions give it a short form. It parses directly from the stored bytes, so a slice captured from a larger response buffer is not copied.

## How it works

`decode(raw, serializer)` has two paths. If the `RawJson` covers its whole storage array (`storageOffset == 0` and `storageLength == storage.size`), it calls `Ghost.deserialize(serializer, bytes)`. If it is a slice of a bigger buffer, it takes a pooled flat reader through `ghostInternalUseFlatReader` and calls `resetSlice` to point it at the slice, so no copy is made. The overload with a `KClass` looks up a serializer from a `GhostRegistry` (default `Ghost`) and throws `Ghost.serializerNotFoundMessage` if none is found; accepting a registry lets tests substitute a fake. The reified `decode<T>` and `RawJson.decodeAs<T>()` are inline shortcuts.

## The code

```kotlin
fun <T : Any> decode(raw: RawJson, serializer: GhostSerializer<T>): T {
    if (raw.storageOffset == 0 && raw.storageLength == raw.storage.size) {
        return Ghost.deserialize(serializer = serializer, bytes = raw.storage)
    }
    return ghostInternalUseFlatReader(bytes = raw.storage) { reader ->
        reader.resetSlice(buffer = raw.storage, offset = raw.storageOffset, length = raw.storageLength)
        serializer.deserialize(reader = reader)
    }
}
```

## Related

- [[RawJson]]: the value type being decoded
- [[RawJsonSerializer]]: captures the `RawJson` values decoded here
- [[Ghost]]: supplies the default registry and the byte decode path
- [[GhostRegistry]]: used to look up the serializer by class
- [[GhostSerializer]]: the type that does the parsing
- [[Ghost.jvmAndroid]]: provides `ghostInternalUseFlatReader` on the JVM and Android

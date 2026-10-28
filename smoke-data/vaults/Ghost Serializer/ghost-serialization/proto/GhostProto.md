---
tags: [ghost-serializer, serialization, proto, facade, api]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/GhostProto.kt
kind: object
---

# GhostProto

The public `GhostProto` object: a proto3-flavoured entry point with `deserialize`, `encodeToBytes` and `encodeToString`.

## What it is about

It mirrors the shape of [[Ghost]] but routes decoding through [[GhostProtoJsonFlatReader]], the reader that understands the proto3 JSON mapping (quoted 64-bit integers, base64 bytes, `"NaN"` doubles, and so on). Keeping it as a separate facade gives proto-only behaviour, such as `google.protobuf.Any` handling, its own home instead of cluttering the general-purpose API.

## How it works

All the decoding overloads end up in `ghostProtoInternalUseFlatReader`, which lends a pooled reader, and then ask the registered [[GhostSerializer]] to deserialize from it. The inline `reified` versions resolve the serializer with `Ghost.resolveSerializer<T>()`. A non-inline variant takes a `KClass` and a [[GhostRegistry]] (defaulting to the global [[Ghost]]) for integrations that only know the type at runtime, and throws if no serializer exists.

The `BufferedSource` overload drains the source into a scratch buffer obtained with `acquireScratchBuffer`, decodes it, and releases the buffer in a `finally` block. Encoding needs no proto-specific code: the KSP-generated serializer already writes the proto3 mapping, so the encode functions simply delegate to [[Ghost]].

## The code

The simplest overload shows the pattern:

```kotlin
    inline fun <reified T : Any> deserialize(bytes: ByteArray): T {
        return ghostProtoInternalUseFlatReader(bytes = bytes) { reader ->
            val serializer = Ghost.resolveSerializer<T>()
            serializer.deserialize(reader = reader)
        }
    }
```

The runtime-typed variant resolves through the registry, which is what lets tests pass a fake:

```kotlin
        val serializer = registry.getSerializer(clazz = clazz)
            ?: Ghost.throwError(message = Ghost.serializerNotFoundMessage(type = clazz.simpleName))
        return ghostProtoInternalUseFlatReader(bytes = bytes) { reader ->
            serializer.deserialize(reader = reader)
        }
```

Encoding is a pure delegation:

```kotlin
    inline fun <reified T : Any> encodeToBytes(value: T): ByteArray =
        Ghost.encodeToBytes(value = value)
```

## Related

- [[Ghost]]: the general-purpose facade this one mirrors and delegates encoding to
- [[GhostProtoJsonFlatReader]]: the reader every decode goes through
- [[GhostProtoPools]]: supplies the pooled reader via `ghostProtoInternalUseFlatReader`
- [[GhostRegistry]]: the lookup used by the `KClass` overload
- [[GhostSerializer]]: the type that actually performs decoding and encoding
- [[GhostProtoContentConverter]]: a Ktor converter that calls into the proto decode path
- [[GhostProtoConverterFactory]]: the Retrofit converter factory that decodes proto bodies

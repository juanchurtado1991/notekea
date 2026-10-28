---
tags: [ghost-serializer, ktor, content-negotiation, json]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostContentConverter.kt
kind: class
---

# GhostContentConverter

The Ktor `ContentConverter` that lets Ktor's ContentNegotiation read and write JSON through Ghost.

## What it is about

Ktor delegates body conversion to registered converters. `GhostContentConverter` is Ghost's JSON implementation, used on both the client and the server in `ghost-ktor`. It replaces reflection-based converters: serializers come from Ghost's generated code, and the bytes are parsed by Ghost's flat reader.

## How it works

For **reading**, `deserialize` first resolves a serializer from the `TypeInfo`. If none is found it returns `null`. It then calls `GhostKtorBuffers.readToScratch`, which drains the `ByteReadChannel` into a pooled scratch buffer. An empty body (offset 0) returns `null`, like Ktor's kotlinx converter, so a nullable target gets `null` and a non-nullable one makes Ktor throw `ContentConvertException`. Otherwise it runs `ghostInternalUseFlatReader` over the buffer, optionally lets the user's `configurer` adjust the reader, and calls `serializer.deserialize(reader)`.

For **writing**, `serialize` delegates to the shared `encodeKtorContent` helper, which encodes to bytes and wraps them as `ByteArrayContent`.

The `registry` parameter defaults to the global `Ghost` and is injectable so tests can substitute a fake. It is used only for plain-class lookups; generic types such as `List<T>` or `Map<K, V>` always resolve through `Ghost` itself, the only place with that capability.

## The code

```kotlin
return GhostKtorBuffers.readToScratch(
    content = content,
    charset = charset
) { scratch, offset ->
    val isEmptyBody = offset == 0
    if (isEmptyBody) {
        return null
    }
    ghostInternalUseFlatReader(bytes = scratch, limit = offset) { reader ->
        configurer?.invoke(reader)
        serializer.deserialize(reader)
    }
}
```

The class is opted in to `InternalGhostApi` because it uses the internal reader helper.

## Related

- [[GhostKtorBuffers]]: the pooled read loop used for the body.
- [[GhostKtorSerializers]]: serializer lookup and byte wrapping helpers.
- [[GhostKtorExtensions]]: the `ghost()` function that registers this converter.
- [[GhostRegistry]]: the lookup contract injected as `registry`.
- [[GhostJsonFlatReader]]: the byte reader that parses the body.
- [[GhostProtoContentConverter]]: sibling converter for proto3 JSON.
- [[GhostYamlContentConverter]]: sibling converter for YAML.
- [[InternalGhostApi]]: the opt-in the class needs.

---
tags: [ghost-serializer, ktor, client, extensions]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostClientExtensions.kt
kind: functions
---

# GhostClientExtensions

Extension functions on Ktor's `HttpResponse` that decode a body with Ghost directly, without ContentNegotiation.

## What it is about

Installing ContentNegotiation is the normal way to use Ghost with Ktor, but sometimes you want one explicit call: read this response as type `T`. The three functions `bodyGhost`, `bodyGhostProto` and `bodyGhostYaml` give that. They live in `ghost-ktor` and work in common code, on every Ktor client target.

## How it works

Each function is `suspend inline reified`, so the target class comes from the type argument. It reads the whole body as a `ByteArray` and decodes it:

- `bodyGhost` looks up the serializer with `Ghost.getSerializer(T::class)`. If none exists, it throws an `IllegalArgumentException` whose text comes from `GhostKtorErrorMessages` and tells the developer to annotate the class with `@GhostSerialization`. Then it calls `Ghost.deserialize`.
- `bodyGhostProto` hands the bytes to `GhostProto.deserialize`, which parses with the proto3-lenient reader.
- `bodyGhostYaml` calls `Ghost.decodeFromYaml`.

Because these bypass the negotiation pipeline, they ignore the `Content-Type` header and any registered converters. The trade-off is simplicity and one fewer layer against losing the central configuration. Unlike the converters, they read the full body with `body<ByteArray>()` and do not use the pooled scratch buffer.

## The code

```kotlin
suspend inline fun <reified T : Any> HttpResponse.bodyGhost(): T {
    val bytes = body<ByteArray>()
    val serializer = Ghost.getSerializer(T::class)
        ?: throw IllegalArgumentException("${GhostKtorErrorMessages.SERIALIZER_NOT_FOUND_PREFIX}${T::class.simpleName}${GhostKtorErrorMessages.SERIALIZER_NOT_FOUND_SUFFIX}")
    return Ghost.deserialize(serializer, bytes)
}
```

## Related

- [[Ghost]]: the entry point the JSON and YAML variants call.
- [[GhostProto]]: used by `bodyGhostProto`.
- [[GhostYamlExtensions]]: where the `decodeFromYaml` helper used by `bodyGhostYaml` is defined.
- [[GhostKtorErrorMessages]]: supplies the exception text.
- [[GhostServerExtensions]]: the server-side counterpart, `respondGhost`.

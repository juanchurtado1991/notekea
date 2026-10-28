---
tags: [ghost-serialization, serialization, yaml, pooling]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/GhostYamlPools.kt
kind: expect/actual
---

# GhostYamlPools

The common declaration of the two functions that lend a pooled YAML reader or writer to a block of code.

## What it is about

The YAML channel of Ghost decodes bytes with `GhostYamlFlatReader` and encodes with `GhostYamlWriter`. Both are heavy to create, so each platform keeps a reusable instance. This file is only the `expect` side: it fixes the signatures in `commonMain`, and the platform source sets provide the `actual` code. Both functions are marked `@InternalGhostApi`, so they are meant for Ghost's own entry points and integrations, not for application code.

## How it works

Each function is a "use" function: it receives a lambda, prepares a ready instance, runs the lambda with it and returns the result. The lambda style prevents the instance from being kept by the caller, which matters because the same object is reset and reused on the next call. The JVM and Android actual stores the objects in `ThreadLocal`s ([[GhostYamlPools.jvmAndroid]]); native and wasm use thread-local top-level variables ([[GhostYamlPools.nonJvm]]).

## The code

The reader function takes the bytes to parse:

```kotlin
@InternalGhostApi
expect fun <T> ghostYamlInternalUseFlatReader(
    bytes: ByteArray,
    block: (GhostYamlFlatReader) -> T
): T
```

The writer function passes both the writer and its backing buffer, so the caller can take the encoded bytes out of the buffer when the writer is done:

```kotlin
@InternalGhostApi
expect fun <T> ghostYamlInternalUseFlatWriter(
    block: (GhostYamlWriter, FlatByteArrayWriter) -> T
): T
```

Callers include the extension functions and the Ktor, Retrofit and Spring converters that read or write YAML bodies.

## Related

- [[GhostYamlPools.jvmAndroid]]: actual for JVM and Android.
- [[GhostYamlPools.nonJvm]]: actual for native and wasm.
- [[GhostYamlFlatReader]]: the pooled reader type.
- [[GhostYamlWriter]]: the pooled writer type.
- [[FlatByteArrayWriter]]: the buffer returned with the writer.
- [[GhostYamlExtensions]]: uses the pool for its public YAML functions.
- [[GhostYamlConverterFactory]]: Retrofit converter that decodes through the pool.
- [[InternalGhostApi]]: the opt-in annotation that guards these functions.

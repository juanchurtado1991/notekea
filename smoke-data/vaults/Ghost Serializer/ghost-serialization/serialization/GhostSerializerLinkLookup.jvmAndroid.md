---
tags: [ghost-serializer, serialization, platform, jvm, android, registry]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/GhostSerializerLinkLookup.jvmAndroid.kt
kind: expect/actual
---

# GhostSerializerLinkLookup.jvmAndroid

The JVM and Android actuals of `findLinkedSerializer` and `serializerRegistrationHint`, both of which do nothing.

## What it is about

Associated-object links only exist on Kotlin/Native and Kotlin/Wasm. On the JVM and Android, serializers are found through generated registries, ServiceLoader and reflection, so there is nothing to link. This file satisfies the `expect` declarations with empty answers.

## How it works

`findLinkedSerializer` always returns `null`, so the last step of `Ghost`'s lookup simply reports "not found" if everything else failed. `serializerRegistrationHint` is an empty string: on these platforms the error message needs no extra registration advice, unlike Native and Wasm, where it tells the developer to apply the Gradle plugin or call `Ghost.addRegistry`.

Returning `null` rather than throwing keeps the lookup chain uniform: `Ghost` calls `findLinkedSerializer` on every platform, and only the answer differs. The same reasoning applies to the empty hint string, which `Ghost.serializerNotFoundMessage` concatenates without any special case.

## The code

```kotlin
internal actual fun findLinkedSerializer(
    clazz: KClass<*>
): GhostSerializer<*>? = null

internal actual val serializerRegistrationHint: String = ""
```

## Related

- [[GhostSerializerLinkLookup.native]]: Native version that reads the associated object
- [[GhostSerializerLinkLookup.wasmJs]]: Wasm version that reads the associated object
- [[GhostSerializerLookup]]: calls `findLinkedSerializer` at the end of its registry lookup
- [[Ghost]]: uses `serializerRegistrationHint` in its not-found message

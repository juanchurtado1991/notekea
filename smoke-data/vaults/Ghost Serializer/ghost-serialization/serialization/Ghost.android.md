---
tags: [ghost-serializer, serialization, platform, android, registry]
source: ghost-serialization/src/androidMain/kotlin/com/ghost/serialization/Ghost.android.kt
kind: expect/actual
---

# Ghost.android

The Android actual of `discoverRegistries`, which finds the generated registries without scanning the classpath first.

## What it is about

Ghost resolves serializers through `GhostRegistry` modules that the compiler generates. On the JVM and Android they are discovered at runtime. To keep startup cheap, this version loads the known generated registry classes directly by name, and falls back to `ServiceLoader` only if the caller keeps iterating.

## How it works

The function returns an `Iterable` whose iterator works in two phases. The first time `hasNext` is called it runs `loadFastRegistries`, which tries the class names `Ghost.DEFAULT_REGISTRY_NAME` and `Ghost.ANDROID_REGISTRY_NAME`, reads the static `INSTANCE` field of each, and keeps the ones that cast to `GhostRegistry`. After those are consumed, the iterator lazily creates a `ServiceLoader` iterator and continues with it, so third-party registries are still found. Errors in either phase are swallowed with `runCatching`, because a missing registry class is normal.

`@file:JvmName("Ghost_jvmKt")` makes the Android class compatible with the JVM version of the same function.

## The code

```kotlin
private fun loadFastRegistries(
    out: MutableList<GhostRegistry>
) {
    listOf(
        Ghost.DEFAULT_REGISTRY_NAME,
        Ghost.ANDROID_REGISTRY_NAME
    ).forEach { name ->
        runCatching {
            val clazz = Class.forName(name)
            val field = runCatching { clazz.getField(Ghost.INSTANCE_FIELD) }
                .getOrNull()
                ?: clazz.getDeclaredField(Ghost.INSTANCE_FIELD)
        }
    }
}
```

The fallback to `getDeclaredField` covers a registry whose `INSTANCE` is not public.

## Related

- [[Ghost.jvm]]: plain-JVM version, which tries the default and test registry names
- [[Ghost]]: declares `discoverRegistries` and the registry name constants
- [[GhostRegistry]]: the interface the discovered objects implement
- [[Ghost.jvmAndroid]]: shared JVM and Android actuals for pools and readers

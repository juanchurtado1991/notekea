---
tags: [ghost-serialization, serialization, heuristics, jvm]
source: ghost-serialization/src/jvmMain/kotlin/com/ghost/serialization/parser/common/GhostHeuristics.jvm.kt
kind: expect/actual
---

# GhostHeuristics.jvm

The JVM `actual` of `GhostHeuristics`: higher defaults than other platforms, and every limit can be overridden with a system property.

## What it is about

Servers have more memory than phones or browsers, so the JVM defaults are more generous. Because JVM deployments are also the ones where operators can set command-line flags, this actual lets each value be changed with `-Dghost.<name>=<number>` without recompiling. It implements the `expect object GhostHeuristics` consulted by the readers, writers and pools.

## How it works

Each property is computed once, when the object is first used:

1. Read the system property (for example `ghost.maxCollectionSize`).
2. Parse it with `toIntOrNull()`; a missing or malformed value yields `null`.
3. Fall back with `?:` to a default.

The fallbacks are partly shared and partly JVM specific. `initialCollectionCapacity` and `maxStringPoolLength` use [[GhostHeuristicDefaults]]. The collection cap is 1,000,000, the discriminator peek distance is 2 KiB, the warm byte writer buffer is 8 MiB and the warm char buffer is 2 MiB; the last three are expressed with the `BYTES_PER_KIB` and `BYTES_PER_MIB` helpers from the defaults object. `encodeToStringViaUtf8Bytes` is a fixed `false`.

A bad property value is silently ignored, not reported, since `toIntOrNull` simply returns `null`.

## The code

```kotlin
private const val PROP_MAX_COLLECTION_SIZE = "ghost.maxCollectionSize"
private const val JVM_MAX_COLLECTION_SIZE = 1_000_000

actual object GhostHeuristics {
    actual val maxCollectionSize: Int = System
        .getProperty(PROP_MAX_COLLECTION_SIZE)
        ?.toIntOrNull()
        ?: JVM_MAX_COLLECTION_SIZE
    // …
}
```

All six numeric properties follow this identical pattern, differing only in the property name and fallback. Reading `System` is allowed here because the file lives in the `jvmMain` source set; Android has its own file so it does not inherit this behaviour.

## Related

- [[GhostHeuristics]]: the `expect object` implemented here.
- [[GhostHeuristicDefaults]]: supplies the fallback constants and the KiB/MiB helpers.
- [[GhostHeuristics.android]]: the Android actual with fixed values and a 50,000 collection cap.
- [[GhostHeuristics.native]]: the Native actual using the plain defaults.
- [[FlatByteArrayWriter]]: consumes the warm write buffer cap after `reset`.
- [[FlatCharArrayWriter]]: consumes the warm char buffer cap.

---
tags: [ghost-serialization, serialization, heuristics, android]
source: ghost-serialization/src/androidMain/kotlin/com/ghost/serialization/parser/common/GhostHeuristics.android.kt
kind: expect/actual
---

# GhostHeuristics.android

The Android `actual` of `GhostHeuristics`: the shared defaults, except that collections are capped at 50,000 items.

## What it is about

Android devices are memory constrained, so a hostile or oversized JSON array should be rejected sooner than on a server. This file implements the `expect object GhostHeuristics` for the Android target and changes exactly one number compared with the baseline: `maxCollectionSize`. It is a defence-in-depth limit against denial of service through huge lists and maps.

## How it works

At object initialization every property is assigned once:

- `maxCollectionSize` uses a private constant `ANDROID_MAX_COLLECTION_SIZE = 50_000`, ten times smaller than the 500,000 default and twenty times smaller than the JVM's 1,000,000.
- The other five numeric properties (initial collection capacity, string pool length, discriminator peek distance and the two warm-buffer caps) come from [[GhostHeuristicDefaults]].
- `encodeToStringViaUtf8Bytes` is `false`, so `encodeToString` uses the char-based writer.

Unlike [[GhostHeuristics.jvm]], Android does not read system properties; the values are fixed at compile time.

## The code

```kotlin
/** Android caps collections tighter than the shared default (memory-constrained devices). */
private const val ANDROID_MAX_COLLECTION_SIZE = 50_000

actual object GhostHeuristics {
    // …
    actual val maxCollectionSize: Int = ANDROID_MAX_COLLECTION_SIZE
    // …
    actual val encodeToStringViaUtf8Bytes: Boolean = false
}
```

When a reader finds more items than this cap while filling a list or map, it raises its "collection size exceeds maximum" error rather than allocating further. Applications that legitimately need larger payloads on Android would have to split them.

## Related

- [[GhostHeuristics]]: the `expect object` implemented here.
- [[GhostHeuristicDefaults]]: source of every value that Android does not override.
- [[GhostHeuristics.jvm]]: the desktop/server actual with a much higher cap.
- [[GhostHeuristics.native]]: the plain-defaults actual for Native.
- [[GhostJsonErrorMessages]]: holds the message text used when the collection cap is exceeded.

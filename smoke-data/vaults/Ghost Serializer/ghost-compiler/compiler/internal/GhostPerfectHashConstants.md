---
tags: [ghost-serializer, compiler, constants, hash]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/internal/GhostPerfectHashConstants.kt
kind: object
---

# GhostPerfectHashConstants

The numbers and error messages that bound the perfect-hash search.

## What it is about

[[PerfectHashFinder]] needs a handful of numeric limits (how many table sizes to try, where multipliers start and stop, how many bits to shift) plus a few bit-manipulation helpers. Following the project rule of keeping literals out of logic, they live in this `internal object`. It is the compiler's smallest constants file and only the hash package reads it.

## How it works

The values describe the search space as a nested loop:

- **Table sizes**: `PERFECT_HASH_TABLE_SIZES` is an `IntArray` in ascending order: 128 (also used as the size for an empty name set), then 256, 512, 1024, 2048, 4096 and 8192. The search tries the smallest first, so the generated table is as compact as the data allows.
- **Multipliers**: start at 31, step by 2 (always odd) and stop at 15000.
- **Shifts**: from 0 up to `HASH_SHIFT_LIMIT = 16`.
- **Collision multiplier**: 31, used by the polynomial hash over the bytes after the fourth. A KDoc comment says it must equal `COLLISION_HASH_MULTIPLIER` in the runtime's `GhostJsonScanConstants`; if they drift apart, generated tables stop matching the reader.
- **Bit helpers**: `BYTE_MASK` (0xFF), shifts of 8, 16, 24 and 32 bits, and small numbers 2, 3 and 4 used as byte indexes.

## The code

The sizes:

```kotlin
val PERFECT_HASH_TABLE_SIZES = intArrayOf(
    PERFECT_HASH_EMPTY_TABLE_SIZE,
    PERFECT_HASH_TABLE_SIZE_256,
    PERFECT_HASH_TABLE_SIZE_512,
    PERFECT_HASH_TABLE_SIZE_1024,
    PERFECT_HASH_TABLE_SIZE_2048,
    PERFECT_HASH_TABLE_SIZE_4096,
    PERFECT_HASH_TABLE_SIZE_8192,
)
```

The failure message is built from two halves around the list of names:

```kotlin
const val STR_ERR_PERFECT_HASH_COLLISION_1 =
    "Could not find a collision-free perfect hash configuration for fields: "

const val STR_ERR_PERFECT_HASH_COLLISION_2 =
    ". Please ensure there are no duplicate JSON names or conflicting field names."
```

The error is only reached when even the extended key cannot separate the names, which in practice means a duplicate name that earlier analysis should have rejected.

## Related

- [[PerfectHashFinder]]: the only consumer of these constants.
- [[PerfectHashConfig]]: its fields are chosen from these ranges.
- [[GhostJsonScanConstants]]: the runtime file that must keep the same collision multiplier.
- [[GhostCommonConstants]]: sibling object for the names shared by all stages.

---
tags: [ghost-serializer, compiler, hash, model]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/hash/PerfectHashConfig.kt
kind: class
---

# PerfectHashConfig

A four-field record describing a perfect hash that [[PerfectHashFinder]] found for a set of field names.

## What it is about

When Ghost generates a deserializer, it emits a lookup table keyed by a hash of the field name. The table only works if the generated code and the runtime reader agree on exactly how the hash is computed. This data class carries the parameters of that computation from the search step to the code emitters, so they can print them into the generated file.

## How it works

It is an `internal data class`, so it exists only inside the compiler and is never visible in a library's public API. `PerfectHashFinder.findPerfectHash` builds one and the emitters read its properties, and the values then become constants in the generated serializer. The class has no behavior: no validation, no methods. Its meaning comes from how the runtime uses the numbers: the shifted and masked product of the key, the multiplier and the name length selects the slot.

The default config for an empty name list uses shift 0, multiplier 31, the smallest table size and no extended hash. That makes the generated code well-formed even for a class with no properties.

## The code

The whole file is the declaration:

```kotlin
internal data class PerfectHashConfig(
    val shift: Int,
    val multiplier: Int,
    val tableSize: Int,
    val extendedKeyHash: Boolean
)
```

- `shift`: how many bits the combined value is shifted right before masking. The search tries 0 to 16.
- `multiplier`: the odd integer multiplied into the 32-bit key.
- `tableSize`: the size of the dispatch table, always a power of two, so a mask can replace a modulo. Sizes run from 128 up to 8192.
- `extendedKeyHash`: true when the first four bytes were not enough to tell the names apart, so the runtime must also fold the rest of the bytes into the key.

## Related

- [[PerfectHashFinder]]: the only producer of this record.
- [[GhostPerfectHashConstants]]: limits of the search that bound these fields.
- [[SerializerSetupEmitter]]: reads the config to emit the options table and hash constants.
- [[FlattenOptionsGenerator]]: reads it for flattened groups of fields.

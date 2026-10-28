---
tags: [ghost-serializer, compiler, hash, codegen]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/hash/PerfectHashFinder.kt
kind: object
---

# PerfectHashFinder

Searches, at compile time, for a hash function that places every JSON field name of a class into its own slot of a small table.

## What it is about

A generated deserializer has to turn each field name it meets in the input into a field index. Instead of comparing strings one by one or building a hash map, Ghost precomputes a perfect hash: a formula with no collisions for exactly this set of names. At runtime the reader hashes the bytes of the name and jumps straight to the right slot (`selectNameAndConsume`). All the search cost is paid by the compiler, so decoding stays cheap and allocation-free.

## How it works

The input is the list of names for one class. The finder tries parameters in this order and returns the first that works:

1. An empty list returns a default config with the smallest table, because no dispatch is needed.
2. A first search (`useExtendedKeyHash = false`). The key is the first four bytes of each name (byte 0 in the low bits). Before searching, `detectPrefixLengthCollisions` checks whether two names share the same first four bytes and the same length; if so, this pass already folds the remaining bytes into the key.
3. A second search with `useExtendedKeyHash = true`, tried only if the first one found nothing. Only a config from this pass is returned with `extendedKeyHash = true`, so the runtime also hashes the rest of the name.

For each variant it loops over table sizes (ascending), odd multipliers from 31 up to 15000, and right-shifts from 0 to 16. A candidate is `((key * multiplier + length) shr shift) and (tableSize - 1)`; it is accepted when no two names land in the same slot. If even the extended search fails (for example with duplicate names), it throws an error that lists the names. Empty names are ignored in the table.

## The code

The key for one name packs up to four bytes, then folds in the rest when the extended hash is on:

```kotlin
if (bytes.size >= PH.VAL_FOUR) {
    key = key or ((bytes[PH.VAL_THREE].toInt() and PH.BYTE_MASK) shl PH.BIT_SHIFT_24)
}
if (hasCollisions) {
    var ci = PH.VAL_FOUR
    while (ci < bytes.size) {
        key = key * PH.COLLISION_HASH_MULTIPLIER + (bytes[ci].toInt() and PH.BYTE_MASK)
        ci++
    }
}
```

The probing step, which stops at the first collision:

```kotlin
val key = computeDispatchKey(bytes = bytes, hasCollisions = hasCollisions)
val hash = ((key * multiplier + bytes.size) shr shift) and tableMask
if (dispatch[hash] == -1) {
    dispatch[hash] = index
} else {
    collision = true
    break
}
```

The constant `COLLISION_HASH_MULTIPLIER` must match the one used by the runtime scanner, otherwise the generated table would not agree with the reader.

## Related

- [[PerfectHashConfig]]: the result type that holds shift, multiplier, table size and the extended-key flag.
- [[GhostPerfectHashConstants]]: the sizes, limits and error text used here.
- [[SerializerSetupEmitter]]: asks for a config when it emits the field-name option tables.
- [[FlattenOptionsGenerator]]: also uses it for flattened field groups.
- [[GhostJsonScanConstants]]: runtime side with the matching collision multiplier.
- [[GhostEmitterConstants]]: provides the small `VAL_ZERO` constant used in the search.

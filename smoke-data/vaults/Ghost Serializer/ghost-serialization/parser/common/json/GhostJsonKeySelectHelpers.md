---
tags: [ghost-serialization, serialization, parser, dispatch, kernels]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonKeySelectHelpers.kt
kind: functions
---

# GhostJsonKeySelectHelpers

The shared pieces of Ghost's field dispatch: hashing a key's first bytes, skipping an unknown key, and resolving a key that contains escapes.

## What it is about

When a generated deserializer reads an object, it must map each JSON key to a field index very quickly. Ghost does this with a perfect-hash dispatch table built by the compiler and wrapped in [[JsonReaderOptions]]. The reader channels (flat, streaming, string) all need the same hash and the same "no match" handling, so those parts live here as `internal inline` kernels that take reader-specific lambdas.

## How it works

`computeKeyHashCore(start, length, hasCollisions, getByte)` packs up to the first four bytes of the key into one `Int`, little-endian (first byte lowest). For keys shorter than four bytes it ORs in the 1 to 3 bytes available. If `hasCollisions` is true and the key is longer than four bytes, it extends the hash by folding the remaining bytes with a multiplier of 31, mirroring what the compiler-side finder does.

`handleSelectNoMatchCore(...)` runs when the key is not a known field. It moves the position to just after the closing quote, resets the next-token byte, and then either consumes the `:` (when `consumeSeparator` is set) or, in strict mode, decodes the key and throws "Unknown field in strict mode". It returns `MATCH_NONE`.

`selectEscapedKeyCore(...)` handles keys containing backslash escapes: it reads the key like a normal string, looks it up in `options.rawStrings` by `indexOf`, and returns the index or `MATCH_NONE`, again throwing in strict mode if unknown and not consuming a separator.

## The code

```kotlin
key = byte0 or
        (byte1 shl SCN.SHIFT_8) or
        (byte2 shl SCN.SHIFT_16) or
        (byte3 shl SCN.SHIFT_24)
if (hasCollisions) {
    var ci = SCN.KEY_HASH_WORD_BYTES
    while (ci < length) {
        key = key * SCN.COLLISION_HASH_MULTIPLIER + getByte(start + ci); ci++
    }
}
```

## Related

- [[JsonReaderOptions]]: calls `computeKeyHashCore` to build its dispatch tables.
- [[GhostJsonScanConstants]]: supplies the shifts, word size and collision multiplier.
- [[GhostJsonErrorMessages]]: supplies the strict-mode message.
- [[GhostJsonFlatReaderSubsystem]]: flat reader code using these kernels.
- [[GhostJsonReaderSubsystem]]: streaming reader code using them.
- [[GhostJsonStringReaderSubsystem]]: string reader code using them.
- [[PerfectHashFinder]]: the compiler-side component that must compute the same hash.

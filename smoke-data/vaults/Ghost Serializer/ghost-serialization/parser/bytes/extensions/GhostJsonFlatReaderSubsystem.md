---
tags: [ghost-serializer, serialization, parser, field-select, perfect-hash]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/extensions/GhostJsonFlatReaderSubsystem.kt
kind: functions
---

# GhostJsonFlatReaderSubsystem

The field-name matcher of the byte reader: `internalSelect`, which turns a JSON key into the index of a Kotlin property.

## What it is about

When a generated serializer reads an object, for each key it must decide which field it belongs to. [[GhostJsonFlatReader]]'s `selectNameAndConsume` and `selectString` (used for enum values) both call `internalSelect`, defined here, with a [[JsonReaderOptions]] built at compile time for that class. The aim is to avoid allocating a `String` for the key at all.

## How it works

`internalSelect` has three tiers:

1. **Predicted key.** Most JSON lists fields in declaration order, so the reader remembers `predictedFieldIndex`. It checks that the byte after the predicted name is a closing quote, then compares the key with the candidate. For names up to eight bytes it does one masked `ghostReadLong8` and compares with a pre-padded copy of the key; longer names compare eight bytes at a time, then byte by byte. On a hit, one pass has matched the key and optionally consumed the `:`.
2. **Perfect hash.** On a miss, it finds the closing quote with a scan, hashes the bytes, and indexes a dispatch table with `((hash * multiplier + length) shr shift) and (size - 1)`. `verifyKeyMatch` confirms the candidate bytes (unrolled by four) to guard against collisions.
3. **No match.** `handleSelectNoMatch` skips unknown keys, or fails in strict mode. An escaped key (no clean closing quote) is routed to `selectEscapedKeyCore`.

The comma rules are validated first by `selectValidateCommas`, so strict mode keeps its exact syntax checks.

## The code

The masked single-word comparison for short names:

```kotlin
val matched = if (candidateLength <= SCN.LONG_BYTES && start + SCN.LONG_BYTES <= localData.size) {
    val inputLong = ghostReadLong8(
        data = localData,
        index = start
    ) and ghostSWARLengthMasks[candidateLength]
    inputLong == ghostReadLong8(data = options.predictedKeyPadded[predicted], index = 0)
} else {
    // …
}
```

The hash dispatch:

```kotlin
val hasIndex =
    ((key * options.multiplier + length) shr options.shift) and (options.dispatch.size - 1)
val index = options.dispatch[hasIndex]
```

## Related

- [[GhostJsonFlatReader]]: the reader whose select methods call this.
- [[JsonReaderOptions]]: holds `rawBytes`, `predictedKeyPadded`, `dispatch`, `multiplier` and `shift`.
- [[GhostSwar]]: provides the read and the length masks.
- [[PerfectHashFinder]]: the compile-time search that produces the multiplier and shift.
- [[GhostJsonKeySelectHelpers]]: the shared no-match, escaped-key and hashing cores.
- [[GhostJsonCommaValidationHelpers]]: the shared comma validation.
- [[GhostJsonStringReaderSubsystem]]: the same selector for the `String` reader.

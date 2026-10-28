---
tags: [ghost-serialization, serialization, parser, codegen-runtime]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostWrappedKeysCapture.kt
kind: class
---

# GhostWrappedKeysCapture

A short-lived collector that stores several JSON values from different keys and reassembles them into one synthetic object, for the `@GhostWrappedKeys` feature.

## What it is about

Some payloads spread the fields of one Kotlin type across several sibling keys of the wire object. Generated deserializers for classes annotated with `@GhostWrappedKeys` use this class to hold each captured value, then ask it to build a small JSON object that the normal decoder can read. It is public because generated code calls it.

## How it works

The constructor takes `slotCount`, the number of keys declared in the annotation. It keeps an array of `RawJson` values (zero-copy slices of the input) and an `Int` bit mask `presentMask`, one bit per slot, so the class supports at most 32 slots. `put(index, value)` stores a slice and sets its bit.

`materializeWrappedObject(keyUtf8Literals, omitIfEmpty, omitIfAbsentIndices)` returns the bytes of `{"k1":v1,"k2":v2}`, or `null`:

1. If `omitIfEmpty` is set and every slot is absent or JSON `null`, return `null`.
2. If any slot listed in `omitIfAbsentIndices` is absent or `null`, return `null`.
3. Estimate the size from key prefixes and value lengths; with nothing present return `{}` (or `null` if `omitIfEmpty`).
4. Acquire a pooled scratch buffer, write `{`, then each present slot in order separated by commas, copying the pre-encoded `"key":` prefix and the value bytes, then `}`.
5. Return `scratch.copyOf(writePos)`.

The scratch buffer comes from the pool, so only the final copy is allocated.

## The code

```kotlin
fun put(index: Int, value: RawJson) {
    values[index] = value
    presentMask = presentMask or (1 shl index)
}
```

Three extension functions named `captureWrappedKey` on the flat, streaming and string readers each call `captureRawJson()` on the reader and pass the result to `put`, so the capture works on any channel.

## Related

- [[RawJson]]: the zero-copy value type stored in each slot.
- [[GhostWrappedKeys]]: the annotation that triggers the use of this class.
- [[GhostJsonFlatReader]]: gets a `captureWrappedKey` extension.
- [[GhostJsonReader]]: gets a `captureWrappedKey` extension.
- [[GhostJsonStringReader]]: gets a `captureWrappedKey` extension.
- [[GhostEmitterConstants]]: the compiler constants that name this class and its `captureWrappedKey` calls in generated code.
- [[GhostPools]]: provides the pooled scratch buffer.

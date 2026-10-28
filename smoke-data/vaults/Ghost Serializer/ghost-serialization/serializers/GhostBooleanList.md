---
tags: [ghost-serializer, serialization, primitive-collection]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/GhostBooleanList.kt
kind: class
---

# GhostBooleanList

An internal growable buffer of `Boolean` that stores values in a primitive `BooleanArray` without boxing.

## What it is about

`GhostBooleanList` is a tiny append-only list used while decoding primitive arrays. When the number of elements in a JSON array is not known in advance, the array serializers need somewhere to collect values; a `ArrayList<Boolean>` would box every element, so Ghost keeps a raw `BooleanArray` and grows it by hand. It is `internal` and lives in the serializer layer, next to the code that uses it (the fast boolean scan). Unlike the Int and Long variants it has no `isEmpty()`, because its only user, the fast boolean scan, never needs to ask.

## How it works

The list starts with a `BooleanArray` of `initialCapacity` elements (default `GhostJsonNumericLimits.DEFAULT_PRIMITIVE_COLLECTION_CAPACITY`). `add` first checks whether the buffer is full; if so it allocates a new array with `copyOf`, whose capacity is the old size times `GhostJsonWriterConstants.BUFFER_SCALE_FACTOR`, falling back to the default capacity if the buffer was created empty. Then it stores the value and bumps `currentSize`.

`toArray()` returns the internal buffer itself when it is exactly full, and otherwise a trimmed copy. Returning the buffer without a copy saves one allocation in the common case, but it means the list must not be reused after `toArray()`; the callers create a fresh list per array.

## The code

The growth step is the core of the class:

```kotlin
    fun add(value: Boolean) {
        if (currentSize == buffer.size) {
            val newCapacity = if (buffer.isEmpty()) {
                NUM.DEFAULT_PRIMITIVE_COLLECTION_CAPACITY
            } else {
                (buffer.size * WR.BUFFER_SCALE_FACTOR)
            }
            buffer = buffer.copyOf(newSize = newCapacity)
        }
        buffer[currentSize++] = value
    }
```

The result is produced by `toArray()`, which avoids the copy when no slack is left:

```kotlin
    fun toArray(): BooleanArray {
        if (currentSize == buffer.size) return buffer
        return buffer.copyOf(newSize = currentSize)
    }
```

## Related

- [[BooleanArraySerializer]]: uses it through the fast boolean scan.
- [[GhostFastPrimitiveArrayHelpers]]: `tryFastBooleanArrayCore` fills one while matching `true`/`false` literals.
- [[GhostJsonNumericLimits]]: provides the default initial capacity.
- [[GhostJsonWriterConstants]]: provides `BUFFER_SCALE_FACTOR`, the growth factor.

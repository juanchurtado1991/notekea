---
tags: [ghost-serializer, serialization, primitive-collection]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/GhostIntList.kt
kind: class
---

# GhostIntList

An internal growable buffer of `Int` that stores values in a primitive `IntArray` without boxing.

## What it is about

`GhostIntList` is a tiny append-only list used while decoding primitive arrays. When the number of elements in a JSON array is not known in advance, the array serializers need somewhere to collect values; a `ArrayList<Int>` would box every element, so Ghost keeps a raw `IntArray` and grows it by hand. It is `internal` and lives in the serializer layer, next to the code that uses it (`IntArraySerializer` and the fast integer scan).

## How it works

The list starts with a `IntArray` of `initialCapacity` elements (default `GhostJsonNumericLimits.DEFAULT_PRIMITIVE_COLLECTION_CAPACITY`). `add` first checks whether the buffer is full; if so it allocates a new array with `copyOf`, whose capacity is the old size times `GhostJsonWriterConstants.BUFFER_SCALE_FACTOR`, falling back to the default capacity if the buffer was created empty. Then it stores the value and bumps `currentSize`.

`toArray()` returns the internal buffer itself when it is exactly full, and otherwise a trimmed copy. Returning the buffer without a copy saves one allocation in the common case, but it means the list must not be reused after `toArray()`; the callers create a fresh list per array.

## The code

The growth step is the core of the class:

```kotlin
    fun add(value: Int) {
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
    fun toArray(): IntArray {
        if (currentSize == buffer.size) return buffer
        return buffer.copyOf(newSize = currentSize)
    }
```

`isEmpty()` simply tests `currentSize == 0`; the array serializers use it in strict mode to know whether a comma must be consumed before the next element.

## Related

- [[IntArraySerializer]]: creates it to collect elements when the fast path does not apply.
- [[GhostFastPrimitiveArrayHelpers]]: `tryFastIntArrayCore` fills one while scanning a compact run of integers.
- [[GhostJsonNumericLimits]]: provides the default initial capacity.
- [[GhostJsonWriterConstants]]: provides `BUFFER_SCALE_FACTOR`, the growth factor.

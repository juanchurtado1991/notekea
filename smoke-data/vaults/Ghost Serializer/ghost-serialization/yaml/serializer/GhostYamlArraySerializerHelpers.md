---
tags: [ghost-serialization, serialization, yaml, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/serializer/GhostYamlArraySerializerHelpers.kt
kind: functions
---

# GhostYamlArraySerializerHelpers

Two inline loop helpers shared by all the primitive-array YAML serializers.

## What it is about

`IntArray`, `LongArray`, `BooleanArray` and the other primitive arrays are read and written with the same loop: begin the sequence, visit each element, end the sequence. Copying that loop into seven serializers would be repetitive, and writing it with generics would box every primitive. This file provides the loop once, as `inline` functions with lambdas, so every caller gets a copy specialized for its own element type.

## How it works

**Reading** (`readYamlArrayCore`): it calls `beginArray`, creates an `ArrayList<T>`, and repeats `readElement` while `hasNextArrayElement` is true; then it calls `endArray` and returns the list. The caller converts the list with `toIntArray()` or the matching function. The `ArrayList` is the one remaining boxing step on the read side, because the length of a YAML sequence is not known in advance.

**Writing** (`writeYamlArrayCore`): the size is known, so it needs no list. It calls `beginArray`, runs `writeElement(index)` for `index` in `0 until size`, then `endArray`. The loop uses a plain `while` with a manual counter.

Since both functions are `inline` and `internal`, the lambdas are expanded into each call site: no function objects are created, and the JIT or native compiler sees concrete types.

## The code

The read kernel:

```kotlin
internal inline fun <T> readYamlArrayCore(
    beginArray: () -> Unit,
    hasNextArrayElement: () -> Boolean,
    readElement: () -> T,
    endArray: () -> Unit,
): ArrayList<T> {
    beginArray()
    val list = ArrayList<T>()
    while (hasNextArrayElement()) {
        list.add(element = readElement())
    }
    endArray()
    return list
}
```

The write kernel:

```kotlin
beginArray()
var index = 0
while (index < size) {
    writeElement(index)
    index++
}
endArray()
```

## Related

- [[GhostYamlIntArraySerializer]]: a user of both kernels.
- [[GhostYamlLongArraySerializer]]: a user of both kernels.
- [[GhostYamlDoubleArraySerializer]]: a user of both kernels.
- [[GhostYamlBooleanArraySerializer]]: a user of both kernels.
- [[GhostYamlFlatReader]]: supplies `beginArray`, `hasNextArrayElement` and `endArray`.
- [[GhostYamlWriter]]: receives the write calls.

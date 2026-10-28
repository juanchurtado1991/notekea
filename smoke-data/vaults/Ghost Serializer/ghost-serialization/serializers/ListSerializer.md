---
tags: [ghost-serializer, serialization, collection-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/ListSerializer.kt
kind: class
---

# ListSerializer

A generic serializer class that reads and writes a Kotlin `List<T>` as a JSON array, delegating each element to an item serializer.

## What it is about

`ListSerializer<T>` is the building block for every `List<T>` that Ghost handles. Unlike the scalar `object` serializers it is a class, constructed with the `itemSerializer: GhostSerializer<T>` that knows the element type. It works on all three reader channels and both writer channels, and it honours the item serializer's `isResilient` flag, which decides whether a malformed element fails the whole list or is skipped.

## How it works

Reading has two branches. If `itemSerializer.isResilient` is true, each element is read inside `reader.decodeResilient { ... }`, which returns `null` for a bad element, and `filterNotNull()` then drops those, so the list keeps only the valid items. Otherwise the plain `reader.readList { ... }` helper is used and the first bad element fails the read. The three read overloads are identical apart from the reader type.

Writing starts with `beginArray()` and returns right away through `endArray()` for an empty list. A list that implements `RandomAccess` (such as `ArrayList`) is walked by index, which avoids creating an iterator; any other list uses a `for` loop. `typeName` is built from `TOK.TYPE_NAME_LIST_PREFIX`, the item's own `typeName` and the generic suffix, which gives names such as `List<Int>` that appear in error messages.

[[CharArraySerializer]] and [[ShortArraySerializer]] reuse this class to avoid writing their own loops.

## The code

The read side, showing the resilient and the plain branch:

```kotlin
    override fun deserialize(reader: GhostJsonReader): List<T> {
        return if (itemSerializer.isResilient) {
            reader.readList {
                reader.decodeResilient {
                    itemSerializer.deserialize(reader = reader)
                }
            }.filterNotNull()
        } else {
            reader.readList {
                itemSerializer.deserialize(reader = reader)
            }
        }
    }
```

The write side picks an index loop for `RandomAccess` lists:

```kotlin
        if (value is RandomAccess) {
            for (i in 0 until size) {
                itemSerializer.serialize(writer = writer, value = value[i])
            }
        } else {
            for (item in value) {
                itemSerializer.serialize(
                    writer = writer,
                    value = item
                )
            }
```

## Related

- [[AbstractGhostSerializer]]: the base class this class extends.
- [[GhostSerializer]]: the contract of the item serializer passed to the constructor.
- [[Ghost]]: its `getSerializer(KType)` builds this serializer around the resolved item serializer.
- [[GhostJsonReader]]: the streaming reader channel.
- [[CharArraySerializer]]: uses a list serializer of `Char` for its elements.
- [[ShortArraySerializer]]: uses a list serializer of `Short` for its elements.
- [[SetSerializer]]: the `Set<T>` counterpart.

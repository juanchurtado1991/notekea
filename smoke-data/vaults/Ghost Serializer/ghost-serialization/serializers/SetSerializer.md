---
tags: [ghost-serializer, serialization, collection-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/SetSerializer.kt
kind: class
---

# SetSerializer

A generic serializer class that reads and writes a Kotlin `Set<T>` as a JSON array, delegating each element to an item serializer.

## What it is about

`SetSerializer<T>` is the `Set` counterpart of [[ListSerializer]]. It is constructed with the `itemSerializer: GhostSerializer<T>` for the element type, and it covers all three reader channels and both writer channels. JSON has no set type, so a set is encoded as an array and decoded into a `LinkedHashSet`, which keeps the order in which the elements appear in the input.

## How it works

Reading has two branches. When the item serializer is resilient (`isResilient`), the set is built by hand: `beginArray()`, then a `while (reader.hasNext())` loop in which every element is read through `reader.decodeResilient { ... }`, and results that are `null` (malformed items) are simply not added; then `endArray()`. This is why the file imports `beginArray`, `hasNext` and `endArray` that the list serializer does not need. Otherwise the shared `reader.readSet { ... }` helper reads the array and fails on the first bad element.

Writing is simpler than for lists: `beginArray()`, an immediate `endArray()` for an empty set, otherwise a `for` loop over the items (a `Set` has no random access), then `endArray()`. `typeName` is `TOK.TYPE_NAME_SET_PREFIX` + the item type name + the generic suffix, which gives names like `Set<String>`.

## The code

The resilient branch is the interesting difference from the list:

```kotlin
            val result = LinkedHashSet<T>()
            reader.beginArray()
            while (reader.hasNext()) {
                val item = reader.decodeResilient {
                    itemSerializer.deserialize(reader = reader)
                }
                if (item != null) result.add(element = item)
            }
            reader.endArray()
            result
```

The write side:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: Set<T>) {
        writer.beginArray()
        if (value.isEmpty()) {
            writer.endArray()
            return
        }
        for (item in value) {
            itemSerializer.serialize(writer = writer, value = item)
        }
        writer.endArray()
    }
```

## Related

- [[AbstractGhostSerializer]]: the base class this class extends.
- [[GhostSerializer]]: the contract of the item serializer passed to the constructor.
- [[ListSerializer]]: the sibling for `List<T>`, with the same resilience rule.
- [[Ghost]]: its `getSerializer(KType)` builds this serializer around the resolved item serializer.
- [[GhostJsonReader]]: the streaming reader channel.
- [[GhostJsonFlatReader]]: the flat byte reader channel.
- [[GhostJsonStringReader]]: the string reader channel.

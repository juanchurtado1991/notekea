---
tags: [ghost-serialization, serialization, yaml, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/serializer/GhostYamlBooleanArraySerializer.kt
kind: object
---

# GhostYamlBooleanArraySerializer

The YAML serializer for `BooleanArray`, a primitive array, written as a YAML sequence.

## What it is about

Primitive arrays such as `BooleanArray` are not `List`s, so they need their own serializer. This object implements [[GhostYamlSerializer]] for `BooleanArray`: reading turns a YAML sequence into the array, writing emits the elements one by one as a sequence. It is a very thin file because the shared loop lives in [[GhostYamlArraySerializerHelpers]]. The serializer is registered in the lookup that resolves a serializer for a type.

## How it works

Reading calls `readYamlArrayCore` with four lambdas that delegate to the reader: `beginArray`, `hasNextArrayElement`, `nextBoolean` for each element, and `endArray`. The elements are collected in an `ArrayList` and then copied with `toBooleanArray()`. The extra list is the only boxing on the read side, accepted so that the loop is written once. Writing calls `writeYamlArrayCore`, which goes through the indexes and calls `writer.value(value = value[it])`. Each element is written as a YAML boolean through the writer.

Both helpers are `inline`, so the lambdas are expanded in place and the generated code is specialized for this element type, with no generic dispatch.

## The code

Reading:

```kotlin
override fun deserialize(
    reader: GhostYamlFlatReader
): BooleanArray = readYamlArrayCore(
    beginArray = { reader.beginArray() },
    hasNextArrayElement = { reader.hasNextArrayElement() },
    readElement = { reader.nextBoolean() },
    endArray = { reader.endArray() },
).toBooleanArray()
```

Writing:

```kotlin
writeYamlArrayCore(
    size = value.size,
    beginArray = { writer.beginArray() },
    writeElement = { writer.value(value = value[it]) },
    endArray = { writer.endArray() },
)
```

## Related

- [[GhostYamlSerializer]]: the interface implemented.
- [[GhostYamlArraySerializerHelpers]]: the shared read and write loops.
- [[GhostYamlFlatReader]]: the reader supplying elements.
- [[GhostYamlWriter]]: the writer receiving them.
- [[GhostSerializerLookup]]: resolves this serializer for `BooleanArray`.
- [[GhostYamlIntArraySerializer]]: a sibling for another primitive type.

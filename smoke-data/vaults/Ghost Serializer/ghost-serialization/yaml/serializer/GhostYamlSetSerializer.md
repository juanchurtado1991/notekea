---
tags: [ghost-serialization, serialization, yaml, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/serializer/GhostYamlSetSerializer.kt
kind: class
---

# GhostYamlSetSerializer

A serializer for `Set<T>` that supports the JSON channels and the YAML channel from one object.

## What it is about

Like [[GhostYamlListSerializer]], this class lets Ghost handle a collection as a body or as a resolved type, here a `Set<T>`. It extends `AbstractGhostSerializer<Set<T>>`, which gives it the JSON contract, and also implements [[GhostYamlSerializer]]. It is used when the library resolves `Set<T>` for a type whose item serializer already supports both formats.

## How it works

The item serializer type `S` must implement both [[GhostSerializer]] and [[GhostYamlSerializer]], so no runtime check is required.

- **JSON:** all four JSON methods (two readers, two writers) call a private [[SetSerializer]] built from the item serializer.
- **YAML write:** a YAML sequence is written with `beginArray`, one `serialize` per item, and `endArray`. The set's iteration order decides the order in the output.
- **YAML read:** `reader.readSet { ... }` collects items into a `Set`.

`typeName` gives `Set<` plus the item type name plus `>`.

## The code

The header and delegate:

```kotlin
class GhostYamlSetSerializer<T, S>(private val itemSerializer: S) :
    AbstractGhostSerializer<Set<T>>(),
    GhostYamlSerializer<Set<T>> where S : GhostSerializer<T>, S : GhostYamlSerializer<T> {

    private val jsonSet = SetSerializer(itemSerializer = itemSerializer)
```

The YAML paths:

```kotlin
override fun deserialize(reader: GhostYamlFlatReader): Set<T> =
    reader.readSet { itemSerializer.deserialize(reader = reader) }
```

```kotlin
writer.beginArray()
for (item in value) {
    itemSerializer.serialize(writer = writer, value = item)
}
writer.endArray()
```

## Related

- [[GhostYamlSerializer]]: the YAML contract implemented.
- [[AbstractGhostSerializer]]: base class for the JSON side.
- [[SetSerializer]]: the JSON set serializer it delegates to.
- [[GhostYamlListSerializer]]: the equivalent for lists.
- [[GhostYamlMapSerializer]]: the equivalent for maps.
- [[GhostYamlFlatReader]]: provides `readSet`.
- [[GhostReflectTypeSerializers]]: builds collection serializers like this one for resolved types.

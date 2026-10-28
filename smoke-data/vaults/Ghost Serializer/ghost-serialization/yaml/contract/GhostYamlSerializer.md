---
tags: [ghost-serialization, serialization, yaml, contract]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/contract/GhostYamlSerializer.kt
kind: interface
---

# GhostYamlSerializer

The two-method interface that a type implements to be read from and written to YAML by Ghost.

## What it is about

Ghost has one serializer contract per text channel. For YAML it is this interface: a serializer knows how to rebuild a `T` from a [[GhostYamlFlatReader]] and how to emit a `T` to a [[GhostYamlWriter]]. The generated serializers, the built-in ones for lists, maps and arrays, and user-written custom coders all meet this contract, so the rest of the library can treat any type the same way.

## How it works

At runtime the YAML entry points look up the serializer for a type and call one method. `deserialize` consumes tokens from the reader and returns the value; `serialize` writes the value through the writer's methods. Lookup code checks that what it found really implements `GhostYamlSerializer`, and fails with an error saying the serializer "does not implement GhostYamlSerializer" otherwise (see [[GhostYamlErrorMessages]]). The interface carries no state and no lifecycle, which keeps implementations cheap and thread-safe.

## The code

The whole file is the interface:

```kotlin
interface GhostYamlSerializer<T> {
    fun deserialize(reader: GhostYamlFlatReader): T

    fun serialize(writer: GhostYamlWriter, value: T)
}
```

The container serializers ([[GhostYamlListSerializer]], [[GhostYamlSetSerializer]], [[GhostYamlMapSerializer]], [[NullableYamlSerializer]]) are implemented on top of it by wrapping an element serializer.

## Related

- [[GhostYamlFlatReader]]: the type `deserialize` reads from.
- [[GhostYamlWriter]]: the type `serialize` writes to.
- [[GhostYamlListSerializer]]: a built-in implementation for lists.
- [[GhostYamlMapSerializer]]: a built-in implementation for maps.
- [[NullableYamlSerializer]]: adds null handling around another serializer.
- [[GhostYamlErrorMessages]]: holds the message for a serializer that lacks this interface.
- [[GhostSerializer]]: the equivalent contract for JSON.

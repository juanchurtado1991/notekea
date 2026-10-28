---
tags: [ghost-serialization, serialization, yaml, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/serializer/GhostYamlMapSerializer.kt
kind: class
---

# GhostYamlMapSerializer

A serializer for `Map<String, V>` that supports JSON and YAML.

## What it is about

Endpoints that return a map as the whole body need a serializer for it. This class wraps a value serializer into a map serializer on both channels. It extends `AbstractGhostSerializer<Map<String, V>>` and implements [[GhostYamlSerializer]]. Keys are always `String`; the value type `V` is handled by the wrapped serializer, whose type `S` must support both [[GhostSerializer]] and [[GhostYamlSerializer]] (checked by the compiler).

## How it works

- **JSON:** reading and writing with either JSON reader or writer is delegated to a private [[MapSerializer]].
- **YAML write:** a YAML mapping is opened with `beginObject`; for each entry the key is written with `writer.name(key = key)` and then the value with the value serializer; `endObject` closes it.
- **YAML read:** `reader.readMap` is called with a key parser that reads `reader.nextKey()` and a value lambda. The key parser uses `!!`, so a missing key would raise a null pointer exception rather than a YAML error; the reader's own checks normally prevent that in the middle of a mapping.

The `typeName` text is `Map<String, ` plus the value type name plus `>`.

## The code

Writing a map:

```kotlin
writer.beginObject()
for ((key, entryValue) in value) {
    writer.name(key = key)
    valueSerializer.serialize(writer = writer, value = entryValue)
}
writer.endObject()
```

Reading a map:

```kotlin
override fun deserialize(reader: GhostYamlFlatReader): Map<String, V> =
    reader.readMap(keyParser = { reader.nextKey()!! }) { valueSerializer.deserialize(reader = reader) }
```

Because `name` quotes keys that could be misread (see [[GhostYamlWriterHelpers]]), any string is safe as a map key on output.

## Related

- [[GhostYamlSerializer]]: the YAML contract implemented.
- [[AbstractGhostSerializer]]: base class for the JSON side.
- [[MapSerializer]]: the JSON map serializer it delegates to.
- [[GhostYamlListSerializer]]: the equivalent for lists.
- [[GhostYamlFlatReader]]: provides `readMap` and `nextKey`.
- [[GhostYamlWriter]]: provides `name`.
- [[GhostYamlWriterHelpers]]: decides when a key is quoted.

---
tags: [ghost-serialization, serialization, yaml, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/serializer/GhostYamlListSerializer.kt
kind: class
---

# GhostYamlListSerializer

A serializer for `List<T>` that works on the JSON channels and the YAML channel at once.

## What it is about

HTTP integrations (Retrofit, Ktor) sometimes need a list as a whole request or response body, for example an endpoint returning `List<User>`. This class wraps an element serializer into a list serializer for both JSON and YAML. It extends `AbstractGhostSerializer<List<T>>` for the JSON side and implements [[GhostYamlSerializer]] for the YAML side.

## How it works

The element serializer is typed with a double bound: `S : GhostSerializer<T>` and `S : GhostYamlSerializer<T>`. The compiler therefore guarantees that the item serializer can serve both channels, and no runtime cast or check is needed.

- **JSON:** both JSON reader types (streaming and string) and both writers delegate to a private [[ListSerializer]] built from the same item serializer, so the list logic is not duplicated.
- **YAML read:** `reader.readList { itemSerializer.deserialize(reader = reader) }` uses the reader's inline `readList` helper.
- **YAML write:** it calls `beginArray`, serializes each item and calls `endArray`.

`typeName` is built as `List<` + the item's type name + `>`.

## The code

The class header shows the double constraint and the JSON delegate:

```kotlin
class GhostYamlListSerializer<T, S>(
    private val itemSerializer: S,
) : AbstractGhostSerializer<List<T>>(), GhostYamlSerializer<List<T>> where S : GhostSerializer<T>, S : GhostYamlSerializer<T> {

    private val jsonList = ListSerializer(itemSerializer = itemSerializer)
```

The YAML write:

```kotlin
override fun serialize(writer: GhostYamlWriter, value: List<T>) {
    writer.beginArray()
    for (item in value) {
        itemSerializer.serialize(
        writer = writer,
        value = item
    )
    }
    writer.endArray()
}
```

## Related

- [[GhostYamlSerializer]]: the YAML contract implemented.
- [[AbstractGhostSerializer]]: the base class that provides the JSON side.
- [[ListSerializer]]: the JSON list serializer it delegates to.
- [[GhostYamlSetSerializer]]: the same idea for `Set<T>`.
- [[GhostYamlMapSerializer]]: the same idea for `Map<String, V>`.
- [[GhostYamlFlatReader]]: provides `readList`.
- [[GhostYamlExtensions]]: uses this serializer for list bodies.

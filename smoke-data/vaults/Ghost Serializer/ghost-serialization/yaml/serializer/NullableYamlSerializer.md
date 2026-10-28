---
tags: [ghost-serialization, serialization, yaml, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/serializer/NullableYamlSerializer.kt
kind: class
---

# NullableYamlSerializer

Adds YAML null handling to a serializer, so a `List<T?>` or similar keeps its YAML codec.

## What it is about

The existing [[NullableSerializer]] wraps a serializer so it can read and write `null` on the JSON channels. That wrapper knows nothing about YAML. This class extends it and adds the [[GhostYamlSerializer]] contract for `T?`, so a nullable element can travel through the YAML channel as well. The wrapped `delegate` must support both formats: its type `S` is constrained to [[GhostSerializer]] and [[GhostYamlSerializer]].

## How it works

Reading checks first whether the next value is null. If it is, the reader consumes the null token and the serializer returns `null`; otherwise it hands over to the delegate. Writing is the mirror: a `null` value becomes the YAML `null` literal; anything else goes to the delegate. JSON behavior is inherited unchanged from [[NullableSerializer]].

The YAML reader parses the whole document into a tree first, and every null spelling (`null`, `Null`, `NULL`, `~`) becomes a `null` in that tree, so `isNextNullValue` is a simple check of the pending value. The writer always emits the word `null`.

## The code

The whole logic fits in two methods:

```kotlin
override fun deserialize(reader: GhostYamlFlatReader): T? {
    if (reader.isNextNullValue()) {
        reader.consumeNull()
        return null
    }
    return delegate.deserialize(reader = reader)
}

override fun serialize(writer: GhostYamlWriter, value: T?) {
    if (value == null) writer.nullValue() else delegate.serialize(writer = writer, value = value)
}
```

The class declaration ties it to the JSON wrapper:

```kotlin
class NullableYamlSerializer<T : Any, S>(
    private val delegate: S
) : NullableSerializer<T>(delegate = delegate), GhostYamlSerializer<T?>
```

## Related

- [[NullableSerializer]]: the JSON base class extended here.
- [[GhostYamlSerializer]]: the YAML contract implemented.
- [[GhostYamlFlatReader]]: provides `isNextNullValue` and `consumeNull`.
- [[GhostYamlWriter]]: provides `nullValue`.
- [[GhostYamlListSerializer]]: a collection serializer that may hold nullable items.
- [[GhostSerializer]]: the JSON contract of the delegate.

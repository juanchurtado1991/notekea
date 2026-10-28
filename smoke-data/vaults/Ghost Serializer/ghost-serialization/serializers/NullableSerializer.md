---
tags: [ghost-serializer, serialization, nullable]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/NullableSerializer.kt
kind: class
---

# NullableSerializer

An open generic wrapper that adds JSON `null` handling to any other serializer, producing a serializer of `T?`.

## What it is about

`NullableSerializer<T>` wraps a `delegate: GhostSerializer<T>` and makes it accept `null`. It is used when Ghost resolves a type argument from a `KType`, for example the `Int?` in `List<Int?>` or the `User?` in `Map<String, User?>`: the collection serializer needs a serializer of `T?`, and this class supplies it. It is `open` so that other modules can subclass it.

## How it works

Reading checks `reader.isNextNullValue()` first. If the next value is `null`, it calls `reader.consumeNull()` and returns `null`; otherwise it hands the reader to the delegate. The same code is repeated for the three reader types. Writing is a single line: `writer.nullValue()` for a `null`, and the delegate's `serialize` otherwise. `typeName` is the delegate's name plus `TOK.TYPE_NAME_NULLABLE_SUFFIX`, so an `Int` delegate gives `Int?`.

The wrapper adds one branch and no allocation per value, and it keeps null handling out of every element serializer.

## The code

The class header and the streaming read:

```kotlin
open class NullableSerializer<T : Any>(
    private val delegate: GhostSerializer<T>
) : AbstractGhostSerializer<T?>() {
    // …
    override fun deserialize(reader: GhostJsonReader): T? {
        if (reader.isNextNullValue()) {
            reader.consumeNull()
            return null
        }
        return delegate.deserialize(reader = reader)
    }
```

The write side:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: T?) {
        if (value == null) writer.nullValue() else delegate.serialize(writer = writer, value = value)
    }
```

## Related

- [[AbstractGhostSerializer]]: the base class this class extends.
- [[GhostSerializer]]: the contract of the wrapped delegate.
- [[ListSerializer]]: wraps it for `List<T?>` element types.
- [[MapSerializer]]: wraps it for `Map<String, T?>` value types.
- [[Ghost]]: `getSerializer(KType)` wraps the resolved serializer in this class when the type is marked nullable.
- [[GhostJsonReader]]: the streaming reader whose `isNextNullValue()` and `consumeNull()` are used.
- [[GhostJsonWriter]]: the byte writer whose `nullValue()` is used.

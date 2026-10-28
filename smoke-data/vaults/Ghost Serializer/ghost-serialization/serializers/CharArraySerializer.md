---
tags: [ghost-serializer, serialization, primitive-serializer, array]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/CharArraySerializer.kt
kind: object
---

# CharArraySerializer

The serializer object for a primitive `CharArray`, implemented by delegating to a list serializer of `Char`.

## What it is about

`CharArraySerializer` handles `CharArray`. Unlike the Int, Long, Double and Float array serializers it has no fast path: every channel delegates to a private [[ListSerializer]] whose item serializer is [[CharSerializer]], and converts between the list and the array. It exists so that `CharArray::class` has a registered serializer, and it keeps rarely used array types small, at the price of boxing each element.

## How it works

On read, the `deserialize` overload of the matching channel calls `elements.deserialize(reader = reader)`, which produces a `List<Char>`, and then `.toCharArray()` copies it into a primitive array. On write, `value.asList()` wraps the array in a list view without copying, and `elements.serialize(...)` writes it as a JSON array.

As a consequence the array format is exactly the format of `List<Char>`: the same bracket and comma handling, the same strict-mode checks, and the same error reporting come from the list serializer and the item serializer. `typeName` is the `TOK.TYPE_NAME_CHAR_ARRAY` constant of [[GhostJsonTokens]], not a generic name.

## The code

The delegate is created once as a private property:

```kotlin
    private val elements = ListSerializer(itemSerializer = CharSerializer)

    override val typeName: String = TOK.TYPE_NAME_CHAR_ARRAY
```

Every read overload converts the list to a primitive array, and every write overload passes an `asList()` view:

```kotlin
    override fun deserialize(
        reader: GhostJsonReader
    ): CharArray = elements.deserialize(reader = reader).toCharArray()
    // …
    override fun serialize(
        writer: GhostJsonWriter,
        value: CharArray
    ) {
        elements.serialize(
            writer = writer,
            value = value.asList()
        )
    }
```

## Related

- [[AbstractGhostSerializer]]: the base class this object extends.
- [[ListSerializer]]: the delegate that does all the array reading and writing.
- [[CharSerializer]]: the item serializer given to that delegate.
- [[GhostJsonTokens]]: holds the `TYPE_NAME_CHAR_ARRAY` constant.
- [[GhostJsonReader]]: the streaming reader channel.
- [[GhostJsonStringWriter]]: the string writer channel.
- [[GhostSerializerLookup]]: maps `CharArray::class` to this serializer.

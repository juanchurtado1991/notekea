---
tags: [ghost-serializer, serialization, proto, wkt, any, registry]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoAnyRegistry.kt
kind: object
---

# ProtoAnyRegistry

A registry that maps `typeUrl` strings to Kotlin classes, so a `ProtoAny` can be packed and unpacked as a typed message.

## What it is about

A [[ProtoAny]] only holds a type URL and raw JSON bytes. To turn a Kotlin message into one, or an `Any` back into a message, Ghost needs to know which URL belongs to which class. `ProtoAnyRegistry` stores that mapping. It is separate from [[Ghost]]'s serializer registry: the message class must still have a [[GhostSerializer]] registered there (for example through `@GhostProtoSerialization` and KSP).

## How it works

Two mutable maps hold the association in both directions. `register(typeUrl, kClass)` fills both, and `typeUrlFor` and `classFor` read them.

`pack` looks up the type URL for the class (failing with a helpful message if none was registered), obtains the serializer from the given [[GhostRegistry]] (the global [[Ghost]] by default), encodes the message with `Ghost.encodeToBytes` and returns a `ProtoAny`. `unpack` runs the other way: it verifies that the wire type URL matches the one registered for the target class, and that the URL is not registered for a different class, then decodes the payload by lending a pooled reader from `ghostProtoInternalUseFlatReader`. `unpackDynamic` resolves the class purely from the URL and returns `null` when nothing is registered or no serializer exists.

The maps are plain `mutableMapOf`, not synchronised, so registrations are expected at start-up. `resetForTest` clears them to keep tests independent.

## The code

```kotlin
    fun register(typeUrl: String, kClass: KClass<*>) {
        typeUrlByClass[kClass] = typeUrl
        classByTypeUrl[typeUrl] = kClass
    }
```

Unpacking verifies the URL before decoding:

```kotlin
        val expectedTypeUrl = typeUrlFor(kClass = kClass)
        if (expectedTypeUrl != null && any.typeUrl != expectedTypeUrl) {
            Ghost.throwError(
                message = "ProtoAny typeUrl mismatch: expected '$expectedTypeUrl' for ${kClass.simpleName}, " +
                        "got '${any.typeUrl}'"
            )
```

Decoding through the pooled reader:

```kotlin
        return ghostProtoInternalUseFlatReader(bytes = any.value) { reader ->
            serializer.deserialize(reader = reader)
        }
```

## Related

- [[ProtoAny]]: the message type that is packed and unpacked
- [[Ghost]]: provides `encodeToBytes` and the default registry
- [[GhostRegistry]]: the serializer lookup passed to `pack` and `unpack`
- [[GhostProtoPools]]: supplies the pooled reader used when unpacking
- [[GhostSerializer]]: the serializer that must exist for each packed type
- [[ProtoAnySerializer]]: reads and writes the `ProtoAny` itself

---
tags: [ghost-serializer, ktor, serializer-lookup, encoding]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostKtorSerializers.kt
kind: functions
---

# GhostKtorSerializers

Internal helper functions shared by Ghost's three Ktor converters: serializer lookup and response encoding.

## What it is about

The JSON, proto3 and YAML converters differ only in the reader or writer they run. The steps around that are identical, and they live here so they are written once. The file is part of `ghost-ktor` and has no public API.

## How it works

There are three functions.

1. `resolveKtorSerializer` finds the serializer. If the Ktor `TypeInfo` carries a Kotlin type (`kotlinType`), the lookup goes through `Ghost.getSerializer(type)`, because only `Ghost` can resolve generic collections like `List<T>`, `Set<T>` and `Map<K, V>`. If that gives nothing, it falls back to `registry.getSerializer(fallbackClass)`, the injectable registry. The result is a `GhostSerializer<Any>?`, with an unchecked cast on the fallback class.
2. `encodeKtorContent` is the JSON and proto3 write path: return `null` for a null value, resolve the serializer, encode with `Ghost.encodeToBytes`, and wrap.
3. `ghostKtorBytesContent` wraps UTF-8 bytes in `ByteArrayContent`. For UTF-8 it does no extra work. For another negotiated charset it decodes the text, re-encodes it with that charset and adds the charset to the content type, as kotlinx.serialization's converter does.

## The code

```kotlin
internal fun resolveKtorSerializer(
    typeInfo: TypeInfo,
    fallbackClass: KClass<*>,
    registry: GhostRegistry
): GhostSerializer<Any>? = typeInfo.kotlinType?.let { Ghost.getSerializer(it) }
    ?: registry.getSerializer(fallbackClass as KClass<Any>)
```

The two-step lookup is the design trade-off: generics get full support, while tests can still inject a fake registry for simple classes.

## Related

- [[GhostContentConverter]]: calls `resolveKtorSerializer` and `encodeKtorContent`.
- [[GhostProtoContentConverter]]: uses the same helpers.
- [[GhostYamlContentConverter]]: uses `resolveKtorSerializer` and `ghostKtorBytesContent`.
- [[GhostRegistry]]: the fallback lookup contract.
- [[GhostSerializer]]: the type returned by the lookup.
- [[Ghost]]: owns generic-type resolution and encoding.

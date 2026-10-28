---
tags: [ghost-serializer, serialization, entry-point, registry, api]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/Ghost.kt
kind: object
---

# Ghost

The `Ghost` singleton: the public entry point for encoding and decoding, and the place where serializers are found and cached.

## What it is about

`Ghost` is what application code calls: `Ghost.deserialize<T>(json)`, `Ghost.encodeToString(value)`, `Ghost.serialize(sink, value)` and so on. It sits on top of the generated serializers and the pooled readers and writers. It also implements `GhostRegistry` itself, so HTTP integrations can depend on the registry abstraction and still get the aggregate of every registered module. The file declares the `expect` hooks that each platform implements (maps, locks, registry discovery, pooled reader and writer helpers).

## How it works

**Serializer lookup.** `getSerializer(KClass)` goes through four steps: primitive and primitive-array serializers, built-in protobuf well-known types, a lock-free `serializerCache`, and finally a synchronized, double-checked search through manually added registries, discovered registries and the compiler-plugin link (see `GhostSerializerLookup`). Hits are cached. `getSerializer(KType)` handles `List`, `Set` and `Map<String, V>` by building the collection serializer around the element serializer and caching it by `KType` in `typeCache`; a nullable type is wrapped in `NullableSerializer` (or the YAML version). The reified `resolveSerializer<T>()` checks the class cache first and only computes `typeOf<T>()` on a miss.

**Registries.** `addRegistry` is required on platforms without discovery (iOS, Wasm without the plugin). `prewarm` discovers registries, caches their serializers and calls `warmUp()` on each. `resetForTest` clears everything.

**Encode and decode.** The inline functions pick a serializer and call a pooled helper: bytes use the flat writer, strings use the char writer (except on Wasm JavaScriptCore, which encodes bytes and converts them with `ghostUtf8BytesToString`), and `serialize(sink, ...)` drains the pooled buffer into the Okio sink in a single write. Decoding from a `BufferedSource` loads the full payload into a pooled scratch buffer and parses it with the flat reader; `deserializeStreaming` instead uses the streaming reader with constant memory.

## The code

```kotlin
override fun <T : Any> getSerializer(clazz: KClass<T>): GhostSerializer<T>? {
    getPrimitiveSerializer(clazz = clazz)?.let { return it }
    getWktSerializer(clazz = clazz)?.let { return it }
    val cached = serializerCache[clazz] as? GhostSerializer<T>
    if (cached != null) {
        return cached
    }
    return runSynchronized(lock = lock) {
        // double-check, then search the registries and cache the result
    }
}
```

- `inline fun <reified T : Any> deserialize(json: String): T` runs the string reader through `ghostInternalUseStringReader`.
- `deserialize(bytes)` and `decodeFromBytes` use `ghostInternalUseFlatReader`; the non-inline `decodeFromSource` and `encodeToSink(..., clazz)` exist for Spring and Retrofit, where there is no reified type.
- `readSourceIntoFlatReader` calls `source.request(Long.MAX_VALUE)`, copies into `acquireScratchBuffer`, parses, then releases the buffer in `finally`.
- `serializerNotFoundMessage` combines `NOT_FOUND`, the type, `MISSING_ANN` and the platform's `serializerRegistrationHint`.
- The `deserialize(json, options)`, `deserialize(source, options)` and `deserialize(bytes, options)` overloads let the caller set `strictMode`, coercion flags or limits on the reader before parsing.

## Related

- [[GhostRegistry]]: the interface `Ghost` implements and the registries it aggregates
- [[GhostSerializerLookup]]: holds the primitive, well-known-type and registry lookup tables
- [[GhostSerializer]]: what `getSerializer` returns
- [[GhostPools]]: scratch buffers used when reading from a `BufferedSource`
- [[Ghost.jvmAndroid]]: JVM and Android actuals for the pooled helpers
- [[Ghost.nonJvm]]: Native and Wasm actuals for the pooled helpers
- [[GhostYamlExtensions]]: adds YAML encode and decode functions to `Ghost`
- [[GhostHeuristics]]: supplies `encodeToStringViaUtf8Bytes`

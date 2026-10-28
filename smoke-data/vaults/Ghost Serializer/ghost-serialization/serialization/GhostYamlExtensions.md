---
tags: [ghost-serializer, serialization, yaml, api]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/GhostYamlExtensions.kt
kind: functions
---

# GhostYamlExtensions

Extension functions that add YAML encoding and decoding to the `Ghost` object.

## What it is about

The core `Ghost` object is about JSON. YAML support lives in its own parser and writer, and this file exposes it with the same style of API: `Ghost.encodeToYaml(value)`, `Ghost.decodeFromYaml<T>(yaml)`, plus multi-document variants and byte variants. Keeping them as extensions means the YAML code does not enlarge `Ghost`.

## How it works

Every function resolves a `GhostYamlSerializer` for the reified type with `resolveYamlSerializer`, then runs it with a pooled YAML flat reader (`ghostYamlInternalUseFlatReader`) or writer (`ghostYamlInternalUseFlatWriter`). The `All` variants handle multi-document streams: decoding calls `readAllDocuments`, and encoding joins documents with a `---` line, either as a string or, for bytes, by writing the separator tokens directly.

`resolveYamlSerializer` first checks the YAML primitive-array serializers, so JSON array serializers do not shadow them. It then asks `Ghost` for the serializer and throws `IllegalArgumentException` if none exists, or if the one found does not implement `GhostYamlSerializer`. `asYamlCapableSerializer` is an unchecked cast helper for code that has already verified `is GhostYamlSerializer<*>` and needs the compiler to accept both bounds, which the YAML collection wrappers require.

## The code

```kotlin
inline fun <reified T : Any> Ghost.encodeToYaml(value: T): String {
    val yamlSerializer = resolveYamlSerializer(clazz = T::class)
    return ghostYamlInternalUseFlatWriter { writer, buffer ->
        yamlSerializer.serialize(writer = writer, value = value)
        buffer.toStringUtf8()
    }
}

/** Decodes every YAML document in [yaml] (separated by `---`) into instances of [T]. */
inline fun <reified T : Any> Ghost.decodeAllFromYaml(yaml: String): List<T> {
    val yamlSerializer = resolveYamlSerializer(clazz = T::class)
    return ghostYamlInternalUseFlatReader(bytes = yaml.encodeToByteArray()) { reader ->
        reader.readAllDocuments { docReader -> yamlSerializer.deserialize(reader = docReader) }
    }
}
```

`ghostYamlEncodeToBytes(serializer, value)` is the non-reified entry point shared by the Retrofit, Spring and Ktor integrations, which resolve serializers dynamically.

## Related

- [[Ghost]]: the object these functions extend
- [[GhostYamlSerializer]]: the contract resolved for each type
- [[GhostYamlPools]]: pooled YAML readers and writers used by the helpers
- [[GhostYamlFlatReader]]: reader that decodes each document
- [[GhostYamlWriter]]: writer that produces the YAML text
- [[GhostYamlListSerializer]]: collection wrapper that needs `asYamlCapableSerializer`
- [[GhostYamlHttpMessageConverter]]: Spring converter that uses the byte-encode function

---
tags: [ghost-serializer, retrofit, converter, yaml]
source: ghost-retrofit/src/main/kotlin/com/ghost/serialization/retrofit/GhostYamlConverterFactory.kt
kind: class
---

# GhostYamlConverterFactory

The Retrofit converter factory for YAML request and response bodies.

## What it is about

For services that speak YAML, this factory lets a Retrofit interface declare models annotated with [[GhostYamlSerialization]] and have Ghost convert them. It lives in `ghost-retrofit` and is used like the JSON factory: `addConverterFactory(GhostYamlConverterFactory.create())`.

## How it works

It builds a `GhostReflectTypeSerializers` with `yamlOnly = true`, so type resolution considers only serializers that support YAML. Both converters then check that the serializer found is a `GhostYamlSerializer`; if not, they return `null` so Retrofit can ask another factory.

- **Request:** the value is encoded with `ghostYamlEncodeToBytes` and wrapped as a `RequestBody` with `application/yaml; charset=UTF-8`.
- **Response:** the body is read into a pooled scratch buffer through [[GhostRetrofitBuffers]] and trimmed to the exact length (a copy is made only when the buffer is not already full), then parsed by `ghostYamlInternalUseFlatReader` and decoded by the serializer. The body is closed with `use`.

Unlike the JSON factory, there is no strict or coerce handling and no special null-body step, since the YAML serializer handles its own document structure.

## The code

```kotlin
val bytesToParse =
    if (offset == scratch.size) scratch else scratch.copyOf(offset)
ghostYamlInternalUseFlatReader(bytes = bytesToParse) { reader ->
    yamlSerializer.deserialize(reader)
}
```

The copy is needed because the YAML reader entry point used here takes only a byte array.

## Related

- [[GhostConverterFactory]]: the JSON counterpart.
- [[GhostProtoConverterFactory]]: the proto3 JSON counterpart.
- [[GhostRetrofitBuffers]]: reads the body into a pooled buffer.
- [[GhostRetrofitMediaTypes]]: provides the YAML media type.
- [[GhostYamlSerializer]]: the contract the found serializer must implement.
- [[GhostYamlFlatReader]]: the YAML reader used to parse.
- [[GhostYamlSerialization]]: annotation the models carry.

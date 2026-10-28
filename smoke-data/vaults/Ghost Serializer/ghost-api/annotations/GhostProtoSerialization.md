---
tags: [ghost-serializer, api, annotations, proto]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostProtoSerialization.kt
kind: annotation
---

# GhostProtoSerialization

The class annotation that makes Ghost generate serializers following the proto3 JSON mapping.

## What it is about

Protocol Buffers define a canonical JSON form that differs from ordinary JSON conventions. Adding `@GhostProtoSerialization` to a class makes the compiler emit a serializer that follows it, so Kotlin messages can talk to gRPC gateways and other proto-aware services. It is the proto counterpart of [[GhostSerialization]] and lives in `ghost-api`.

## How it works

The generated code applies the proto3 rules: field names in `lowerCamelCase`, default or empty values omitted when writing, 64-bit integers written as quoted strings, enums written as strings and byte arrays as Base64. The optional `name` argument serves the same purpose as in [[GhostSerialization]], avoiding registry collisions.

The annotation is deliberately restrictive. It is incompatible with envelopes, flatten and wrap, sealed or `inferred` types on the message itself, custom decoders and encoders, `RawJson`, and opaque `ByteArray` fields that are not proto bytes. Combining any of those, or [[GhostResilient]], with [[GhostYamlSerialization]] on the same class is a KSP error.

## The code

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GhostProtoSerialization(
    val name: String = "",
)
```

Only the optional `name` exists; everything else is decided by the proto rules in the compiler and the runtime helpers.

## Related

- [[GhostSerialization]]: the regular JSON annotation it parallels.
- [[GhostYamlSerialization]]: can be added on top for YAML output.
- [[GhostAnalyzer]]: detects the annotation and chooses the proto path.
- [[GhostProto]]: runtime entry point for proto3 JSON.
- [[GhostProtoContentConverter]]: Ktor converter that uses proto serializers.
- [[GhostProtoConverterFactory]]: Retrofit converter factory for the same.
- [[GhostJsonEnvelope]]: one of the annotations it cannot be combined with.

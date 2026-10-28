---
tags: [ghost-serializer, spring-boot-starter, constants, media-types]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostSpringMediaTypes.kt
kind: object
---

# GhostSpringMediaTypes

An internal object with the media types and MIME types used by the Spring MVC converters and WebFlux codecs.

## What it is about

Spring has two parallel type systems: `MediaType` (used in MVC) and `MimeType` (used by the reactive codecs). It also has no built-in constants for YAML or NDJSON. This object defines all that Ghost needs, so the converters stay free of repeated literals. It belongs to `ghost-spring-boot-starter`.

## How it works

The object offers four groups of members.

- **String parts:** `TYPE_APPLICATION`, `TYPE_TEXT`, and subtypes such as `*+json`, `yaml`, `x-yaml`, `x-ndjson`.
- **`MediaType` values** for MVC: `APPLICATION_JSON_SUFFIX` (matches any `+json` type), `APPLICATION_YAML`, `APPLICATION_X_YAML` and `TEXT_YAML`.
- **`MimeType` values** for WebFlux: the three YAML variants and `MIME_APPLICATION_X_NDJSON`.
- **NDJSON helpers:** `NDJSON_NEWLINE`, the byte for `\n`, and `isNdJson(mimeType)`, which returns true when the subtype contains `ndjson`.

NDJSON (newline-delimited JSON) is a streaming format where each line is one JSON value. The reactive decoder and encoder use these helpers to switch between one joined document and a line-by-line stream.

## The code

```kotlin
/** Record separator of an NDJSON stream. */
const val NDJSON_NEWLINE: Byte = '\n'.code.toByte()

fun isNdJson(mimeType: MimeType?): Boolean = mimeType?.subtype?.contains(SUBTYPE_NDJSON_TOKEN) == true
```

## Related

- [[GhostHttpMessageConverter]]: registers `application/json` plus the `*+json` suffix type.
- [[GhostYamlHttpMessageConverter]]: registers the three YAML media types.
- [[GhostReactiveDecoder]]: uses NDJSON detection and the newline byte.
- [[GhostReactiveEncoder]]: appends the newline in NDJSON mode.
- [[GhostYamlReactiveDecoder]]: uses the YAML `MimeType` constants.
- [[GhostKtorMediaTypes]]: the Ktor module's equivalent.

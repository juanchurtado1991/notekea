---
tags: [ghost-serializer, ktor, constants, yaml]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostKtorMediaTypes.kt
kind: object
---

# GhostKtorMediaTypes

An internal object with the content-type constants used for YAML negotiation in Ghost's Ktor module.

## What it is about

Ktor has a built-in `ContentType.Application.Json` but none for YAML, so Ghost defines its own. These constants are used by the client and server code in `ghost-ktor` and by its tests, so that `application/yaml` is spelled in a single place.

## How it works

The object holds string constants for the pieces and the full values: `TYPE_APPLICATION`, `SUBTYPE_YAML`, `JSON_CONTENT_TYPE` (`application/json`) and `YAML_CONTENT_TYPE` (`application/yaml`). It also builds a ready-to-use `ContentType` value, `APPLICATION_YAML`, from the type and subtype constants. That value is the default for `ghostYaml()` registration and is what `respondGhostYaml` puts in the response.

Like [[GhostKtorErrorMessages]], it is `@PublishedApi internal`, which lets inline public functions such as `respondGhostYaml` refer to it even though it is not part of the public API. The Spring and Retrofit modules have their own equivalents, which keeps each integration free of cross-dependencies.

Keeping the constants in one object also means the client converter, the server response helper and the tests cannot drift apart: if the YAML type ever changed, one line would change for all of them. The `JSON_CONTENT_TYPE` string is there for places that need the plain text form of the JSON type rather than a `ContentType` instance.

## The code

```kotlin
@PublishedApi
internal object GhostKtorMediaTypes {
    const val TYPE_APPLICATION = "application"
    const val SUBTYPE_YAML = "yaml"
    const val JSON_CONTENT_TYPE = "application/json"
    const val YAML_CONTENT_TYPE = "application/yaml"

    val APPLICATION_YAML = ContentType(TYPE_APPLICATION, SUBTYPE_YAML)
}
```

## Related

- [[GhostKtorExtensions]]: uses `APPLICATION_YAML` as the default content type of `ghostYaml()`.
- [[GhostServerExtensions]]: responds with it in `respondGhostYaml`.
- [[GhostYamlContentConverter]]: the converter registered under this type.
- [[GhostRetrofitMediaTypes]]: the Retrofit module's equivalent.
- [[GhostSpringMediaTypes]]: the Spring module's equivalent.

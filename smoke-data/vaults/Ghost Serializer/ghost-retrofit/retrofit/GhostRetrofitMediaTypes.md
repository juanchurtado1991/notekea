---
tags: [ghost-serializer, retrofit, constants, media-types]
source: ghost-retrofit/src/main/kotlin/com/ghost/serialization/retrofit/GhostRetrofitMediaTypes.kt
kind: object
---

# GhostRetrofitMediaTypes

An internal object with the media-type strings used by Ghost's Retrofit converter factories.

## What it is about

Converters must tell OkHttp what they are sending, and a string such as `application/json; charset=UTF-8` is easy to mistype if repeated. This small object keeps the values in one place for the `ghost-retrofit` module. It has no logic.

## How it works

The constants are plain `const val` strings:

- `APPLICATION_JSON` and `APPLICATION_YAML` for the bare types.
- `APPLICATION_JSON_UTF8` and `APPLICATION_YAML_UTF8`, the same with `; charset=UTF-8`.
- `CONTENT_TYPE_HEADER`, the header name `Content-Type`.

The factories convert these strings into OkHttp `MediaType` objects once, in their companion objects, with `toMediaType()`, so there is no per-request parsing. The JSON and proto3 JSON factories use the JSON value (proto3 JSON is ordinary JSON on the wire), and the YAML factory uses the YAML one. Always stating the charset means requests do not depend on OkHttp's default.

Because the object is `internal`, it is invisible to applications that use the Retrofit module, so the values can be renamed or extended without affecting the public API. It has no behaviour to test beyond the strings themselves, which the converter factory tests exercise indirectly through the request headers.

## The code

```kotlin
internal object GhostRetrofitMediaTypes {
    const val APPLICATION_JSON = "application/json"
    const val APPLICATION_YAML = "application/yaml"
    const val CONTENT_TYPE_HEADER = "Content-Type"
    const val APPLICATION_JSON_UTF8 = "application/json; charset=UTF-8"
    const val APPLICATION_YAML_UTF8 = "application/yaml; charset=UTF-8"
}
```

## Related

- [[GhostConverterFactory]]: builds its JSON request media type from here.
- [[GhostProtoConverterFactory]]: uses the JSON constant too.
- [[GhostYamlConverterFactory]]: uses the YAML constant.
- [[GhostKtorMediaTypes]]: the Ktor module's equivalent.
- [[GhostSpringMediaTypes]]: the Spring module's equivalent.

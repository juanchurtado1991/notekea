---
tags: [ghost-serializer, retrofit, converter, json]
source: ghost-retrofit/src/main/kotlin/com/ghost/serialization/retrofit/GhostConverterFactory.kt
kind: class
---

# GhostConverterFactory

The Retrofit `Converter.Factory` that serializes and deserializes JSON request and response bodies with Ghost.

## What it is about

Retrofit asks its converter factories to turn Java `Type`s into body converters. `GhostConverterFactory` is Ghost's JSON answer, in the `ghost-retrofit` module: add it with `addConverterFactory(GhostConverterFactory.create())` and endpoints using `@GhostSerialization` models are handled without reflection on the models themselves.

## How it works

The constructor is private; `create(registry)` builds the factory, with the global `Ghost` as default registry so tests can inject a fake. The factory owns a `GhostReflectTypeSerializers`, a resolver that maps a Retrofit `Type` to a serializer, including `List<T>`, `Set<T>` and `Map<String, V>` when the element serializers are registered. If no serializer is found, the factory returns `null`, which lets Retrofit try the next converter.

- **Requests:** `requestBodyConverter` encodes the value with `Ghost.encodeToBytes` and wraps it as a `RequestBody` of `application/json; charset=UTF-8`.
- **Responses:** `responseBodyConverter` inspects the endpoint annotations. If `@GhostStrict` or `@GhostCoerce` is present, the flags are applied to the reader. The body is read into a pooled buffer by [[GhostRetrofitBuffers]] and parsed by `ghostInternalUseFlatReader`. A JSON `null` body is consumed and returned as `null` rather than passed to the serializer. The `ResponseBody` is closed with `use`.

## The code

```kotlin
val isStrict = annotations.any { it is GhostStrict }
val isCoerce = annotations.any { it is GhostCoerce }

return Converter { body ->
    body.use {
        GhostRetrofitBuffers.readToScratch(
            stream = it.byteStream(),
            charset = it.contentType()?.charset()
        ) { scratch, offset ->
            ghostInternalUseFlatReader(
                bytes = scratch, limit = offset
            ) { reader ->
                reader.applyOptions(isStrict = isStrict, isCoerce = isCoerce)
                // …
```

Because the annotations come from the Retrofit method, one interface can mix strict and lenient endpoints.

## Related

- [[GhostRetrofitBuffers]]: reads the response stream into a pooled buffer.
- [[GhostRetrofitMediaTypes]]: provides the JSON media type string.
- [[GhostReflectTypeSerializers]]: resolves Retrofit types to serializers.
- [[GhostStrict]]: annotation honoured on the endpoint.
- [[GhostCoerce]]: annotation honoured on the endpoint.
- [[GhostProtoConverterFactory]]: variant for proto3 JSON.
- [[GhostYamlConverterFactory]]: variant for YAML.
- [[GhostRegistry]]: the lookup contract for the `registry` parameter.

---
tags: [ghost-serializer, serialization, parser, http, options]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/GhostReaderOptions.kt
kind: functions
---

# GhostReaderOptions

Two extension functions that apply per-request `strict` and `coerce` flags to a JSON or YAML flat reader.

## What it is about

The HTTP integrations let a developer turn on strict parsing or value coercion through annotations or configuration. The framework resolves two booleans for each request, and these helpers copy them onto the reader the call is about to use. They are public so other modules can call them, hence `@InternalGhostApi`.

## How it works

Both functions do the same thing for different readers. `strictMode` is set to `isStrict`. If `isCoerce` is true, both `coerceStringsToNumbers` and `coerceBooleans` are switched on; if it is false they are left as they are, which means a pooled reader's own defaults apply. The YAML overload is the counterpart for `GhostYamlFlatReader`.

## The code

```kotlin
@InternalGhostApi
fun GhostJsonFlatReader.applyOptions(
    isStrict: Boolean,
    isCoerce: Boolean
) {
    strictMode = isStrict
    if (isCoerce) {
        coerceStringsToNumbers = true
        coerceBooleans = true
    }
}
```

## Related

- [[GhostJsonFlatReader]]: the JSON reader configured by the first overload
- [[GhostYamlFlatReader]]: the YAML reader configured by the second overload
- [[GhostConverterFactory]]: Retrofit integration that applies the options
- [[GhostHttpMessageConverter]]: Spring integration that applies them
- [[GhostYamlHttpMessageConverter]]: Spring YAML converter that applies them
- [[GhostStrict]]: annotation that requests strict parsing
- [[GhostCoerce]]: annotation that requests coercion

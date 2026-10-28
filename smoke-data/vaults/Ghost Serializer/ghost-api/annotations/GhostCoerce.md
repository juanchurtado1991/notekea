---
tags: [ghost-serializer, api, annotations, coercion]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostCoerce.kt
kind: annotation
---

# GhostCoerce

A runtime annotation that asks Ghost to accept loosely typed JSON, such as the string `"42"` where an integer is expected.

## What it is about

Real APIs are not always strict about types: a number may arrive quoted, or a flag as `"true"`. `@GhostCoerce` turns on that tolerance for a given endpoint, parameter or whole controller, instead of changing the model class. It lives in `ghost-api` and is meant for the web integrations, where request handling methods are the natural place to annotate.

## How it works

Unlike most annotations in the module, this one has `RUNTIME` retention, so it can be found by reflection. The Spring request advice and the Retrofit converter factory look for it on the controller method, parameter or class and, when found, create the reader with coercion enabled. In the compiler-generated model code nothing changes; only the reader's options differ for that call.

The related annotation [[GhostStrict]] pushes the opposite direction, enabling strict syntax checks. They address different concerns: coercion widens what is accepted, strictness narrows it.

## The code

```kotlin
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class GhostCoerce
```

The three targets match the three levels where the integrations look: a single method (an endpoint), one parameter, or a class (a controller). There are no arguments; the presence of the annotation is the setting.

## Related

- [[GhostStrict]]: the opposite switch, strict JSON syntax checking.
- [[GhostRequestBodyAdvice]]: Spring component that reads this annotation on incoming bodies.
- [[GhostConverterFactory]]: Retrofit converter factory that honours it.
- [[GhostReaderOptions]]: the reader options that the annotation ends up toggling.
- [[GhostResilient]]: another leniency feature, applied per property instead.

---
tags: [ghost-serializer, api, annotations, strict]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostStrict.kt
kind: annotation
---

# GhostStrict

A runtime annotation that makes JSON parsing strict for one endpoint, parameter or controller.

## What it is about

By default Ghost's parser is permissive and fast about small syntax flaws. For inputs you do not trust, you may want to reject missing, duplicate or trailing commas, and fields in the payload that the model does not map. `@GhostStrict` switches those checks on where it is placed. It lives in `ghost-api` and is aimed at the Spring and Retrofit integrations.

## How it works

Its retention is `RUNTIME`, so the web integrations can look it up by reflection on the handler method, its parameter, or the controller class. When found, they create the reader with the strict option enabled for that call. The models and generated serializers are unchanged; only the reader behaviour differs, so the same class can be parsed leniently in one endpoint and strictly in another.

Strictness is the mirror image of [[GhostCoerce]]: one narrows what is acceptable, the other widens it. The cost of strictness is some extra validation work per document, which is why it is opt-in.

## The code

```kotlin
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class GhostStrict
```

A marker with no arguments. The targets give three granularities: method, parameter, class.

## Related

- [[GhostCoerce]]: the loose-typing counterpart.
- [[GhostRequestBodyAdvice]]: Spring advice that applies the option to request bodies.
- [[GhostConverterFactory]]: Retrofit converter factory that reads it.
- [[GhostReaderOptions]]: the reader options carrying the strict flag.
- [[GhostJsonException]]: the error type thrown when strict parsing rejects the input.

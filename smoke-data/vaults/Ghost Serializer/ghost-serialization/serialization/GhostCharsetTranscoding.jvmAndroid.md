---
tags: [ghost-serializer, serialization, jvm, http, charset]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/GhostCharsetTranscoding.jvmAndroid.kt
kind: functions
---

# GhostCharsetTranscoding.jvmAndroid

Two tiny functions that let the HTTP integrations honor a non-UTF-8 `charset=` on a request or response body.

## What it is about

Ghost's parsers read UTF-8 only. A server can still declare `ISO-8859-1`, `UTF-16` or similar in `Content-Type`. The Retrofit and Spring integrations use these helpers to detect that case and re-encode the bytes to UTF-8 before parsing. They are public only so those modules can call them, hence `@InternalGhostApi`.

## How it works

`ghostInternalNeedsTranscoding` returns true when a charset is declared and is neither UTF-8 nor US-ASCII. ASCII is a strict subset of UTF-8, so no work is needed for it. `ghostInternalTranscodeToUtf8` decodes the first `length` bytes with the declared charset into a `String` and encodes it again as UTF-8. This allocates a string and a new array, which is acceptable because it only runs on the uncommon non-UTF-8 path.

## The code

```kotlin
@InternalGhostApi
fun ghostInternalNeedsTranscoding(
    charset: Charset?
): Boolean = charset != null && charset != Charsets.UTF_8 && charset != Charsets.US_ASCII

@InternalGhostApi
fun ghostInternalTranscodeToUtf8(
    bytes: ByteArray,
    length: Int,
    charset: Charset
): ByteArray = String(bytes, 0, length, charset).encodeToByteArray()
```

## Related

- [[GhostRetrofitBuffers]]: Retrofit response and request buffer helpers that call these functions
- [[GhostSpringDataBuffers]]: Spring buffer helper that calls them
- [[InternalGhostApi]]: the opt-in marker for these public-but-internal functions

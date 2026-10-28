---
tags: [ghost-serialization, serialization, yaml, errors]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/exception/GhostYamlException.kt
kind: class
---

# GhostYamlException

The exception thrown for invalid or unsupported YAML, with a path and an optional hint, plus the function that picks the hint.

## What it is about

Every YAML parse or decode failure in Ghost ends as a `GhostYamlException`. Besides the message it carries a JSONPath-like location and a developer-facing suggestion. The file also holds `hintForYamlError`, which turns an error message into its hint. It is the YAML counterpart of [[GhostJsonException]].

## How it works

There are two phases. During byte parsing the document is not yet a tree, so the path stays at the root `"$"`; Ghost avoids inventing a deeper location that would mislead. During the cursor phase (typed deserialization walking the parsed tree) the path is specific, such as `$.user.age`. The `message` property is built on demand: base text, then ` [path ...]`, then, if a hint exists, a new line `Hint: ...`.

`hintForYamlError` first asks the shared JSON function `hintForJsonError` (required field, discriminator, enum and similar messages). Only when that returns nothing does it check YAML-specific prefixes with `startsWith`.

## The code

The exception composes its message lazily:

```kotlin
override val message: String
    get() = "$baseMessage [path $path]" +
        if (hint.isNullOrEmpty()) "" else "$HINT_PREFIX$hint"
```

The hint lookup falls back from shared to YAML-specific rules:

```kotlin
internal fun String.hintForYamlError(): String? {
    hintForJsonError()?.let { return it }

    return when {
        startsWith(prefix = EM.ERR_EXPECTED_MAP_PREFIX) -> HINT_EXPECTED_MAP
        startsWith(prefix = EM.ERR_EXPECTED_LIST_PREFIX) -> HINT_EXPECTED_LIST
        // …
        else -> null
    }
}
```

The numeric prefixes (Int, Long, Double, Float, ULong) share `HINT_EXPECTED_SCALAR`; Boolean maps to the coerce hint; the nesting-depth and anchor-not-found prefixes have their own.

## Related

- [[GhostYamlHintMessages]]: the hint texts returned here.
- [[GhostYamlErrorMessages]]: the prefixes matched here.
- [[GhostYamlErrorReporting]]: parse-phase code that builds this exception.
- [[GhostYamlCursorTraversal]]: cursor-phase code that supplies real paths.
- [[GhostJsonException]]: the JSON equivalent.
- [[GhostYamlWriter]]: throws it for write errors such as exceeding the depth.

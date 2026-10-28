---
tags: [ghost-serialization, serialization, yaml, parser, errors]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlErrorReporting.kt
kind: functions
---

# GhostYamlErrorReporting

A single extension function, `yamlError`, that every parser file calls to fail with a positioned `GhostYamlException`.

## What it is about

The YAML reader is split into many small files, and all of them need to report syntax problems the same way. This file gives them one entry point on `GhostYamlFlatReader`. It returns `Nothing`, so the compiler knows the code after the call is unreachable and the caller does not need a return value.

## How it works

`yamlError(message)` builds the final text by appending the reader's current byte `position` in the form ` (position=N)`, using the fragments of [[GhostYamlErrorMessages]]. It then throws [[GhostYamlException]] with three values: the text, the path `"$"`, and a hint from `hintForYamlError` computed on the original message. The path is always the root because this is the parse phase: no tree exists yet, and a made-up deeper path would be misleading. The cursor phase builds its own exceptions with real paths.

## The code

```kotlin
internal fun GhostYamlFlatReader.yamlError(message: String): Nothing {
    // Parse phase: no AST cursor yet — keep path at root so we never invent a fake location.
    throw GhostYamlException(
        baseMessage = "$message${EM.ERR_AT_POSITION_PAREN_PREFIX}$position${EM.ERR_AT_POSITION_PAREN_SUFFIX}",
        path = JSON_PATH_ROOT,
        hint = message.hintForYamlError(),
    )
}
```

The hint is computed from `message`, not from the text with the position, so that prefix matching is not disturbed by the suffix. `@OptIn(InternalGhostApi::class)` is needed because the hint function is internal API.

## Related

- [[GhostYamlException]]: the exception thrown, and the hint function used.
- [[GhostYamlErrorMessages]]: supplies the message and position fragments.
- [[GhostYamlFlatReader]]: the receiver; its `position` is reported.
- [[GhostYamlKeyReading]]: one of many callers.
- [[GhostYamlWhitespace]]: another caller, for tab and comment errors.
- [[GhostYamlFlowStyleSubsystem]]: reports flow collection errors through it.

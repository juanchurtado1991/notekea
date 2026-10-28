---
tags: [ghost-serializer, compiler-plugin, constants, native]
source: ghost-compiler-plugin/src/main/kotlin/com/ghost/serialization/compiler/plugin/GhostCompilerPluginConstants.kt
kind: object
---

# GhostCompilerPluginConstants

An internal object that gathers the identifiers and messages shared by the parts of the Ghost compiler plugin.

## What it is about

The compiler plugin is a small Kotlin compiler extension with several classes that must agree on names: the plugin id, the command-line option, the annotation classes it looks for, and the naming of generated serializers. Keeping them in one object avoids magic strings. It lives in `ghost-compiler-plugin`, which is applied only to Kotlin/Native and Kotlin/Wasm builds.

## How it works

Three groups of constants matter.

1. **Plugin identity.** `PLUGIN_ID` is `com.ghostserializer.ghost` and `OPTION_ENABLED` is `enabled`. The Gradle side passes the same values, so they must match exactly. `KEY_ENABLED` is a `CompilerConfigurationKey<Boolean>` used to carry the parsed option from the command-line processor to the registrar.
2. **Declarations it links.** Three `ClassId` values point to the annotations `GhostSerialization` and `GhostProtoSerialization` and to the contract class `GhostSerializerLink`.
3. **Naming rules.** `SERIALIZER_SUFFIX` (`Serializer`) and `NESTED_NAME_SEPARATOR` (`_`) mirror how the KSP processor names generated serializers, for example `<package>.<Outer_Inner>Serializer`. The plugin recomputes that name instead of asking KSP.

The `MISSING_SERIALIZER_*` strings are assembled into a warning when a model has no generated serializer.

## The code

```kotlin
internal object GhostCompilerPluginConstants {
    const val PLUGIN_ID = "com.ghostserializer.ghost"

    const val OPTION_ENABLED = "enabled"
    // …
    private const val KEY_ENABLED_NAME = "ghost.enabled"
    val KEY_ENABLED = CompilerConfigurationKey<Boolean>(KEY_ENABLED_NAME)
```

The warning text tells the developer to check that KSP runs for the source set or to call `Ghost.addRegistry`, the manual fallback.

## Related

- [[GhostCommandLineProcessor]]: uses the plugin id and the option constants.
- [[GhostCompilerPluginRegistrar]]: reads the enabled key.
- [[GhostSerializerLinkExtension]]: uses the class ids and the naming rules.
- [[GhostSerialization]]: one of the annotations the ids point to.
- [[GhostProtoSerialization]]: the second annotation.
- [[GhostCompilerPluginSupport]]: the Gradle side that must pass the same plugin id.

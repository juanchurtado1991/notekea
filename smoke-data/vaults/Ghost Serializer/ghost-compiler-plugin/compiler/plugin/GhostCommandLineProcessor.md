---
tags: [ghost-serializer, compiler-plugin, cli-options]
source: ghost-compiler-plugin/src/main/kotlin/com/ghost/serialization/compiler/plugin/GhostCommandLineProcessor.kt
kind: class
---

# GhostCommandLineProcessor

Parses the single option of the Ghost compiler plugin, `enabled`, and stores it in the compiler configuration.

## What it is about

Kotlin compiler plugins receive settings as `-P plugin:<id>:<name>=<value>` arguments. A `CommandLineProcessor` declares which options exist and converts them into configuration entries. For Ghost there is only one option, which the Gradle plugin sets from the `autoRegistration` property. The class belongs to `ghost-compiler-plugin`.

## How it works

The processor advertises its `pluginId` and one `CliOption`, named `enabled`, not required, with the value description `<true|false>`. When the compiler passes an option, `processOption` checks its name. For `enabled` it calls `toBooleanStrict()` on the text and stores the result under `KEY_ENABLED`. Strict parsing means anything other than `true` or `false` throws instead of being silently treated as false. Any other option name raises `CliOptionProcessingException` with a message that begins "Unknown Ghost compiler plugin option".

Because the option is not required, the plugin works without any argument, and the registrar defaults it to enabled.

## The code

```kotlin
override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration
) {
    when (option.optionName) {
        C.OPTION_ENABLED -> configuration.put(C.KEY_ENABLED, value.toBooleanStrict())
        else -> throw CliOptionProcessingException(message = C.UNKNOWN_OPTION_PREFIX + option.optionName)
    }
}
```

All names come from [[GhostCompilerPluginConstants]] (imported as `C`), which keeps them identical to what the Gradle plugin sends.

## Related

- [[GhostCompilerPluginRegistrar]]: reads the configuration value stored here.
- [[GhostCompilerPluginConstants]]: defines the option name, description and key.
- [[GhostCompilerPluginSupport]]: the Gradle class that passes `enabled` to the compiler.
- [[GhostExtension]]: the `ghost { autoRegistration }` setting behind the value.

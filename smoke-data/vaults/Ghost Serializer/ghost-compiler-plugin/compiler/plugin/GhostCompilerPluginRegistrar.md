---
tags: [ghost-serializer, compiler-plugin, registrar]
source: ghost-compiler-plugin/src/main/kotlin/com/ghost/serialization/compiler/plugin/GhostCompilerPluginRegistrar.kt
kind: class
---

# GhostCompilerPluginRegistrar

The entry point the Kotlin compiler loads to install Ghost's compiler plugin.

## What it is about

A Kotlin compiler plugin needs a registrar, a class the compiler discovers and asks to register its extensions. This is it for Ghost. It is tiny: it decides whether the plugin is enabled and, if so, registers the single extension that links models to their serializers. It lives in `ghost-compiler-plugin`.

## How it works

When the compiler calls `registerExtensions` the registrar reads `KEY_ENABLED` from the compiler configuration, defaulting to `true`. If the option was set to `false` through the Gradle `autoRegistration` setting, it returns without registering anything, so the build behaves as if the plugin were absent. Otherwise it creates a [[GhostSerializerLinkExtension]], giving it the configuration's message collector for warnings, and registers it through [[GhostCompilerCompat]].

`supportsK2` is `true`, meaning the plugin works with the K2 compiler frontend.

The class carries a compatibility detail in a comment: `pluginId` is intentionally not marked `override`. Kotlin 2.4 added it as an abstract member of the registrar, but 2.2.21 (the compile target) does not declare it. A plain property still emits `getPluginId()`, which satisfies the 2.4 abstract method at runtime, so one jar works on both.

## The code

```kotlin
override fun ExtensionStorage.registerExtensions(
    configuration: CompilerConfiguration
) {
    val isEnabled = configuration.get(C.KEY_ENABLED, true)
    if (!isEnabled) return
    GhostCompilerCompat.registerIrGenerationExtension(
        storage = this,
        extension = GhostSerializerLinkExtension(messageCollector = configuration.messageCollector)
    )
}
```

## Related

- [[GhostCommandLineProcessor]]: parses the option that this class reads from the configuration.
- [[GhostSerializerLinkExtension]]: the extension it installs.
- [[GhostCompilerCompat]]: handles registration across compiler versions.
- [[GhostCompilerPluginConstants]]: provides the plugin id and the enabled key.
- [[GhostCompilerPluginSupport]]: the Gradle plugin that activates the compiler plugin and sets the option.

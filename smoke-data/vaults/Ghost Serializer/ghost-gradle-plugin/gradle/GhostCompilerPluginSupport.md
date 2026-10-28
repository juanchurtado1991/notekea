---
tags: [ghost-serializer, gradle-plugin, compiler-plugin, native, wasm]
source: ghost-gradle-plugin/src/main/kotlin/com/ghost/gradle/GhostCompilerPluginSupport.kt
kind: class
---

# GhostCompilerPluginSupport

The Gradle-side half of the compiler plugin: it applies `ghost-compiler-plugin` to Native and Wasm compilations.

## What it is about

The compiler plugin that links models to their serializers is only useful on Kotlin/Native and Kotlin/Wasm, where ServiceLoader discovery does not exist. This class, a `KotlinCompilerPluginSupportPlugin`, tells Kotlin's Gradle plugin which compilations should receive it. JVM and Android compilations are never touched, so their bytecode is unchanged. [[GhostPlugin]] applies it when the KMP plugin is present.

## How it works

- `isApplicable` returns true only when the compilation's platform type is `native` or `wasm`.
- `getPluginArtifact` points to `com.ghostserializer:ghost-compiler-plugin`, and `getCompilerPluginId` returns `com.ghostserializer.ghost`, the same id the compiler plugin uses.
- `applyToCompilation` maps the extension's `autoRegistration` property to a single `SubpluginOption` called `enabled`. The mapping is lazy, so the value is read after the `ghost { }` block has run.
- `apply` compares the project's Kotlin version against a list of versions the plugin is tested with in CI. For any other version it still applies the plugin but logs a warning that suggests `ghost { autoRegistration = false }` and `Ghost.addRegistry` if native or wasm compilation fails, since compiler APIs are not stable across Kotlin releases.

## The code

```kotlin
override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
): Provider<List<SubpluginOption>> {
    val extension = kotlinCompilation.target.project.extensions.getByType(GhostExtension::class.java)
    return extension.autoRegistration.map { isEnabled ->
        listOf(
            SubpluginOption(
                key = OPTION_ENABLED,
                value = isEnabled.toString()
            )
        )
    }
}
```

## Related

- [[GhostPlugin]]: applies this plugin for KMP projects.
- [[GhostExtension]]: provides the `autoRegistration` setting.
- [[GhostCommandLineProcessor]]: receives the `enabled` option inside the compiler.
- [[GhostCompilerPluginRegistrar]]: the compiler entry point loaded from the plugin artifact.
- [[GhostCompilerPluginConstants]]: holds the matching plugin id on the compiler side.

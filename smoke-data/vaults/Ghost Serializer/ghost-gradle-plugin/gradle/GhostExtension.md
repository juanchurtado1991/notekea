---
tags: [ghost-serializer, gradle-plugin, configuration]
source: ghost-gradle-plugin/src/main/kotlin/com/ghost/gradle/GhostExtension.kt
kind: interface
---

# GhostExtension

The Gradle extension interface behind the `ghost { }` block in a build script.

## What it is about

Users configure the Ghost Gradle plugin through a `ghost` block. This interface declares what can be set there. It is registered by [[GhostPlugin]] and read by the other classes of `ghost-gradle-plugin`. It contains no logic, only four properties.

## How it works

Each setting is a Gradle `Property`, which is lazy: its value is read later, after the build script has finished configuring the project, so the order of statements in the script does not matter. Gradle generates the implementation of the interface at runtime when the plugin calls `project.extensions.create`, which is why it can stay an interface with no class body. The defaults are set as conventions by the plugin, not here.

The properties are:

- `autoRegistration`: link each `@GhostSerialization` class to its generated serializer on Kotlin/Native and Kotlin/Wasm through the compiler plugin, so `Ghost.addRegistry` is not needed there. Default `true`.
- `autoInjectKtor`: add `ghost-ktor` when a Ktor client dependency is found. Default `true`.
- `autoInjectRetrofit`: add `ghost-retrofit` when Retrofit is found. Default `true`.
- `version`: the version of all Ghost artifacts (runtime, API, compiler, adapters). Defaults to the plugin release.

## The code

```kotlin
interface GhostExtension {
    val autoInjectKtor: Property<Boolean>
    val autoInjectRetrofit: Property<Boolean>
    val autoRegistration: Property<Boolean>
    val version: Property<String>
}
```

A typical use is `ghost { autoRegistration = false }` to opt out of the compiler plugin, a step the plugin's own warning recommends when a Kotlin version is untested.

## Related

- [[GhostPlugin]]: creates the extension and sets its defaults.
- [[GhostNetworkAutoInjector]]: reads the two auto-inject switches and the version.
- [[GhostCompilerPluginSupport]]: reads `autoRegistration` to build the compiler option.
- [[GhostCommandLineProcessor]]: receives the resulting `enabled` value in the compiler.

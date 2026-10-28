---
tags: [ghost-serializer, gradle-plugin, ksp, wiring]
source: ghost-gradle-plugin/src/main/kotlin/com/ghost/gradle/GhostPlugin.kt
kind: class
---

# GhostPlugin

The Gradle plugin (id `com.ghostserializer.ghost`) that sets a project up for Ghost: dependencies, KSP and extras.

## What it is about

Using Ghost normally needs several steps: add the runtime and API artifacts, add the KSP compiler, and on some platforms add adapters or the compiler plugin. `GhostPlugin` does all of that when applied, for Android, JVM and Kotlin Multiplatform (KMP) projects. It is the main class of `ghost-gradle-plugin`.

## How it works

`apply` creates the `ghost` extension, with conventions of `true` for `autoInjectKtor`, `autoInjectRetrofit` and `autoRegistration`, and a default version. It then wires KSP, hands network adapters to [[GhostNetworkAutoInjector]], and, once the KMP plugin is present, applies [[GhostCompilerPluginSupport]].

The wiring is reactive: it uses `plugins.withId` listeners, so it works whichever order the KSP, KMP and Android plugins are applied. A flag guarantees the KSP dependency is added only once, and it is added only when the KSP plugin is present.

- For **KMP**, the core runtime and API go into `commonMainImplementation`, and KSP wiring is delegated to [[GhostKmpKspWiring]].
- For **Android or JVM**, the runtime and API go into `implementation`, and the compiler goes into the `ksp` configuration.

Dependency coordinates are built from a lazy `Provider` of the version, so they follow the `ghost { version }` setting. KMP-specific classes are kept in separate files because they mention Kotlin Gradle plugin types that do not exist on JVM-only classpaths.

## The code

```kotlin
override fun apply(project: Project) {
    val extension = createExtension(project = project)
    wireKspAutoConfiguration(
        project = project,
        ghostVersion = extension.version
    )
    GhostNetworkAutoInjector.wireNetworkAutoInjection(
        project = project,
        extension = extension
    )
    project.plugins.withId(PLUGIN_KMP) {
        project.plugins.apply(GhostCompilerPluginSupport::class.java)
    }
}
```

The artifacts used are `ghost-compiler`, `ghost-serialization` and `ghost-api` under group `com.ghostserializer`.

## Related

- [[GhostExtension]]: the `ghost { }` block this plugin creates.
- [[GhostKmpKspWiring]]: KSP setup for multiplatform projects.
- [[GhostNetworkAutoInjector]]: adds `ghost-ktor` or `ghost-retrofit` when detected.
- [[GhostCompilerPluginSupport]]: applies the compiler plugin for Native and Wasm.
- [[GhostSerializationProcessor]]: the KSP processor that the `ghost-compiler` dependency brings in.

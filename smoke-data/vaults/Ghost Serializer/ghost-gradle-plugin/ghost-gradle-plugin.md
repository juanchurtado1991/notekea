---
tags: [ghost-serializer, gradle-plugin, index]
---

# ghost-gradle-plugin

The Gradle plugin that adds Ghost, its KSP processor and the compiler plugin to a project in one line.

Part of [[Ghost Serializer]]. Its notes, by package:


## gradle

- [[GhostCompilerPluginSupport]]: The Gradle-side half of the compiler plugin: it applies `ghost-compiler-plugin` to Native and Wasm compilations.
- [[GhostExtension]]: The Gradle extension interface behind the `ghost { }` block in a build script.
- [[GhostKmpKspWiring]]: An internal helper that adds the Ghost KSP compiler to the right KSP configurations of a Kotlin Multiplatform project.
- [[GhostNetworkAutoInjector]]: An internal object that adds `ghost-ktor` or `ghost-retrofit` to a project when it detects the matching HTTP client.
- [[GhostPlugin]]: The Gradle plugin (id `com.ghostserializer.ghost`) that sets a project up for Ghost: dependencies, KSP and extras.

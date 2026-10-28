---
tags: [ghost-serializer, gradle-plugin, ksp, kmp]
source: ghost-gradle-plugin/src/main/kotlin/com/ghost/gradle/GhostKmpKspWiring.kt
kind: object
---

# GhostKmpKspWiring

An internal helper that adds the Ghost KSP compiler to the right KSP configurations of a Kotlin Multiplatform project.

## What it is about

In a KMP project, KSP has one configuration per target plus one for the common metadata. Adding `ghost-compiler` to all of them by hand is error-prone, and in some layouts doing so would generate every serializer twice. This object contains that decision logic. [[GhostPlugin]] calls it only after the KMP plugin is applied, because the class names Kotlin Gradle plugin types that JVM-only builds do not have.

## How it works

`wire` always adds the compiler to `kspCommonMainMetadata`. For each other target, it adds the dependency to the target's `ksp<Target>` configuration, through a lazy provider. That provider returns an empty list when `sharesCommonMetadataOutput` is true.

That condition holds when `commonMain` already includes `build/generated/ksp/metadata/commonMain/kotlin` as a source directory, a setup used when other KSP processors generate into commonMain. In that layout every target already compiles the metadata serializers, so generating per target would declare each one twice. Because the check is lazy, it sees the final source sets whatever the order of declarations in the build script.

In the shared layout, `afterEvaluate` also adds the metadata KSP resources directory to `commonMain` resources, built by the `kspCommonMainKotlinMetadata` task, since no per-target run would produce `META-INF/services` entries or R8 rules anymore.

## The code

```kotlin
private fun sharesCommonMetadataOutput(
    project: Project,
    kotlinExtension: KotlinMultiplatformExtension
): Boolean {
    val metadataOutput = project.layout.buildDirectory.dir(COMMON_METADATA_OUTPUT).get().asFile
    val commonMain = kotlinExtension.sourceSets.findByName(SOURCE_SET_COMMON_MAIN) ?: return false
    return commonMain.kotlin.srcDirs.any { it.absoluteFile == metadataOutput.absoluteFile }
}
```

## Related

- [[GhostPlugin]]: the only caller, once the KMP plugin is present.
- [[GhostExtension]]: provides the version used to build the compiler dependency.
- [[GhostSerializationProcessor]]: the processor that runs through the KSP configurations wired here.
- [[GhostCompilerPluginSupport]]: the other KMP-only piece of the Gradle plugin.

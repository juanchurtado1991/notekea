---
tags: [ghost-serializer, gradle-plugin, network, auto-injection]
source: ghost-gradle-plugin/src/main/kotlin/com/ghost/gradle/GhostNetworkAutoInjector.kt
kind: object
---

# GhostNetworkAutoInjector

An internal object that adds `ghost-ktor` or `ghost-retrofit` to a project when it detects the matching HTTP client.

## What it is about

Users who already depend on a Ktor client or on Retrofit would otherwise have to add the Ghost adapter artifact themselves. This object does it for them. It was split from [[GhostPlugin]] because it changes for another reason: supporting one more HTTP library should touch one list here and not the plugin's `apply`.

## How it works

`wireNetworkAutoInjection` registers an `afterEvaluate` hook, so dependency declarations in the build script are already known. It reads the extension version and loops over `networkAdapters`. For each adapter that is enabled and detected, it injects `com.ghostserializer:<artifact>:<version>`.

Detection looks in the `implementation`, `api` and `commonMainImplementation` configurations for a dependency with the adapter's group and a name that passes its matcher: group `io.ktor` with a name starting `ktor-client` for Ktor, and group `com.squareup.retrofit2` with the exact name `retrofit` for Retrofit. Injection goes to `commonMainImplementation` in a KMP project and `implementation` otherwise.

A private data class, `NetworkAdapter`, bundles the artifact id, the enabled flag from [[GhostExtension]], the group and a name predicate.

## The code

```kotlin
NetworkAdapter(
    artifactId = ARTIFACT_KTOR,
    isEnabled = extension.autoInjectKtor.get(),
    group = GROUP_KTOR,
    matchesName = { it.startsWith(PREFIX_KTOR_CLIENT) }
),
NetworkAdapter(
    artifactId = ARTIFACT_RETROFIT,
    isEnabled = extension.autoInjectRetrofit.get(),
    group = GROUP_RETROFIT,
    matchesName = { it == NAME_RETROFIT }
)
```

## Related

- [[GhostPlugin]]: calls `wireNetworkAutoInjection` from `apply`.
- [[GhostExtension]]: supplies the toggles and the version.
- [[GhostContentConverter]]: the main converter in the `ghost-ktor` artifact being injected.
- [[GhostConverterFactory]]: the main converter factory in the `ghost-retrofit` artifact.

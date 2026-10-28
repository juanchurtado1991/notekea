---
tags: [ghost-serializer, compiler, ksp, heuristics]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/ksp/TestSourceSetDetection.kt
kind: object
---

# TestSourceSetDetection

A small helper that guesses whether the current KSP run is compiling test sources.

## What it is about

Each module gets a generated registry named `GhostModuleRegistry_<module>`. If a project does not set a module name, the name is `Default`, and then the main and test compilations of that module would both produce `GhostModuleRegistry_Default` and clash. The processor therefore adds a `_Test` suffix in test compilations. KSP offers no stable API to ask whether a source file belongs to a test source set, so this object answers the question itself.

## How it works

`isTestCompilation` checks two things in order:

1. An explicit option `ghost.isTest`. If it is present, its value decides: the case-insensitive text `true` means test, anything else means not test. The path check is skipped entirely.
2. Otherwise, a path check over the originating files: if any file path contains a known test directory segment, the run is a test run.

The known segments are `/src/test/`, `/src/androidTest/` and `/src/testKsp/`. The matching is deliberately narrow, a plain substring test, so projects with unusual source-set names will not match and should set the option explicitly. The trade-off is simplicity over completeness: false positives are unlikely, and the escape hatch is one Gradle option.

## The code

The decision function:

```kotlin
fun isTestCompilation(
    options: Map<String, String>,
    filePaths: Iterable<String>,
): Boolean {
    options[PC.OPTION_IS_TEST]?.let { raw ->
        return raw.equals(CC.STR_TRUE, ignoreCase = true)
    }
    return filePaths.any { pathLooksLikeTestSource(filePath = it) }
}
```

And the path heuristic, which uses the markers listed in the private `testPathMarkers`:

```kotlin
fun pathLooksLikeTestSource(filePath: String): Boolean {
    return testPathMarkers.any { marker -> filePath.contains(marker) }
}
```

The processor passes `originatingFiles.map { it.filePath }`, which are the files of the classes it has processed so far. It only asks when the module name is still `Default`.

## Related

- [[GhostSerializationProcessor]]: calls it when it computes the registry class name.
- [[GhostProcessorConstants]]: defines `OPTION_IS_TEST` and the three path markers.
- [[GhostCommonConstants]]: the `"true"` literal used for the option value.
- [[GhostModuleRegistryGenerator]]: its registry class name is the thing this heuristic keeps unique.

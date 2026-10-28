---
tags: [ghost-serializer, compiler, analysis, ksp]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/TypeHelpers.kt
kind: functions
---

# TypeHelpers

Extension functions on KSP's `KSType` (and a few on the property model) that answer "what kind of type is this?" for the emitters.

## What it is about

Code generation is full of decisions that depend on a type: does this field use `nextInt`, `nextDouble` or `nextString`? Is it a list? Is it `RawJson`? This file centralizes those checks as `internal` extension functions, so the emitters stay readable (`type.isPrimitiveInt()` instead of a string comparison). It is the emitter-facing sibling of [[GhostAnalyzerTypeHelpers]], which serves the analyzers.

## How it works

Most functions compare the declaration's qualified name with a constant from [[GhostAnalyzerConstants]] (`isList`, `isMap`, `isSet`, `isString`, `isRawJson`, `isByteArray`, and the primitive checks for byte, char, short and unsigned long). A few, such as `isPrimitiveInt`, `isPrimitiveLong`, `isPrimitiveDouble`, `isPrimitiveFloat` and `isPrimitiveBoolean`, compare the type's KotlinPoet name with the non-null version, so `Int` and `Int?` both count.

The functions that do more than a comparison:

- `containsYamlIncompatibleType(isProto)` walks a type recursively, through value-class wrappers, list or set elements and map values, and says whether JSON-only features are inside: `RawJson`, a non-proto `ByteArray` or a sealed class. [[GhostYamlEligibility]] uses it.
- `serializerClassName()` computes the generated serializer's name, joining nested names with underscores and adding `Serializer`. The same rule is used when the processor names the generated file.
- `isKotlinUnsignedPrimitive` distinguishes `UInt`, `ULong`, `UShort` and `UByte` from user-defined value classes, since unsigned types are read natively and never unboxed.
- `localValueName`, `localTrackingName`, `localNameSuffix` and `toGeneratedLocalBase` build names of local variables in generated code. Underscores are dropped and the next letter capitalized, so `id_internal` gives `idInternalValue`.

## The code

The recursive YAML check:

```kotlin
internal fun KSType.containsYamlIncompatibleType(isProto: Boolean): Boolean {
    if (isRawJson()) return true
    if (!isProto && isByteArray()) return true
    val declaration = this.declaration as? KSClassDeclaration ?: return false
    if (declaration.modifiers.contains(Modifier.SEALED)) return true
    // …
    if (isList() || isSet()) {
        val inner = arguments.firstOrNull()?.type?.resolve()
        if (inner != null && inner.containsYamlIncompatibleType(isProto = isProto)) return true
    }
```

And the generated serializer name:

```kotlin
internal fun ClassName.serializerClassName(): ClassName {
    return ClassName(
        packageName,
        "${simpleNames.joinToString(CC.STR_UNDERSCORE)}${CC.STR_SERIALIZER_SUFFIX}"
    )
}
```

## Related

- [[GhostAnalyzerTypeHelpers]]: the analyzer-side helper file.
- [[GhostAnalyzerConstants]]: the qualified-name constants compared here.
- [[GhostCommonConstants]]: the underscore and `Serializer` suffix literals.
- [[GhostYamlEligibility]]: calls `containsYamlIncompatibleType`.
- [[GhostPropertyModel]]: the receiver of the local-name helpers.
- [[SerializerSetupEmitter]]: one of the codegen files that reads these checks.

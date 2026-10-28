---
tags: [ghost-serializer, compiler, analysis, codegen, defaults]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/GhostPropertyExtensions.kt
kind: functions
---

# GhostPropertyExtensions

Extension functions on [[GhostPropertyModel]] that compute the Kotlin source snippets the deserializer and serializer emitters need for each property: local variable types, initial values, return expressions and omission rules.

## What it is about

A generated deserializer reads fields into local variables, one per property, and at the end builds the object. For each property the emitter must decide the local's type, its starting value, and what expression to pass to the constructor, which depends on whether the type is nullable, primitive, a value class, or a proto3 field. Those decisions are pure functions of the model, so they live here and are shared by the "standard" and the "fragmented" (large-class) emitters.

## How it works

**Local variables.** `getVariableType` gives the local's type: primitives and non-null value classes of primitives stay unboxed, all else becomes nullable. `getInitialValue` returns the literal (`null`, `0`, `0L`, `0uL`, `0.0`, `0.0f`, `false`, `'\u0000'`). Value classes use their inner property's type.

**Return expressions.** `getReturnExpression` gives the constructor argument. Primitives pass the local as is. Nullable ones too. For other non-null types it appends `!!`, or, for a proto field with a zero value, `(local ?: zero)`. The `getFragmented...` variants do the same for `ctx.<field>` in the chunked decoder.

**Defaults.** If a field is absent, the constructor default must apply. `getDefaultValueReturnExpression` and `getSingleShotDefaultArgExpression` produce `if ((mask and bit) != 0L) parsed else default`. When the default equals the local's initial value (`defaultMatchesLocalInit`), the check is dropped. `allDefaultsHaveExpressions` tells whether the single-constructor path, with no `.copy()`, is possible.

**Wire rules.** `isRequiredOnWire` says a missing field must fail: a non-null constructor parameter with no default and no proto3 zero. The proto helpers (`protoZeroLiteral`, `omitsProtoZeroOnEncode`) implement the proto3 rule that zero values are omitted on encode, and `omitsNullOnEncode` allows skipping a null only when the default is `null`, otherwise the round trip would change.

## The code

How a return expression is chosen:

```kotlin
return when {
    isPrimitive -> varName
    isUnboxedValueClass -> {
        val unwrapped = if (valueClassProperty!!.holdsNullableOrPrimitive()) {
            varName
        } else {
            requireLocalValue(local = varName)
        }
        AC.TEMPLATE_WRAP_TYPE.format(typeName, unwrapped)
    }
    // …
```

and the proto-aware non-null access:

```kotlin
internal fun GhostPropertyModel.requireLocalValue(
    local: String
): String {
    val zeroLiteral = protoOmittedZeroLiteral() ?: return local + AC.STR_BANG_BANG
    return AC.TEMPLATE_ELVIS_PARENTHESIZED.format(local, zeroLiteral)
}
```

## Related

- [[GhostPropertyModel]]: the receiver of every function.
- [[TypeHelpers]]: provides `isPrimitive`, `localValueName` and the other type helpers used here.
- [[GhostAnalyzerConstants]]: templates and literals for the generated text.
- [[StandardEmitter]]: uses the plain variants.
- [[FragmentedEmitter]]: uses the `ctx.` variants for large classes.
- [[DeserializeCodeEmitter]]: builds the decoding body from these expressions.
- [[SerializeCodeEmitter]]: uses the omit rules when writing.

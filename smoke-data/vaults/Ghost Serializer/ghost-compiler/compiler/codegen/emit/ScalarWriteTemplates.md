---
tags: [ghost-serializer, compiler, codegen, serialize, proto]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/ScalarWriteTemplates.kt
kind: object
---

# ScalarWriteTemplates

A lookup table that maps a Kotlin scalar type to the statement the generated code uses to write it, with a different form for proto3.

## What it is about

Writing a scalar looks almost the same for every type: call `writer.value(x)`. A few types differ, and proto3 canonical JSON differs again (64-bit integers are quoted strings, `ByteArray` is Base64, non-finite floating values become `"NaN"` or `"Infinity"`). Keeping this as a table means a new scalar is one more entry, not another branch in the big type dispatch of [[BaseSerializeEmitter]].

## How it works

A private `Template` holds the `plain` statement and a `proto` statement that defaults to the plain one. The map is keyed by the type's qualified name. `templateFor(qualifiedName, isProto)` returns the proto form when requested, the plain form otherwise, and `null` when the type is not in the table, which tells the caller to try other cases (lists, maps, Ghost types, contextual serializers).

The entries: `Int`, `String`, `Boolean` and `Char` use the generic value template. `Byte` and `Short` convert with `toInt()`. `Long` and `ULong` use the same template in plain mode and a long-as-string statement in proto mode. `Double` and `Float` use a proto floating-value statement under proto. `ByteArray` writes a raw value in plain mode and Base64 in proto mode.

## The code

```kotlin
private class Template(val plain: String, val proto: String = plain)

private val templates = mapOf(
    AC.K_INT to Template(plain = C.TEMPLATE_WRITER_VALUE),
    AC.K_LONG to Template(plain = C.TEMPLATE_WRITER_VALUE, proto = C.STR_WRITER_VAL_LONG_AS_STRING),
    AC.K_DOUBLE to Template(plain = C.TEMPLATE_WRITER_VALUE, proto = C.STR_WRITER_PROTO_FLOATING_VALUE),
    AC.K_BYTE_ARRAY to Template(plain = C.STR_WRITER_RAW_VALUE_L, proto = C.STR_WRITER_VAL_BYTES_AS_BASE64),
    // …
)
```

```kotlin
fun templateFor(qualifiedName: String?, isProto: Boolean): String? {
    val template = templates[qualifiedName] ?: return null
    return if (isProto) template.proto else template.plain
}
```

The returned string is a KotlinPoet format with a `%L` slot for the accessor, used directly by `addStatement`.

## Related

- [[BaseSerializeEmitter]]: the only caller, inside `emitTypeValue`.
- [[ScalarReaderCalls]]: the equivalent table for reading.
- [[GhostEmitterConstants]]: the template strings.
- [[GhostAnalyzerConstants]]: the qualified-name constants used as keys.

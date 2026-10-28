---
tags: [ghost-serializer, compiler, codegen, naming]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/HeaderConstantNames.kt
kind: object
---

# HeaderConstantNames

An object that chooses collision-free names for the cached `H_*` and `HS_*` header constants of a generated serializer.

## What it is about

A generated serializer precomputes, for every JSON field name, the bytes (and for the text channel, the string) of the `"name":` header so that writing a field is a single copy. Each constant needs a Kotlin identifier, and JSON names can contain characters that are not legal there. `HeaderConstantNames` is the single place that maps a JSON name to the identifier suffix, so the declaration site and every write site agree.

## How it works

For each name, `suffixFor` first builds the "historical" form: dots replaced by `_` and the text upper-cased (`requestId` becomes `REQUESTID`). If every character is an ASCII letter, digit, `_` or `.`, that form is kept, so existing output does not change. Otherwise every other character (hyphen, space, non-ASCII) becomes `_`, and the hex of the name's `hashCode` is appended so two different odd names do not merge.

`allocate` works on the whole set of names of a class. It sorts the distinct names, computes each suffix and, when a suffix is already taken (`id` and `ID`, or `a.b` and `a_b`), appends `_` plus the name's hash. In the unlikely case that is also taken it keeps appending `_`. Because the result depends only on the full sorted name set, the later of two colliding names in sorted order is the one that gets the hash.

## The code

The allocation loop is the heart of the file:

```kotlin
for (name in jsonNames.distinct().sorted()) {
    var suffix = suffixFor(jsonName = name)
    if (!taken.add(suffix)) {
        suffix += CC.STR_UNDERSCORE + hashSuffix(jsonName = name)
        while (!taken.add(suffix)) {
            suffix += CC.STR_UNDERSCORE
        }
    }
    suffixes[name] = suffix
}
```

`hashSuffix` converts `hashCode()` to an unsigned value and prints it in upper-case base 16, which avoids a minus sign in the identifier. `isPlainNameChar` is the ASCII test: `char.code < 128 && char.isLetterOrDigit()`, or `_`, or `.`. Using `isLetterOrDigit` alone would admit non-ASCII letters, hence the explicit limit.

[[SerializerSetupEmitter]] uses `allocate` to declare the constants, and [[BaseSerializeEmitter]] uses the same mapping when it writes a field, which is why the doc stresses that both must call it with the class's whole name set.

## Related

- [[SerializerSetupEmitter]]: declares the `H_*` and `HS_*` properties using these suffixes.
- [[BaseSerializeEmitter]]: references the constants when writing fields.
- [[JsonStringEscaping]]: escapes the same names inside the header text.
- [[GhostCommonConstants]]: supplies the string constants used here.

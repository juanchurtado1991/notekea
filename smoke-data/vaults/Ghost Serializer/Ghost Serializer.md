---
tags: [ghost-serializer, index]
---

# Ghost Serializer

The fastest JSON path for Kotlin Multiplatform: compile-time generated serializers, SWAR parsing and zero-allocation
hot paths, with YAML and Protobuf channels and drop-in integrations for Ktor, Retrofit and Spring Boot.

This vault explains the repository's production code, one note per Kotlin file: what it is about, how it works and
a walk through its code, with links to the files it uses and the ones that use it. It is test data for Notekea
(`smoke-data/README.md`): the kind of vault a code repository becomes (`ROADMAP.md`, after the MVP, Repositories).

## Modules

- [[ghost-api]]: The annotations and small public types a user puts on their DTOs: what to serialize, how to name fields, how strict to be. (22 notes)
- [[ghost-compiler]]: The KSP processor that reads annotated classes at compile time and writes a fast, allocation-free serializer for each. (56 notes)
- [[ghost-compiler-plugin]]: The Kotlin compiler plugin side, which wires generated serializers into the user's build. (5 notes)
- [[ghost-gradle-plugin]]: The Gradle plugin that adds Ghost, its KSP processor and the compiler plugin to a project in one line. (5 notes)
- [[ghost-ktor]]: The Ktor integration: content negotiation that sends and receives DTOs through Ghost. (10 notes)
- [[ghost-retrofit]]: The Retrofit integration: a converter factory that encodes requests and decodes responses with Ghost. (5 notes)
- [[ghost-serialization]]: The runtime: readers and writers for JSON (bytes, strings, streaming), YAML and Protobuf, the built-in serializers and the registry that finds generated ones. (245 notes)
- [[ghost-spring-boot-starter]]: The Spring Boot starter: an HTTP message converter and auto-configuration that put Ghost in front of Jackson for annotated types. (14 notes)

## Where to start

- [[Ghost]]: the entry point an application calls to encode and decode.
- [[GhostJsonFlatReader]]: the byte-array JSON reader, the fastest decoding channel.
- [[GhostJsonWriter]]: the byte writer that encodes to JSON.
- [[PerfectHashFinder]]: how generated code finds a field from its name without a map.

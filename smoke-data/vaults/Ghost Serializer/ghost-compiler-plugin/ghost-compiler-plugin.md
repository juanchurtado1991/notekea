---
tags: [ghost-serializer, compiler-plugin, index]
---

# ghost-compiler-plugin

The Kotlin compiler plugin side, which wires generated serializers into the user's build.

Part of [[Ghost Serializer]]. Its notes, by package:


## compiler/plugin

- [[GhostCommandLineProcessor]]: Parses the single option of the Ghost compiler plugin, `enabled`, and stores it in the compiler configuration.
- [[GhostCompilerCompat]]: An internal object that hides differences between Kotlin compiler versions, so one plugin artifact works on several of them.
- [[GhostCompilerPluginConstants]]: An internal object that gathers the identifiers and messages shared by the parts of the Ghost compiler plugin.
- [[GhostCompilerPluginRegistrar]]: The entry point the Kotlin compiler loads to install Ghost's compiler plugin.
- [[GhostSerializerLinkExtension]]: The IR extension that does the plugin's real work: it attaches a link annotation to every Ghost model class.

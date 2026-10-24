package app.notekea.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.type.KoTypeDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoObjectDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * Turns the rules of `ARCHITECTURE.md` into failing tests, so layer boundaries and naming conventions hold no matter
 * who (or which AI agent) writes the code. Layer rules run on production code; file-structure rules run on all the
 * code of both modules, tests included. Rules pass while a category is empty.
 */
class ArchitectureTest {
    private val allCodeScope = Konsist.scopeFromModule(moduleName = MODULE_NAME) +
        Konsist.scopeFromModule(moduleName = KTLINT_RULES_MODULE_NAME)
    private val productionScope = Konsist.scopeFromProduction(moduleName = MODULE_NAME)

    private val designSystemOnlyImports = listOf(
        "AlertDialog",
        "AssistChip",
        "Badge",
        "BadgedBox",
        "BasicAlertDialog",
        "Button",
        "Card",
        "Checkbox",
        "DropdownMenu",
        "DropdownMenuItem",
        "ElevatedButton",
        "ElevatedCard",
        "ExtendedFloatingActionButton",
        "FilledIconButton",
        "FilledTonalButton",
        "FilterChip",
        "FloatingActionButton",
        "IconButton",
        "InputChip",
        "LeadingIconTab",
        "ListItem",
        "ModalBottomSheet",
        "NavigationRail",
        "NavigationRailItem",
        "OutlinedButton",
        "OutlinedCard",
        "OutlinedTextField",
        "PrimaryTabRow",
        "RadioButton",
        "ScrollableTabRow",
        "SecondaryTabRow",
        "Slider",
        "Snackbar",
        "SuggestionChip",
        "Switch",
        "Tab",
        "TabRow",
        "TextButton",
        "TextField",
        "TooltipBox"
    ).map { "$MATERIAL3_PACKAGE.$it" }

    private val forbiddenDomainImportPrefixes = listOf(
        "androidx.",
        "de.kherud.",
        "io.ktor.",
        "java.io.",
        "java.nio.",
        "okio.",
        "org.apache.lucene.",
        "org.eclipse.jgit.",
        "org.jetbrains.compose.",
        "org.koin."
    )

    @Test
    fun `domain does not depend on other layers or on frameworks`() {
        filesInLayer(layer = DOMAIN_LAYER).assertFalse(additionalMessage = "ARCHITECTURE.md §1") { file ->
            file.imports.any { import ->
                val isOtherLayer = importsLayer(
                    importName = import.name,
                    layers = listOf(DATA_LAYER, PRESENTATION_LAYER, DI_LAYER)
                )
                isOtherLayer || forbiddenDomainImportPrefixes.any { import.name.startsWith(prefix = it) }
            }
        }
    }

    @Test
    fun `domain errors are sealed interfaces in domain`() {
        productionScope
            .interfaces()
            .withNameEndingWith("Error")
            .assertTrue(additionalMessage = "ARCHITECTURE.md §3") {
                it.hasSealedModifier && it.resideInPackage(name = "..$DOMAIN_LAYER..")
            }
    }

    @Test
    fun `dispatchers are injected instead of referenced directly`() {
        val dispatcherReference = Regex(pattern = DIRECT_DISPATCHER_PATTERN)
        productionScope.files
            .filterNot { it.name == DISPATCHERS_IMPLEMENTATION }
            .assertFalse(additionalMessage = "ARCHITECTURE.md §5: inject AppDispatchers") {
                dispatcherReference.containsMatchIn(input = it.text)
            }
    }

    @Test
    fun `dtos live in the data layer`() {
        productionScope
            .classes()
            .withNameEndingWith("Dto")
            .assertTrue(additionalMessage = "ARCHITECTURE.md §2") { it.resideInPackage(name = "..$DATA_LAYER..") }
    }

    @Test
    fun `reusable material components are only used inside the design system`() {
        productionScope.files
            .filterNot { file -> DESIGN_SYSTEM_PACKAGES.any { file.packagee?.name?.startsWith(prefix = it) == true } }
            .assertFalse(
                additionalMessage = "ARCHITECTURE.md §11: use or add a component in core.presentation.components"
            ) { file ->
                file.imports.any {
                    val isMaterialWildcard = it.text.trim().endsWith(suffix = "$MATERIAL3_PACKAGE.*")
                    isMaterialWildcard || it.name in designSystemOnlyImports
                }
            }
    }

    @Test
    fun `every UI composable has a preview in its file`() {
        productionScope.files
            .filterNot { file -> PREVIEW_EXEMPT_PACKAGES.any { file.packagee?.name?.startsWith(prefix = it) == true } }
            .assertTrue(additionalMessage = "GUIDELINES.md §6: add a @Preview named <Composable>…Preview") { file ->
                val functions = file.functions(includeNested = false, includeLocal = false)
                val previews = functions.filter { it.hasAnnotationWithName(PREVIEW_ANNOTATION) }
                functions
                    .filter { it.hasAnnotationWithName(COMPOSABLE_ANNOTATION) && !it.hasPrivateModifier }
                    .filterNot { it.hasAnnotationWithName(PREVIEW_ANNOTATION) }
                    .all { composable -> previews.any { it.name.startsWith(prefix = composable.name) } }
            }
    }

    @Test
    fun `hard-coded colors only live in the theme`() {
        productionScope.files
            .filterNot { it.packagee?.name?.startsWith(prefix = THEME_PACKAGE) == true }
            .assertFalse(additionalMessage = "ARCHITECTURE.md §1: use theme tokens") {
                it.text.contains(other = COLOR_LITERAL)
            }
    }

    @Test
    fun `implementations are not named Impl`() {
        productionScope
            .classes()
            .assertFalse(additionalMessage = "ARCHITECTURE.md §2: name it after the technology") {
                it.name.endsWith(suffix = "Impl")
            }
    }

    @Test
    fun `interfaces are fully abstract`() {
        allCodeScope
            .interfaces()
            .assertFalse(
                additionalMessage = "GUIDELINES.md §1: move shared behavior to an abstract class"
            ) { declaration ->
                val hasFunctionBody = declaration.functions(includeNested = false).any {
                    it.hasBlockBody || it.hasExpressionBody
                }
                val hasPropertyGetter = declaration.properties(includeNested = false).any { it.hasGetter }
                hasFunctionBody || hasPropertyGetter
            }
    }

    @Test
    fun `interfaces are not extended with extension functions or properties`() {
        val interfaceNames = allCodeScope.interfaces().map { it.name }.toSet()
        val extendedTypeName: (KoTypeDeclaration?) -> String? = { type ->
            type?.name?.substringBefore(delimiter = '<')?.removeSuffix(suffix = "?")
        }
        allCodeScope
            .functions(includeNested = true, includeLocal = true)
            .assertFalse(additionalMessage = "GUIDELINES.md §1: declare it in the interface or a class") {
                extendedTypeName(it.receiverType) in interfaceNames
            }
        allCodeScope
            .properties(includeNested = true)
            .assertFalse(additionalMessage = "GUIDELINES.md §1: declare it in the interface or a class") {
                extendedTypeName(it.receiverType) in interfaceNames
            }
    }

    @Test
    fun `koin is only used through constructor injection`() {
        productionScope.files.assertFalse(additionalMessage = "ARCHITECTURE.md §6") { file ->
            file.imports.any { it.name == KOIN_COMPONENT_IMPORT }
        }
    }

    @Test
    fun `only the system environment reads the operating system name`() {
        productionScope.files
            .filterNot { it.name == SYSTEM_ENVIRONMENT_IMPLEMENTATION }
            .assertFalse(additionalMessage = "PLATFORM_GUIDELINES.md: read PlatformConventions instead") {
                it.text.contains(other = OS_NAME_PROPERTY)
            }
    }

    @Test
    fun `presentation does not depend on data`() {
        filesInLayer(layer = PRESENTATION_LAYER).assertFalse(additionalMessage = "ARCHITECTURE.md §1") { file ->
            file.imports.any {
                importsLayer(
                    importName = it.name,
                    layers = listOf(DATA_LAYER)
                )
            }
        }
    }

    @Test
    fun `system clock is only read in dependency injection`() {
        productionScope.files
            .filterNot { isInLayer(file = it, layer = DI_LAYER) }
            .assertFalse(additionalMessage = "ARCHITECTURE.md §5: inject kotlin.time.Clock") {
                it.text.contains(other = SYSTEM_CLOCK)
            }
    }

    @Test
    fun `use cases live in domain and expose a single operator invoke`() {
        productionScope
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue(additionalMessage = "ARCHITECTURE.md §2") { useCase ->
                val invokeFunctions = useCase.functions().filter { it.name == "invoke" }
                val hasSingleOperatorInvoke = invokeFunctions.size == 1 && invokeFunctions.first().hasOperatorModifier
                hasSingleOperatorInvoke && useCase.resideInPackage(name = "..$DOMAIN_LAYER..")
            }
    }

    @Test
    fun `view models extend BaseViewModel and live in presentation`() {
        productionScope
            .classes()
            .withNameEndingWith("ViewModel")
            .filterNot { it.name == BASE_VIEW_MODEL }
            .assertTrue(additionalMessage = "ARCHITECTURE.md §4") { viewModel ->
                val parentNames = viewModel.parents().map { it.name.substringBefore(delimiter = '<') }
                val extendsBase = BASE_VIEW_MODEL in parentNames
                extendsBase && viewModel.resideInPackage(name = "..$PRESENTATION_LAYER..")
            }
    }

    @Test
    fun `each file declares at most one top-level type`() {
        productionScope.files.assertTrue(
            additionalMessage = "GUIDELINES.md §2: one class, interface or object per file"
        ) {
            topLevelTypeCount(file = it) <= 1
        }
    }

    @Test
    fun `companion objects are the last declaration of their class`() {
        allCodeScope
            .classes()
            .filter { koClass -> koClass.objects(includeNested = false).any { it.hasCompanionModifier } }
            .assertTrue(additionalMessage = "GUIDELINES.md §2: move the companion object to the end of the class") {
                val lastDeclaration = it.declarations(includeNested = false, includeLocal = false).lastOrNull()
                lastDeclaration is KoObjectDeclaration && lastDeclaration.hasCompanionModifier
            }
    }

    @Test
    fun `constants live in the companion object of the file's class`() {
        allCodeScope.files
            .filter { topLevelTypeCount(file = it) > 0 }
            .assertFalse(additionalMessage = "GUIDELINES.md §2: move constants into the companion object") {
                it.properties(includeNested = false).isNotEmpty()
            }
    }

    @Test
    fun `utility functions do not share a file with a type`() {
        productionScope.files
            .filter { topLevelTypeCount(file = it) > 0 }
            .assertFalse(additionalMessage = "GUIDELINES.md §2: move utilities to their own file") { file ->
                file.functions(includeNested = false, includeLocal = false).any { !it.hasPrivateModifier }
            }
    }

    private fun topLevelTypeCount(
        file: KoFileDeclaration
    ): Int = file.classes(includeNested = false).size +
        file.interfaces(includeNested = false).size +
        file.objects(includeNested = false).size

    private fun filesInLayer(
        layer: String
    ): List<KoFileDeclaration> = productionScope.files.filter {
        isInLayer(
            file = it,
            layer = layer
        )
    }

    private fun importsLayer(
        importName: String,
        layers: List<String>
    ): Boolean = layers.any { importName.split(".").contains(element = it) }

    private fun isInLayer(
        file: KoFileDeclaration,
        layer: String
    ): Boolean = file.packagee
        ?.name
        ?.split(".")
        ?.contains(element = layer) == true

    companion object {
        private const val BASE_VIEW_MODEL = "BaseViewModel"
        private const val COLOR_LITERAL = "Color(0x"
        private const val DATA_LAYER = "data"
        private const val DI_LAYER = "di"
        private const val DIRECT_DISPATCHER_PATTERN = "Dispatchers\\.(IO|Default|Main|Unconfined)"
        private const val DISPATCHERS_IMPLEMENTATION = "JvmAppDispatchers"
        private const val DOMAIN_LAYER = "domain"
        private const val KTLINT_RULES_MODULE_NAME = "ktlint-rules"
        private const val KOIN_COMPONENT_IMPORT = "org.koin.core.component.KoinComponent"
        private const val MODULE_NAME = "composeApp"
        private const val OS_NAME_PROPERTY = "\"os.name\""
        private const val PRESENTATION_LAYER = "presentation"
        private const val SYSTEM_CLOCK = "Clock.System"
        private const val SYSTEM_ENVIRONMENT_IMPLEMENTATION = "JvmSystemEnvironment"
        private const val COMPOSABLE_ANNOTATION = "Composable"
        private const val MATERIAL3_PACKAGE = "androidx.compose.material3"
        private const val PREVIEW_ANNOTATION = "Preview"
        private const val THEME_PACKAGE = "app.notekea.core.presentation.theme"
        private val DESIGN_SYSTEM_PACKAGES = listOf("app.notekea.core.presentation.components", THEME_PACKAGE)

        // Theme functions, the preview helper and the developer gallery are tooling, not UI elements.
        private val PREVIEW_EXEMPT_PACKAGES = listOf(
            THEME_PACKAGE,
            "app.notekea.core.presentation.gallery",
            "app.notekea.core.presentation.preview"
        )
    }
}

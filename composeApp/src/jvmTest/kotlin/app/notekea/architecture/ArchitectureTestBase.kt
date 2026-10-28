package app.notekea.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

/**
 * Turns the rules of `ARCHITECTURE.md` into failing tests, so layer boundaries and naming conventions hold no matter
 * who (or which AI agent) writes the code. Layer rules run on production code; file-structure rules run on all the
 * code of both modules, tests included. Rules pass while a category is empty.
 */
abstract class ArchitectureTestBase {
    protected val allCodeScope = Konsist.scopeFromModule(moduleName = MODULE_NAME) +
        Konsist.scopeFromModule(moduleName = KTLINT_RULES_MODULE_NAME)
    protected val productionScope = Konsist.scopeFromProduction(moduleName = MODULE_NAME)

    protected val designSystemOnlyImports = listOf(
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

    protected val forbiddenDomainImportPrefixes = listOf(
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

    protected fun topLevelTypeCount(
        file: KoFileDeclaration
    ): Int = file.classes(includeNested = false).size +
        file.interfaces(includeNested = false).size +
        file.objects(includeNested = false).size

    /** The feature a package belongs to (`vault`, `notes`…), or null for core, the app root and generated code. */
    protected fun featureOf(
        packageName: String?
    ): String? {
        val segments = packageName?.removePrefix(prefix = APP_PACKAGE_PREFIX)?.takeIf { it != packageName }?.split(".")
        return segments?.takeIf { it.size > 1 }?.first()?.takeUnless { it in NON_FEATURE_PACKAGES }
    }

    protected fun filesInLayer(
        layer: String
    ): List<KoFileDeclaration> = productionScope.files.filter {
        isInLayer(
            file = it,
            layer = layer
        )
    }

    protected fun importsLayer(
        importName: String,
        layers: List<String>
    ): Boolean = layers.any { importName.split(".").contains(element = it) }

    protected fun isInLayer(
        file: KoFileDeclaration,
        layer: String
    ): Boolean = file.packagee
        ?.name
        ?.split(".")
        ?.contains(element = layer) == true

    companion object {
        const val APP_PACKAGE_PREFIX = "app.notekea."
        const val BASE_VIEW_MODEL = "BaseViewModel"
        const val COLOR_LITERAL = "Color(0x"
        const val DATA_LAYER = "data"
        const val DI_LAYER = "di"
        const val DIRECT_DISPATCHER_PATTERN = "Dispatchers\\.(IO|Default|Main|Unconfined)"
        const val DISPATCHERS_IMPLEMENTATION = "JvmAppDispatchers"
        const val DOMAIN_LAYER = "domain"
        const val ICON_LIBRARY_PACKAGE = "com.composables.icons"
        const val ICONS_FILE = "NotekeaIcons"
        const val KTLINT_RULES_MODULE_NAME = "ktlint-rules"
        const val KOIN_COMPONENT_IMPORT = "org.koin.core.component.KoinComponent"
        const val MODULE_NAME = "composeApp"
        const val OS_NAME_PROPERTY = "\"os.name\""
        const val PRESENTATION_LAYER = "presentation"
        const val SYSTEM_CLOCK = "Clock.System"
        const val SYSTEM_ENVIRONMENT_IMPLEMENTATION = "JvmSystemEnvironment"
        const val COMPOSABLE_ANNOTATION = "Composable"
        const val MATERIAL3_PACKAGE = "androidx.compose.material3"
        const val PREVIEW_ANNOTATION = "Preview"
        const val FOLLOWER_PREFIX = "Follow"
        const val THEME_PACKAGE = "app.notekea.core.presentation.theme"
        val NON_FEATURE_PACKAGES = setOf("architecture", "core", "db", "di", "resources")
        val DESIGN_SYSTEM_PACKAGES = listOf("app.notekea.core.presentation.components", THEME_PACKAGE)

        // Theme functions, the preview helper and the developer gallery are tooling, not UI elements.
        val PREVIEW_EXEMPT_PACKAGES = listOf(
            THEME_PACKAGE,
            "app.notekea.core.presentation.gallery",
            "app.notekea.core.presentation.preview"
        )
    }
}

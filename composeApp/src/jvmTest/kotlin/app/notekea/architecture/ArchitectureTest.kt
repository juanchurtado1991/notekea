package app.notekea.architecture

import com.lemonappdev.konsist.api.declaration.type.KoTypeDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * Turns the rules of `ARCHITECTURE.md` into failing tests, so layer boundaries and naming conventions hold no matter
 * who (or which AI agent) writes the code. Layer rules run on production code; file-structure rules run on all the
 * code of both modules, tests included. Rules pass while a category is empty.
 */
class ArchitectureTest : ArchitectureTestBase() {
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
    fun `features do not depend on each other`() {
        productionScope.files
            .filter { featureOf(packageName = it.packagee?.name) != null }
            .assertFalse(
                additionalMessage = "ARCHITECTURE.md §1: share through core, or let the app compose the features"
            ) { file ->
                val feature = featureOf(packageName = file.packagee?.name)
                file.imports.any { import -> featureOf(packageName = import.name).let { it != null && it != feature } }
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
}

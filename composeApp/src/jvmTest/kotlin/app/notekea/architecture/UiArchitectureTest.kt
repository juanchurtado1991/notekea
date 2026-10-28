package app.notekea.architecture

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/** The rules of the UI */
class UiArchitectureTest : ArchitectureTestBase() {

    @Test
    fun `icons come from NotekeaIcons, never from the icon library`() {
        productionScope.files
            .filterNot { it.name == ICONS_FILE }
            .assertFalse(
                additionalMessage = "LIBRARIES.md §2: add the icon to NotekeaIcons and use it from there"
            ) { file ->
                file.imports.any { it.name.startsWith(prefix = ICON_LIBRARY_PACKAGE) }
            }
    }

    @Test
    fun `every UI composable has a preview in its file`() {
        productionScope.files
            .filterNot { file -> PREVIEW_EXEMPT_PACKAGES.any { file.packagee?.name?.startsWith(prefix = it) == true } }
            .assertTrue(additionalMessage = "GUIDELINES.md §6: add a @Preview named <Composable>…Preview") { file ->
                val functions = file.functions(includeNested = false, includeLocal = false)
                val previews = functions.filter { it.hasAnnotationWithName(PREVIEW_ANNOTATION) }
                // An actual's preview is its expect's, in the common file; a Follow… composable draws nothing.
                functions
                    .filter { it.hasAnnotationWithName(COMPOSABLE_ANNOTATION) && !it.hasPrivateModifier }
                    .filterNot { it.hasActualModifier }
                    .filterNot { it.name.startsWith(prefix = FOLLOWER_PREFIX) }
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
}

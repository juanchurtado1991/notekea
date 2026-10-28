package app.notekea.architecture

import com.lemonappdev.konsist.api.declaration.KoObjectDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/** The rules of how code is laid out in files */
class FileStructureTest : ArchitectureTestBase() {

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

    @Test
    fun `each file declares at most one top-level type`() {
        productionScope.files.assertTrue(
            additionalMessage = "GUIDELINES.md §2: one class, interface or object per file"
        ) {
            topLevelTypeCount(file = it) <= 1
        }
    }
}

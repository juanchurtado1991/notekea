package app.notekea.core.presentation.menu

import app.notekea.core.platform.domain.FakeSystemEnvironment
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.resources.Res
import app.notekea.resources.menu_about
import app.notekea.resources.menu_edit
import app.notekea.resources.menu_exit
import app.notekea.resources.menu_file
import app.notekea.resources.menu_help
import app.notekea.resources.menu_settings
import app.notekea.resources.menu_shortcuts
import app.notekea.resources.menu_view
import app.notekea.resources.menu_window
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The menu bar on each system (`PLATFORM_GUIDELINES.md` §4). */
class AppMenusTest {
    private val linux = AppMenus.of(conventions = conventions(system = "Linux"))
    private val macos = AppMenus.of(conventions = conventions(system = "Mac OS X"))

    @Test
    fun `every system has File, Edit, View, Window and Help, in that order`() {
        val titles = listOf(
            Res.string.menu_file,
            Res.string.menu_edit,
            Res.string.menu_view,
            Res.string.menu_window,
            Res.string.menu_help
        )

        assertEquals(
            expected = titles to titles,
            actual = linux.map { it.title } to macos.map { it.title }
        )
    }

    @Test
    fun `File holds Settings and Exit, and Help holds About, where there is no app menu`() {
        assertTrue(actual = Res.string.menu_settings in labels(menus = linux))
        assertTrue(actual = Res.string.menu_exit in labels(menus = linux))
        assertEquals(
            expected = listOf(Res.string.menu_about),
            actual = labels(menus = linux.takeLast(n = 1))
        )
    }

    @Test
    fun `on macOS the app menu has them, and Help opens the shortcuts`() {
        assertFalse(actual = Res.string.menu_settings in labels(menus = macos))
        assertFalse(actual = Res.string.menu_exit in labels(menus = macos))
        assertEquals(
            expected = listOf(AppMenuEntry.OpensDialog(Res.string.menu_shortcuts, WindowDialog.ABOUT)),
            actual = macos.last().entries
        )
    }

    @Test
    fun `no shortcut is offered twice, so each runs one command`() {
        val shortcuts = linux.flatMap { it.entries }.filterIsInstance<AppMenuEntry.Command>().map { it.shortcut }

        assertEquals(
            expected = shortcuts,
            actual = shortcuts.distinct()
        )
    }

    private fun labels(
        menus: List<AppMenu>
    ): List<StringResource> = menus.flatMap { it.entries }.mapNotNull { entry ->
        when (entry) {
            is AppMenuEntry.Command -> entry.label
            is AppMenuEntry.OpensDialog -> entry.label
            AppMenuEntry.Separator -> null
        }
    }

    private fun conventions(
        system: String
    ): PlatformConventions = ResolvePlatformConventionsUseCase(
        systemEnvironment = FakeSystemEnvironment(operatingSystemName = system, userHome = HOME)
    )()

    private companion object {
        const val HOME = "/home/ana"
    }
}

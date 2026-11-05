package app.notekea.core.presentation.menu

import app.notekea.core.platform.domain.FakeSystemEnvironment
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.resources.Res
import app.notekea.resources.menu_edit
import app.notekea.resources.menu_file
import app.notekea.resources.menu_help
import app.notekea.resources.menu_shortcuts
import app.notekea.resources.menu_view
import app.notekea.resources.menu_window
import kotlin.test.Test
import kotlin.test.assertEquals

/** The menu bar of macOS (`PLATFORM_GUIDELINES.md` §4). */
class AppMenusTest {
    private val macos = AppMenus.of(conventions = conventions(system = "Mac OS X"))

    @Test
    fun `it has File, Edit, View, Window and Help, in that order`() {
        val titles = listOf(
            Res.string.menu_file,
            Res.string.menu_edit,
            Res.string.menu_view,
            Res.string.menu_window,
            Res.string.menu_help
        )

        assertEquals(
            expected = titles,
            actual = macos.map { it.title }
        )
    }

    @Test
    fun `Help opens the shortcuts, as the app menu has the rest`() {
        assertEquals(
            expected = listOf(AppMenuEntry.OpensDialog(Res.string.menu_shortcuts, WindowDialog.ABOUT)),
            actual = macos.last().entries
        )
    }

    @Test
    fun `no shortcut is offered twice, so each runs one command`() {
        val shortcuts = macos.flatMap { it.entries }.filterIsInstance<AppMenuEntry.Command>().map { it.shortcut }

        assertEquals(
            expected = shortcuts,
            actual = shortcuts.distinct()
        )
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

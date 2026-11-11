package app.notekea.editor.presentation.noteinfo

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test

/** Note info's dates follow the interface's language (bug hunt, 2026-11-11): they were English in Spanish too. */
@OptIn(ExperimentalTestApi::class)
class NoteInfoRowsTest {
    private val original = Locale.getDefault()

    @AfterTest
    fun tearDown() {
        Locale.setDefault(original)
    }

    @Test
    fun `in Spanish the dates read in Spanish`() = runComposeUiTest {
        Locale.setDefault(Locale.forLanguageTag("es"))

        setContent {
            NotekeaTheme(darkTheme = false) {
                NoteInfoRows(
                    info = NoteInfo(
                        createdAt = LocalMoment(date = DAY),
                        modifiedAt = LocalMoment(
                            date = DAY,
                            hour = 15,
                            minute = 8
                        )
                    )
                )
            }
        }

        onNodeWithText(text = "11 nov 2026").assertExists()
        onNodeWithText(text = "11 nov 2026, 15:08").assertExists()
    }

    private companion object {
        val DAY = CalendarDate(
            year = 2026,
            month = 11,
            day = 11
        )
    }
}

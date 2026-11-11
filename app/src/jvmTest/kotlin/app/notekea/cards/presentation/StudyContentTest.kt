package app.notekea.cards.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test

/** The Study tab on screen (bug hunt, 2026-11-11): when the next card comes back is a date people read. */
@OptIn(ExperimentalTestApi::class)
class StudyContentTest {
    @Test
    fun `with nothing due it says the day the next card comes back, as a readable date`() =
        runComposeUiTest {
            setContent {
                NotekeaTheme(darkTheme = false) {
                    StudyContent(
                        state = StudyState(
                            isLoading = false,
                            total = 3,
                            reviewed = 2,
                            nextDate = CalendarDate(
                                year = 2026,
                                month = 11,
                                day = 12
                            )
                        ),
                        onIntent = {}
                    )
                }
            }

            onNodeWithText(
                text = "The next one comes back on Nov 12, 2026.",
                substring = true
            ).assertExists()
        }
}

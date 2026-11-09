package app.notekea.core.presentation

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A failure's last steps are what the person did: the vault's notes reaching each ViewModel
 * (`Workspace: OnTreeChange`, `TagPicker: OnTagsChange`…) would hide them, so they are not kept.
 */
class IntentTrailTest {
    private val steps = mutableListOf<String>()

    @AfterTest
    fun stopListening() {
        IntentTrail.listener = null
    }

    @Test
    fun `the trail names what the person did and leaves out what the app feeds itself`() {
        IntentTrail.listener = { steps += it }

        IntentTrail.record(
            owner = WorkspaceViewModel(),
            intent = TestIntent.OnTreeChange
        )

        IntentTrail.record(
            owner = WorkspaceViewModel(),
            intent = TestIntent.OnTabSelect
        )

        assertEquals(
            expected = listOf("Workspace: OnTabSelect"),
            actual = steps
        )
    }

    private class WorkspaceViewModel

    private sealed interface TestIntent {
        data object OnTreeChange :
            TestIntent,
            FeedIntent

        data object OnTabSelect : TestIntent
    }
}

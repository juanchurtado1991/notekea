package app.notekea.core.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.elapsed_days
import app.notekea.resources.elapsed_hours
import app.notekea.resources.elapsed_just_now
import app.notekea.resources.elapsed_minutes
import app.notekea.resources.elapsed_months
import app.notekea.resources.elapsed_weeks
import app.notekea.resources.elapsed_years
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_MINUTES = 5

/** How long ago, as people say it: "Just now", "5 minutes ago", "2 days ago"… (the recent vaults, the search index). */
@Composable
fun elapsedTimeText(
    elapsed: ElapsedTime
): String = when (elapsed) {
    is ElapsedTime.Days -> countText(resource = Res.plurals.elapsed_days, count = elapsed.count)
    is ElapsedTime.Hours -> countText(resource = Res.plurals.elapsed_hours, count = elapsed.count)
    ElapsedTime.JustNow -> stringResource(resource = Res.string.elapsed_just_now)
    is ElapsedTime.Minutes -> countText(resource = Res.plurals.elapsed_minutes, count = elapsed.count)
    is ElapsedTime.Months -> countText(resource = Res.plurals.elapsed_months, count = elapsed.count)
    is ElapsedTime.Weeks -> countText(resource = Res.plurals.elapsed_weeks, count = elapsed.count)
    is ElapsedTime.Years -> countText(resource = Res.plurals.elapsed_years, count = elapsed.count)
}

@Composable
private fun countText(
    resource: PluralStringResource,
    count: Int
): String = pluralStringResource(resource, count, count)

@Preview
@Composable
private fun elapsedTimeTextPreview() {
    NotekeaPreviewThemes { Text(text = elapsedTimeText(elapsed = ElapsedTime.Minutes(count = PREVIEW_MINUTES))) }
}

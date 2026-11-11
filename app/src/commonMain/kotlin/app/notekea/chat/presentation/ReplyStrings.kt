package app.notekea.chat.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.ReplyLanguage
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.chat_acted
import app.notekea.resources.chat_acted_es
import app.notekea.resources.chat_action_add_tag
import app.notekea.resources.chat_action_add_tag_es
import app.notekea.resources.chat_action_add_task
import app.notekea.resources.chat_action_add_task_es
import app.notekea.resources.chat_action_append_text
import app.notekea.resources.chat_action_append_text_es
import app.notekea.resources.chat_action_bookmark
import app.notekea.resources.chat_action_bookmark_es
import app.notekea.resources.chat_action_cancel
import app.notekea.resources.chat_action_cancel_es
import app.notekea.resources.chat_action_cancelled
import app.notekea.resources.chat_action_cancelled_es
import app.notekea.resources.chat_action_close_tabs
import app.notekea.resources.chat_action_close_tabs_es
import app.notekea.resources.chat_action_complete_task
import app.notekea.resources.chat_action_complete_task_es
import app.notekea.resources.chat_action_confirm
import app.notekea.resources.chat_action_confirm_es
import app.notekea.resources.chat_action_create_folder
import app.notekea.resources.chat_action_create_folder_es
import app.notekea.resources.chat_action_create_note
import app.notekea.resources.chat_action_create_note_es
import app.notekea.resources.chat_action_done_title
import app.notekea.resources.chat_action_done_title_es
import app.notekea.resources.chat_action_from
import app.notekea.resources.chat_action_from_es
import app.notekea.resources.chat_action_link_notes
import app.notekea.resources.chat_action_link_notes_es
import app.notekea.resources.chat_action_more
import app.notekea.resources.chat_action_more_es
import app.notekea.resources.chat_action_move
import app.notekea.resources.chat_action_move_es
import app.notekea.resources.chat_action_notes_from_text
import app.notekea.resources.chat_action_notes_from_text_es
import app.notekea.resources.chat_action_open
import app.notekea.resources.chat_action_open_es
import app.notekea.resources.chat_action_panel
import app.notekea.resources.chat_action_panel_es
import app.notekea.resources.chat_action_rename
import app.notekea.resources.chat_action_rename_es
import app.notekea.resources.chat_action_running
import app.notekea.resources.chat_action_running_es
import app.notekea.resources.chat_action_search
import app.notekea.resources.chat_action_search_es
import app.notekea.resources.chat_action_set_property
import app.notekea.resources.chat_action_set_property_es
import app.notekea.resources.chat_action_source
import app.notekea.resources.chat_action_source_es
import app.notekea.resources.chat_action_split
import app.notekea.resources.chat_action_split_es
import app.notekea.resources.chat_action_template
import app.notekea.resources.chat_action_template_es
import app.notekea.resources.chat_action_theme
import app.notekea.resources.chat_action_theme_es
import app.notekea.resources.chat_action_today
import app.notekea.resources.chat_action_today_es
import app.notekea.resources.chat_action_trash
import app.notekea.resources.chat_action_trash_es
import app.notekea.resources.chat_action_trash_note
import app.notekea.resources.chat_action_trash_note_es
import app.notekea.resources.chat_action_undo
import app.notekea.resources.chat_action_undo_es
import app.notekea.resources.chat_action_undone
import app.notekea.resources.chat_action_undone_es
import app.notekea.resources.chat_action_will
import app.notekea.resources.chat_action_will_es
import app.notekea.resources.chat_action_write_from_notes
import app.notekea.resources.chat_action_write_from_notes_es
import app.notekea.resources.chat_failed
import app.notekea.resources.chat_failed_es
import app.notekea.resources.chat_failed_search
import app.notekea.resources.chat_failed_search_es
import app.notekea.resources.chat_found_count
import app.notekea.resources.chat_found_count_es
import app.notekea.resources.chat_found_no_tasks
import app.notekea.resources.chat_found_no_tasks_es
import app.notekea.resources.chat_found_none
import app.notekea.resources.chat_found_none_es
import app.notekea.resources.chat_found_tasks
import app.notekea.resources.chat_found_tasks_es
import app.notekea.resources.chat_not_found
import app.notekea.resources.chat_not_found_es
import app.notekea.resources.chat_problem_exists
import app.notekea.resources.chat_problem_exists_es
import app.notekea.resources.chat_problem_failed
import app.notekea.resources.chat_problem_failed_es
import app.notekea.resources.chat_problem_folder
import app.notekea.resources.chat_problem_folder_es
import app.notekea.resources.chat_problem_long
import app.notekea.resources.chat_problem_long_es
import app.notekea.resources.chat_problem_many
import app.notekea.resources.chat_problem_many_es
import app.notekea.resources.chat_problem_missing
import app.notekea.resources.chat_problem_missing_es
import app.notekea.resources.chat_problem_note
import app.notekea.resources.chat_problem_note_es
import app.notekea.resources.chat_problem_query
import app.notekea.resources.chat_problem_query_es
import app.notekea.resources.chat_problem_query_failed
import app.notekea.resources.chat_problem_query_failed_es
import app.notekea.resources.chat_problem_refused
import app.notekea.resources.chat_problem_refused_es
import app.notekea.resources.chat_problem_task
import app.notekea.resources.chat_problem_task_es
import app.notekea.resources.chat_retry
import app.notekea.resources.chat_retry_es
import app.notekea.resources.chat_shown_close_tabs
import app.notekea.resources.chat_shown_close_tabs_es
import app.notekea.resources.chat_shown_open_note
import app.notekea.resources.chat_shown_open_note_es
import app.notekea.resources.chat_shown_open_today
import app.notekea.resources.chat_shown_open_today_es
import app.notekea.resources.chat_shown_panel
import app.notekea.resources.chat_shown_panel_es
import app.notekea.resources.chat_shown_search
import app.notekea.resources.chat_shown_search_es
import app.notekea.resources.chat_shown_source
import app.notekea.resources.chat_shown_source_es
import app.notekea.resources.chat_shown_split
import app.notekea.resources.chat_shown_split_es
import app.notekea.resources.chat_shown_theme
import app.notekea.resources.chat_shown_theme_es
import app.notekea.resources.chat_small_hint
import app.notekea.resources.chat_small_hint_es
import app.notekea.resources.chat_sources
import app.notekea.resources.chat_sources_es
import app.notekea.resources.chat_step_composing
import app.notekea.resources.chat_step_composing_es
import app.notekea.resources.chat_step_drafting
import app.notekea.resources.chat_step_drafting_es
import app.notekea.resources.chat_step_further
import app.notekea.resources.chat_step_further_es
import app.notekea.resources.chat_step_planning
import app.notekea.resources.chat_step_planning_es
import app.notekea.resources.chat_step_reading
import app.notekea.resources.chat_step_reading_es
import app.notekea.resources.chat_step_reading_one
import app.notekea.resources.chat_step_reading_one_es
import app.notekea.resources.chat_step_searching
import app.notekea.resources.chat_step_searching_es
import app.notekea.resources.chat_step_writing
import app.notekea.resources.chat_step_writing_es
import app.notekea.resources.chat_stopped
import app.notekea.resources.chat_stopped_es
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The language the chat replies to a question in: the question's own (the app's strings are English). */
internal val LocalReplyLanguage = staticCompositionLocalOf { ReplyLanguage.ENGLISH }

/** [resource] in the language of the question being answered ([LocalReplyLanguage]), with its [arguments]. */
@Composable
internal fun replyString(
    resource: StringResource,
    vararg arguments: Any
): String {
    val isSpanish = LocalReplyLanguage.current == ReplyLanguage.SPANISH
    val chosen = if (isSpanish) SPANISH[resource] ?: resource else resource

    return if (arguments.isEmpty()) {
        stringResource(resource = chosen)
    } else {
        stringResource(chosen, *arguments)
    }
}

@Preview
@Composable
private fun replyStringPreview() {
    NotekeaPreviewThemes {
        CompositionLocalProvider(LocalReplyLanguage provides ReplyLanguage.SPANISH) {
            Text(text = replyString(resource = Res.string.chat_not_found))
        }
    }
}

// The chat's replies in Spanish, the other language its models are evaluated in (docs/answer-quality.md).
private val SPANISH: Map<StringResource, StringResource> = mapOf(
    Res.string.chat_step_further to Res.string.chat_step_further_es,
    Res.string.chat_not_found to Res.string.chat_not_found_es,
    Res.string.chat_stopped to Res.string.chat_stopped_es,
    Res.string.chat_acted to Res.string.chat_acted_es,
    Res.string.chat_failed to Res.string.chat_failed_es,
    Res.string.chat_failed_search to Res.string.chat_failed_search_es,
    Res.string.chat_retry to Res.string.chat_retry_es,
    Res.string.chat_sources to Res.string.chat_sources_es,
    Res.string.chat_small_hint to Res.string.chat_small_hint_es,
    Res.string.chat_step_searching to Res.string.chat_step_searching_es,
    Res.string.chat_step_reading to Res.string.chat_step_reading_es,
    Res.string.chat_step_reading_one to Res.string.chat_step_reading_one_es,
    Res.string.chat_step_writing to Res.string.chat_step_writing_es,
    Res.string.chat_step_drafting to Res.string.chat_step_drafting_es,
    Res.string.chat_step_planning to Res.string.chat_step_planning_es,
    Res.string.chat_step_composing to Res.string.chat_step_composing_es,
    Res.string.chat_action_will to Res.string.chat_action_will_es,
    Res.string.chat_action_done_title to Res.string.chat_action_done_title_es,
    Res.string.chat_action_undone to Res.string.chat_action_undone_es,
    Res.string.chat_action_cancelled to Res.string.chat_action_cancelled_es,
    Res.string.chat_action_confirm to Res.string.chat_action_confirm_es,
    Res.string.chat_action_cancel to Res.string.chat_action_cancel_es,
    Res.string.chat_action_undo to Res.string.chat_action_undo_es,
    Res.string.chat_action_running to Res.string.chat_action_running_es,
    Res.string.chat_action_trash_note to Res.string.chat_action_trash_note_es,
    Res.string.chat_action_more to Res.string.chat_action_more_es,
    Res.string.chat_action_create_note to Res.string.chat_action_create_note_es,
    Res.string.chat_action_write_from_notes to Res.string.chat_action_write_from_notes_es,
    Res.string.chat_action_notes_from_text to Res.string.chat_action_notes_from_text_es,
    Res.string.chat_action_append_text to Res.string.chat_action_append_text_es,
    Res.string.chat_action_set_property to Res.string.chat_action_set_property_es,
    Res.string.chat_action_add_tag to Res.string.chat_action_add_tag_es,
    Res.string.chat_action_add_task to Res.string.chat_action_add_task_es,
    Res.string.chat_action_complete_task to Res.string.chat_action_complete_task_es,
    Res.string.chat_action_link_notes to Res.string.chat_action_link_notes_es,
    Res.string.chat_action_bookmark to Res.string.chat_action_bookmark_es,
    Res.string.chat_action_create_folder to Res.string.chat_action_create_folder_es,
    Res.string.chat_action_rename to Res.string.chat_action_rename_es,
    Res.string.chat_action_move to Res.string.chat_action_move_es,
    Res.string.chat_action_trash to Res.string.chat_action_trash_es,
    Res.string.chat_action_today to Res.string.chat_action_today_es,
    Res.string.chat_action_from to Res.string.chat_action_from_es,
    Res.string.chat_action_template to Res.string.chat_action_template_es,
    Res.string.chat_action_open to Res.string.chat_action_open_es,
    Res.string.chat_action_search to Res.string.chat_action_search_es,
    Res.string.chat_action_split to Res.string.chat_action_split_es,
    Res.string.chat_action_close_tabs to Res.string.chat_action_close_tabs_es,
    Res.string.chat_action_source to Res.string.chat_action_source_es,
    Res.string.chat_action_theme to Res.string.chat_action_theme_es,
    Res.string.chat_action_panel to Res.string.chat_action_panel_es,
    Res.string.chat_shown_open_note to Res.string.chat_shown_open_note_es,
    Res.string.chat_shown_open_today to Res.string.chat_shown_open_today_es,
    Res.string.chat_shown_search to Res.string.chat_shown_search_es,
    Res.string.chat_shown_panel to Res.string.chat_shown_panel_es,
    Res.string.chat_shown_split to Res.string.chat_shown_split_es,
    Res.string.chat_shown_close_tabs to Res.string.chat_shown_close_tabs_es,
    Res.string.chat_shown_source to Res.string.chat_shown_source_es,
    Res.string.chat_shown_theme to Res.string.chat_shown_theme_es,
    Res.string.chat_found_none to Res.string.chat_found_none_es,
    Res.string.chat_found_no_tasks to Res.string.chat_found_no_tasks_es,
    Res.string.chat_found_count to Res.string.chat_found_count_es,
    Res.string.chat_found_tasks to Res.string.chat_found_tasks_es,
    Res.string.chat_problem_note to Res.string.chat_problem_note_es,
    Res.string.chat_problem_folder to Res.string.chat_problem_folder_es,
    Res.string.chat_problem_missing to Res.string.chat_problem_missing_es,
    Res.string.chat_problem_many to Res.string.chat_problem_many_es,
    Res.string.chat_problem_exists to Res.string.chat_problem_exists_es,
    Res.string.chat_problem_task to Res.string.chat_problem_task_es,
    Res.string.chat_problem_query to Res.string.chat_problem_query_es,
    Res.string.chat_problem_query_failed to Res.string.chat_problem_query_failed_es,
    Res.string.chat_problem_long to Res.string.chat_problem_long_es,
    Res.string.chat_problem_failed to Res.string.chat_problem_failed_es,
    Res.string.chat_problem_refused to Res.string.chat_problem_refused_es
)

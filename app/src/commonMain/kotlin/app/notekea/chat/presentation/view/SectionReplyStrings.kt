package app.notekea.chat.presentation.view

import app.notekea.resources.Res
import app.notekea.resources.chat_follow_up_about
import app.notekea.resources.chat_follow_up_about_es
import app.notekea.resources.chat_follow_up_title
import app.notekea.resources.chat_follow_up_title_es
import app.notekea.resources.chat_problem_cannot_undo
import app.notekea.resources.chat_problem_cannot_undo_es
import app.notekea.resources.chat_section_clip
import app.notekea.resources.chat_section_clip_es
import app.notekea.resources.chat_section_clip_hint
import app.notekea.resources.chat_section_clip_hint_es
import app.notekea.resources.chat_section_closest
import app.notekea.resources.chat_section_closest_es
import app.notekea.resources.chat_section_counted
import app.notekea.resources.chat_section_counted_es
import app.notekea.resources.chat_section_counted_full
import app.notekea.resources.chat_section_counted_full_es
import app.notekea.resources.chat_section_deep
import app.notekea.resources.chat_section_deep_es
import app.notekea.resources.chat_section_deep_hint
import app.notekea.resources.chat_section_deep_hint_es
import app.notekea.resources.chat_section_found
import app.notekea.resources.chat_section_found_es
import app.notekea.resources.chat_section_full
import app.notekea.resources.chat_section_full_all
import app.notekea.resources.chat_section_full_all_es
import app.notekea.resources.chat_section_full_es
import app.notekea.resources.chat_section_full_hint
import app.notekea.resources.chat_section_full_hint_es
import app.notekea.resources.chat_section_index
import app.notekea.resources.chat_section_index_es
import app.notekea.resources.chat_section_left_out
import app.notekea.resources.chat_section_left_out_es
import app.notekea.resources.chat_section_levels
import app.notekea.resources.chat_section_levels_es
import app.notekea.resources.chat_section_links
import app.notekea.resources.chat_section_links_es
import app.notekea.resources.chat_section_links_hint
import app.notekea.resources.chat_section_links_hint_es
import app.notekea.resources.chat_section_no_summaries
import app.notekea.resources.chat_section_no_summaries_es
import app.notekea.resources.chat_section_one_page
import app.notekea.resources.chat_section_one_page_es
import app.notekea.resources.chat_section_only
import app.notekea.resources.chat_section_only_es
import app.notekea.resources.chat_section_only_hint
import app.notekea.resources.chat_section_only_hint_es
import app.notekea.resources.chat_section_pages
import app.notekea.resources.chat_section_pages_es
import app.notekea.resources.chat_section_recommended
import app.notekea.resources.chat_section_recommended_es
import app.notekea.resources.chat_site_clip
import app.notekea.resources.chat_site_clip_es
import app.notekea.resources.chat_site_found
import app.notekea.resources.chat_site_found_es
import app.notekea.resources.chat_site_looking
import app.notekea.resources.chat_site_looking_es
import app.notekea.resources.chat_site_none
import app.notekea.resources.chat_site_none_es
import app.notekea.resources.chat_site_search
import app.notekea.resources.chat_site_search_es
import app.notekea.resources.chat_site_search_hint
import app.notekea.resources.chat_site_search_hint_es
import app.notekea.resources.chat_site_sources
import app.notekea.resources.chat_site_sources_es
import org.jetbrains.compose.resources.StringResource

// The question of a page's links and the follow-ups in Spanish ([replyString]): apart from the chat's other
// replies for the file's length.
internal val SECTION_SPANISH: Map<StringResource, StringResource> = mapOf(
    Res.string.chat_problem_cannot_undo to Res.string.chat_problem_cannot_undo_es,
    Res.string.chat_site_search to Res.string.chat_site_search_es,
    Res.string.chat_site_search_hint to Res.string.chat_site_search_hint_es,
    Res.string.chat_site_sources to Res.string.chat_site_sources_es,
    Res.string.chat_site_looking to Res.string.chat_site_looking_es,
    Res.string.chat_site_found to Res.string.chat_site_found_es,
    Res.string.chat_site_none to Res.string.chat_site_none_es,
    Res.string.chat_site_clip to Res.string.chat_site_clip_es,
    Res.string.chat_follow_up_title to Res.string.chat_follow_up_title_es,
    Res.string.chat_follow_up_about to Res.string.chat_follow_up_about_es,
    Res.string.chat_section_clip to Res.string.chat_section_clip_es,
    Res.string.chat_section_clip_hint to Res.string.chat_section_clip_hint_es,
    Res.string.chat_section_closest to Res.string.chat_section_closest_es,
    Res.string.chat_section_counted to Res.string.chat_section_counted_es,
    Res.string.chat_section_counted_full to Res.string.chat_section_counted_full_es,
    Res.string.chat_section_deep to Res.string.chat_section_deep_es,
    Res.string.chat_section_deep_hint to Res.string.chat_section_deep_hint_es,
    Res.string.chat_section_found to Res.string.chat_section_found_es,
    Res.string.chat_section_full to Res.string.chat_section_full_es,
    Res.string.chat_section_full_all to Res.string.chat_section_full_all_es,
    Res.string.chat_section_full_hint to Res.string.chat_section_full_hint_es,
    Res.string.chat_section_index to Res.string.chat_section_index_es,
    Res.string.chat_section_left_out to Res.string.chat_section_left_out_es,
    Res.string.chat_section_levels to Res.string.chat_section_levels_es,
    Res.string.chat_section_links to Res.string.chat_section_links_es,
    Res.string.chat_section_links_hint to Res.string.chat_section_links_hint_es,
    Res.string.chat_section_no_summaries to Res.string.chat_section_no_summaries_es,
    Res.string.chat_section_one_page to Res.string.chat_section_one_page_es,
    Res.string.chat_section_only to Res.string.chat_section_only_es,
    Res.string.chat_section_only_hint to Res.string.chat_section_only_hint_es,
    Res.string.chat_section_pages to Res.string.chat_section_pages_es,
    Res.string.chat_section_recommended to Res.string.chat_section_recommended_es
)

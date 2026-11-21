package app.notekea.chat.presentation.view

import app.notekea.resources.Res
import app.notekea.resources.chat_action_forget
import app.notekea.resources.chat_action_forget_es
import app.notekea.resources.chat_action_remember
import app.notekea.resources.chat_action_remember_es
import app.notekea.resources.chat_agent_memory
import app.notekea.resources.chat_agent_memory_es
import app.notekea.resources.chat_agent_web_search
import app.notekea.resources.chat_agent_web_search_es
import app.notekea.resources.chat_memory_forget
import app.notekea.resources.chat_memory_forget_es
import app.notekea.resources.chat_remember_choice
import app.notekea.resources.chat_remember_choice_es
import app.notekea.resources.chat_remember_where
import app.notekea.resources.chat_remember_where_es
import app.notekea.resources.chat_sources_found
import app.notekea.resources.chat_sources_found_es
import app.notekea.resources.chat_sources_hint
import app.notekea.resources.chat_sources_hint_es
import app.notekea.resources.chat_sources_never
import app.notekea.resources.chat_sources_never_es
import app.notekea.resources.chat_sources_new
import app.notekea.resources.chat_sources_new_es
import app.notekea.resources.chat_sources_omitted
import app.notekea.resources.chat_sources_omitted_es
import app.notekea.resources.chat_sources_read
import app.notekea.resources.chat_sources_read_es
import app.notekea.resources.chat_sources_remember
import app.notekea.resources.chat_sources_remember_es
import app.notekea.resources.chat_sources_trust
import app.notekea.resources.chat_sources_trust_es
import app.notekea.resources.chat_sources_trusted
import app.notekea.resources.chat_sources_trusted_es
import app.notekea.resources.chat_web_search
import app.notekea.resources.chat_web_search_es
import app.notekea.resources.chat_web_search_hint
import app.notekea.resources.chat_web_search_hint_es
import app.notekea.resources.chat_web_search_request
import app.notekea.resources.chat_web_search_request_es
import org.jetbrains.compose.resources.StringResource

// The chat's memory and web search in Spanish (`docs/agent-memory.md`, `docs/agent-web.md`), for [replyString].
internal val MEMORY_SPANISH: Map<StringResource, StringResource> = mapOf(
    Res.string.chat_agent_memory to Res.string.chat_agent_memory_es,
    Res.string.chat_remember_choice to Res.string.chat_remember_choice_es,
    Res.string.chat_remember_where to Res.string.chat_remember_where_es,
    Res.string.chat_memory_forget to Res.string.chat_memory_forget_es,
    Res.string.chat_web_search to Res.string.chat_web_search_es,
    Res.string.chat_web_search_hint to Res.string.chat_web_search_hint_es,
    Res.string.chat_web_search_request to Res.string.chat_web_search_request_es,
    Res.string.chat_agent_web_search to Res.string.chat_agent_web_search_es,
    Res.string.chat_sources_found to Res.string.chat_sources_found_es,
    Res.string.chat_sources_hint to Res.string.chat_sources_hint_es,
    Res.string.chat_sources_new to Res.string.chat_sources_new_es,
    Res.string.chat_sources_omitted to Res.string.chat_sources_omitted_es,
    Res.string.chat_sources_read to Res.string.chat_sources_read_es,
    Res.string.chat_sources_remember to Res.string.chat_sources_remember_es,
    Res.string.chat_sources_trust to Res.string.chat_sources_trust_es,
    Res.string.chat_sources_never to Res.string.chat_sources_never_es,
    Res.string.chat_sources_trusted to Res.string.chat_sources_trusted_es,
    Res.string.chat_action_remember to Res.string.chat_action_remember_es,
    Res.string.chat_action_forget to Res.string.chat_action_forget_es
)

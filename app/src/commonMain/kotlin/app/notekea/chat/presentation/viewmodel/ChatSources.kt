package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.sources.AnswerFromPagesUseCase
import app.notekea.chat.domain.sources.SourceSites

/**
 * What the chat looks further with when the notes do not hold an answer ([ChatSiteSearch]): the sites of the vault's
 * web sources ([sourceSites], none where the clips feature is not there) and the answer written from pages just
 * clipped.
 */
class ChatSources(
    val answerFromPages: AnswerFromPagesUseCase,
    val sourceSites: SourceSites?
)

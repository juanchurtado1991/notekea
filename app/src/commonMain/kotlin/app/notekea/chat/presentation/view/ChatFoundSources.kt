package app.notekea.chat.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaCard
import app.notekea.core.presentation.designsystem.components.NotekeaChip
import app.notekea.core.presentation.designsystem.components.fields.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.web.domain.FoundSource
import app.notekea.core.web.domain.SourceTrust
import app.notekea.core.web.domain.WebResult
import app.notekea.resources.Res
import app.notekea.resources.chat_sources_found
import app.notekea.resources.chat_sources_hint
import app.notekea.resources.chat_sources_never
import app.notekea.resources.chat_sources_new
import app.notekea.resources.chat_sources_omitted
import app.notekea.resources.chat_sources_read
import app.notekea.resources.chat_sources_remember
import app.notekea.resources.chat_sources_trust
import app.notekea.resources.chat_sources_trusted

private const val INITIAL_DP = 18

/**
 * The sources a web search found, to approve before any is read (`docs/agent-web.md`, chosen by the developer on
 * 2026-11-15): a row each with a checkbox, its site's initial, its title and its site; the trusted ones ticked and
 * tagged, the new ones with Remember ▾ (always or never trust their site); the ones left out counted; Read N sources.
 */
@Composable
internal fun ChatFoundSources(
    sources: List<FoundSource>,
    picked: Set<String>,
    omitted: Int,
    onToggle: (String) -> Unit,
    onTrust: (domain: String, isTrusted: Boolean) -> Unit,
    onRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotekeaCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            Text(
                text = replyString(Res.string.chat_sources_found, sources.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = replyString(resource = Res.string.chat_sources_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            sources.forEach { source ->
                SourceRow(
                    source = source,
                    isPicked = source.result.url in picked,
                    onToggle = { onToggle(source.result.url) },
                    onTrust = { onTrust(source.result.domain, it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val omittedLine = replyString(Res.string.chat_sources_omitted, omitted)
                val left = if (omitted > 0) omittedLine else ""

                Text(
                    text = left,
                    modifier = Modifier.weight(weight = 1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NotekeaButton(
                    text = replyString(Res.string.chat_sources_read, picked.size),
                    onClick = onRead,
                    enabled = picked.isNotEmpty(),
                    style = NotekeaButtonStyle.PRIMARY
                )
            }
        }
    }
}

@Composable
private fun SourceRow(
    source: FoundSource,
    isPicked: Boolean,
    onToggle: () -> Unit,
    onTrust: (Boolean) -> Unit
) {
    val isTrusted = source.trust == SourceTrust.Trust.TRUSTED

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Initial(domain = source.result.domain)

        Column(modifier = Modifier.weight(weight = 1f)) {
            NotekeaCheckbox(
                checked = isPicked,
                onClick = onToggle,
                label = source.result.title
            )

            val site = if (isTrusted) {
                source.result.domain
            } else {
                replyString(Res.string.chat_sources_new, source.result.domain)
            }

            Text(
                text = site,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isTrusted) {
            NotekeaChip(label = replyString(resource = Res.string.chat_sources_trusted))
        } else {
            val remember = replyString(resource = Res.string.chat_sources_remember)

            NotekeaPopUpButton(
                choice = remember,
                choices = listOf(
                    NotekeaMenuItem(
                        label = replyString(Res.string.chat_sources_trust, source.result.domain),
                        onClick = { onTrust(true) }
                    ),
                    NotekeaMenuItem(
                        label = replyString(Res.string.chat_sources_never, source.result.domain),
                        onClick = { onTrust(false) }
                    )
                ),
                label = remember
            )
        }
    }
}

// The site's first letter on a quiet square, in place of a favicon, which would be a request of its own.
@Composable
private fun Initial(domain: String) {
    Box(
        modifier = Modifier
            .size(size = INITIAL_DP.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = domain.take(n = 1).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun ChatFoundSourcesPreview() {
    NotekeaPreviewThemes {
        ChatFoundSources(
            sources = listOf(
                FoundSource(
                    result = WebResult("Tomato under glass", "https://www.fao.org/tomato", ""),
                    trust = SourceTrust.Trust.TRUSTED
                ),
                FoundSource(
                    result = WebResult("Drip irrigation", "https://www.ucr.ac.cr/drip", ""),
                    trust = SourceTrust.Trust.NEW
                )
            ),
            picked = setOf("https://www.fao.org/tomato"),
            omitted = 2,
            onToggle = {},
            onTrust = { _, _ -> },
            onRead = {}
        )
    }
}

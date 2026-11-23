package app.notekea.query.domain

import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.llm.domain.ReplyText

/**
 * Turns a request in plain words into a query of the vault (3.8): the model only writes the query, which is checked
 * against the [QueryCatalog] before it runs, and the query tab or the chat shows it so it can be corrected. A property
 * the vault does not have is not guessed: it comes back with the closest ones, to ask which was meant.
 */
class TranslateQueryUseCase(
    private val llmEngine: LlmEngine,
    private val reader: QueryJsonReader
) {
    suspend operator fun invoke(
        request: String,
        catalog: QueryCatalog,
        today: String
    ): QueryTranslation {
        val json = try {
            ReplyText.of(
                llmEngine = llmEngine,
                messages = QueryPrompt.messages(
                    request = request,
                    catalog = catalog,
                    today = today
                ),
                options = GenerationOptions(
                    maxTokens = MAX_TOKENS,
                    temperature = 0f,
                    jsonSchema = QueryPrompt.SCHEMA
                )
            )
        } catch (_: LlmException) {
            return QueryTranslation.Failed
        }

        val written = reader.read(json = json) ?: return QueryTranslation.Failed

        return checked(
            query = written,
            catalog = catalog
        )
    }

    /** [query] with the catalog's spelling of every name, or the first property it does not know. */
    private fun checked(
        query: VaultQuery,
        catalog: QueryCatalog
    ): QueryTranslation {
        val names = catalog.keys.associateBy { it.name.lowercase() }

        val filters = query.filters.map { filter ->
            when (filter.condition) {
                QueryCondition.IN_FOLDER -> filter.copy(
                    property = "",
                    value = folderOf(
                        named = filter.value.trim('/'),
                        folders = catalog.folders
                    )
                        ?: return unknown(
                            name = filter.value,
                            among = catalog.folders
                        )
                )

                QueryCondition.HAS_TAG -> filter.copy(
                    property = "",
                    value = filter.value.removePrefix(prefix = "#")
                )

                // A property no note has is empty in every note: "no priority" with no priority written yet.
                else -> filter.copy(
                    property = names[filter.property.lowercase()]?.name
                        ?: filter.property.takeIf {
                            filter.condition == QueryCondition.IS_EMPTY
                        }
                        ?: return unknown(
                            name = filter.property,
                            among = catalog.keys.map { it.name }
                        )
                )
            }
        }

        // An order by a property the vault lacks asks which was meant too, rather than leaving the order out.
        val sort = query.sort?.let { sort ->
            val known = names[sort.property.lowercase()]
                ?: return unknown(
                    name = sort.property,
                    among = catalog.keys.map { it.name }
                )

            sort.copy(property = known.name)
        }

        return QueryTranslation.Query(
            query = VaultQuery(
                filters = filters,
                sort = sort
            )
        )
    }

    // A folder by its path, or by its own name where one folder alone has it: "Projects" is Work/Projects.
    private fun folderOf(
        named: String,
        folders: List<String>
    ): String? = folders.firstOrNull { it.equals(named, ignoreCase = true) }
        ?: folders.singleOrNull {
            it
                .substringAfterLast(delimiter = '/')
                .equals(named, ignoreCase = true)
        }

    private fun unknown(
        name: String,
        among: List<String>
    ): QueryTranslation = QueryTranslation.UnknownProperty(
        property = name,
        suggestions = among
            .sortedBy {
                closeness(
                    first = it.lowercase(),
                    second = name.lowercase()
                )
            }
            .take(n = SUGGESTIONS)
    )

    /** How far apart two names are: shared start counts most, then length. */
    private fun closeness(
        first: String,
        second: String
    ): Int {
        val shared = first.commonPrefixWith(other = second).length

        return kotlin.math.abs(first.length - second.length) - shared * PREFIX_WEIGHT
    }

    private companion object {
        const val MAX_TOKENS = 300
        const val SUGGESTIONS = 3
        const val PREFIX_WEIGHT = 4
    }
}

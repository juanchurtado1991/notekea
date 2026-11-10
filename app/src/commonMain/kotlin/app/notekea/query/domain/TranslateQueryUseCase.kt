package app.notekea.query.domain

import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException

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
        val json = StringBuilder()

        try {
            llmEngine
                .generate(
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
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return QueryTranslation.Failed
        }

        val written = reader.read(json = json.toString()) ?: return QueryTranslation.Failed

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
                    value = catalog.folders.firstOrNull {
                        it.equals(filter.value.trim('/'), ignoreCase = true)
                    }
                        ?: return unknown(
                            name = filter.value,
                            among = catalog.folders
                        )
                )

                QueryCondition.HAS_TAG -> filter.copy(
                    property = "",
                    value = filter.value.removePrefix(prefix = "#")
                )

                else -> filter.copy(
                    property = names[filter.property.lowercase()]?.name
                        ?: return unknown(
                            name = filter.property,
                            among = catalog.keys.map { it.name }
                        )
                )
            }
        }

        val sort = query.sort?.let { sort ->
            names[sort.property.lowercase()]?.let { sort.copy(property = it.name) }
        }

        return QueryTranslation.Query(
            query = VaultQuery(
                filters = filters,
                sort = sort
            )
        )
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

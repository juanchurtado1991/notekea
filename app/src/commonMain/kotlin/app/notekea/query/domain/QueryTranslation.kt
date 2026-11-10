package app.notekea.query.domain

/** What a request in plain words became (3.8): a [Query] to run and show, or why it could not be one. */
sealed interface QueryTranslation {
    data class Query(val query: VaultQuery) : QueryTranslation

    /** It names a property the vault does not have; [suggestions] are the closest the vault has. */
    data class UnknownProperty(
        val property: String,
        val suggestions: List<String>
    ) : QueryTranslation

    /** The model was not available, or wrote no query. */
    data object Failed : QueryTranslation
}

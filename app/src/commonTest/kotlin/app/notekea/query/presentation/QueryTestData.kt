package app.notekea.query.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryKey
import app.notekea.query.domain.QueryPreferencesStore
import app.notekea.query.domain.QuerySort
import app.notekea.query.domain.QueryValueKind
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SaveQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.VaultQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The query the tests keep between runs, in memory. */
internal class KeptQuery : QueryPreferencesStore {
    var query = VaultQuery()

    override suspend fun read(vaultPath: String): VaultQuery = query

    override suspend fun write(
        vaultPath: String,
        query: VaultQuery
    ) {
        this.query = query
    }
}

/** The vault the query tests run on: two books with a rating. */
internal object QueryTestData {
    const val VAULT = "/vault"
    val DUNE = RelativePath(value = "Books/Dune.md")

    fun note(
        path: RelativePath,
        rating: String
    ) = NoteEntry(
        file = NoteFile(
            path = path,
            size = null,
            modifiedAt = null
        ),
        metadata = NoteMetadata.NONE.copy(
            properties = listOf(
                NoteProperty(
                    key = "rating",
                    value = PropertyValue.Number(text = rating)
                )
            )
        )
    )

    fun rating(text: String) = NoteProperty(
        key = "rating",
        value = PropertyValue.Number(text = text)
    )

    val NOTES = listOf(
        note(
            path = DUNE,
            rating = "5"
        ),
        note(
            path = RelativePath("Books/Emma.md"),
            rating = "2"
        )
    )
}

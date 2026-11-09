package app.notekea.query.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryParse
import app.notekea.query.domain.QueryProblem
import app.notekea.resources.Res
import app.notekea.resources.query_chip_filter
import app.notekea.resources.query_chip_folder
import app.notekea.resources.query_chip_tag
import app.notekea.resources.query_condition_after
import app.notekea.resources.query_condition_before
import app.notekea.resources.query_condition_contains
import app.notekea.resources.query_condition_is
import app.notekea.resources.query_condition_is_empty
import app.notekea.resources.query_condition_less_than
import app.notekea.resources.query_condition_more_than
import app.notekea.resources.query_count_many
import app.notekea.resources.query_count_one
import app.notekea.resources.query_invalid_condition
import app.notekea.resources.query_invalid_key
import app.notekea.resources.query_invalid_order
import app.notekea.resources.query_invalid_part
import org.jetbrains.compose.resources.stringResource

private const val TAG_MARK = "#"

/** How a person reads [condition]; a folder and a tag are just "is". */
@Composable
internal fun conditionLabel(
    condition: QueryCondition
): String = when (condition) {
    QueryCondition.IS, QueryCondition.IN_FOLDER, QueryCondition.HAS_TAG ->
        stringResource(resource = Res.string.query_condition_is)

    QueryCondition.CONTAINS -> stringResource(resource = Res.string.query_condition_contains)

    QueryCondition.MORE_THAN -> stringResource(resource = Res.string.query_condition_more_than)

    QueryCondition.LESS_THAN -> stringResource(resource = Res.string.query_condition_less_than)

    QueryCondition.BEFORE -> stringResource(resource = Res.string.query_condition_before)

    QueryCondition.AFTER -> stringResource(resource = Res.string.query_condition_after)

    QueryCondition.IS_EMPTY -> stringResource(resource = Res.string.query_condition_is_empty)
}

/** What a chip says about [filter]. */
@Composable
internal fun filterLabel(
    filter: QueryFilter
): String = when (filter.condition) {
    QueryCondition.IN_FOLDER -> stringResource(resource = Res.string.query_chip_folder, filter.value)

    QueryCondition.HAS_TAG ->
        stringResource(resource = Res.string.query_chip_tag, filter.value.removePrefix(prefix = TAG_MARK))

    else -> stringResource(
        resource = Res.string.query_chip_filter,
        filter.property,
        conditionLabel(condition = filter.condition),
        filter.value
    ).trim()
}

@Composable
internal fun noteCountLabel(
    count: Int
): String = if (count == 1) {
    stringResource(resource = Res.string.query_count_one)
} else {
    stringResource(resource = Res.string.query_count_many, count)
}

/** Which line of a query block cannot be read, and why. */
@Composable
internal fun invalidLabel(
    invalid: QueryParse.Invalid
): String = when (invalid.problem) {
    QueryProblem.UNKNOWN_KEY -> stringResource(resource = Res.string.query_invalid_key, invalid.line)
    QueryProblem.UNKNOWN_CONDITION -> stringResource(resource = Res.string.query_invalid_condition, invalid.line)
    QueryProblem.UNKNOWN_ORDER -> stringResource(resource = Res.string.query_invalid_order, invalid.line)
    QueryProblem.MISSING_PART -> stringResource(resource = Res.string.query_invalid_part, invalid.line)
}

@Preview
@Composable
private fun conditionLabelPreview() {
    NotekeaPreviewThemes { Text(text = conditionLabel(condition = QueryCondition.MORE_THAN)) }
}

@Preview
@Composable
private fun filterLabelPreview() {
    NotekeaPreviewThemes { Text(text = filterLabel(filter = QueryPreviewData.ratingFilter)) }
}

@Preview
@Composable
private fun noteCountLabelPreview() {
    NotekeaPreviewThemes { Text(text = noteCountLabel(count = 2)) }
}

@Preview
@Composable
private fun invalidLabelPreview() {
    NotekeaPreviewThemes {
        Text(text = invalidLabel(invalid = QueryParse.Invalid(line = 2, problem = QueryProblem.MISSING_PART)))
    }
}

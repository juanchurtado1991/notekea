package app.notekea.editor.domain

/**
 * What changes a table from the cell the cursor is in ([TableEditing]). [NEXT_ROW] is Tab in the last cell: a new last
 * row, with the cursor in its first cell. [AFTER] leaves the table downwards: a blank line and the cursor under it.
 */
enum class TableAction {
    ROW_ABOVE,
    ROW_BELOW,
    NEXT_ROW,
    COLUMN_LEFT,
    COLUMN_RIGHT,
    ALIGN_LEFT,
    ALIGN_CENTER,
    ALIGN_RIGHT,
    DELETE_ROW,
    DELETE_COLUMN,
    AFTER
}

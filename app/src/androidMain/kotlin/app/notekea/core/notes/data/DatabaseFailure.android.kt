package app.notekea.core.notes.data

import android.database.SQLException

internal actual fun isDatabaseFailure(exception: Exception): Boolean = exception is SQLException

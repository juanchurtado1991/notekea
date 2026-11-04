package app.notekea.notes.data

import android.database.SQLException

internal actual fun isDatabaseFailure(exception: Exception): Boolean = exception is SQLException

package app.notekea.selftest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.testbuddy.CaseFolders

/**
 * Notekea as an automated case shows it: a [CaseApp] on the case's folders, closed with the case. A case can quit it
 * ([CaseApps.quit]): the window's close guard runs, as when a person closes the window, then a new Notekea starts on
 * the same folders, as a person launching it again.
 */
@Composable
fun NotekeaCase(folders: CaseFolders) {
    var launch by remember { mutableIntStateOf(0) }
    key(launch) {
        val app = remember { CaseApp(appData = folders.appData, vaults = folders.data) }
        val quit = remember { CaseApps.register(app = app) }
        LaunchedEffect(quit.isRequested) {
            if (!quit.isRequested) return@LaunchedEffect
            app.closeGuard.beforeClose()
            app.close()
            quit.whileClosed()
            launch++
        }
        DisposableEffect(app) {
            onDispose {
                CaseApps.unregister(app = app)
                app.close()
            }
        }
        app.Content()
    }
}

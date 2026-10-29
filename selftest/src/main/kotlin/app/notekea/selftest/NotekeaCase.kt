package app.notekea.selftest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.testbuddy.CaseFolders

/** Notekea as an automated case shows it: a [CaseApp] on the case's folders, closed with the case. */
@Composable
fun NotekeaCase(folders: CaseFolders) {
    val app = remember(folders) { CaseApp(appData = folders.appData, vaults = folders.data).also(CaseApps::register) }
    DisposableEffect(app) {
        onDispose {
            CaseApps.unregister(app = app)
            app.close()
        }
    }
    app.Content()
}

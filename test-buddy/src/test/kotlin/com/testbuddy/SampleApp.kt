package com.testbuddy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.delay
import java.nio.file.Files

/**
 * A tiny app for Test Buddy's own tests: a note read from the case's data, a field to edit it, Mod+S and a button that
 * save it (the button a moment later, as apps save in the background), a counter and a long list.
 */
@Composable
internal fun SampleApp(folders: CaseFolders) {
    val note = folders.data.resolve("notes/draft.txt")
    var text by remember { mutableStateOf(Files.readString(note)) }
    var clicks by remember { mutableStateOf(0) }
    var saveLater by remember { mutableStateOf(false) }
    LaunchedEffect(saveLater) {
        if (saveLater) {
            delay(timeMillis = 500)
            Files.writeString(note, text)
            saveLater = false
        }
    }
    Column(
        modifier = Modifier.onPreviewKeyEvent { event ->
            val isSave = event.type == KeyEventType.KeyDown && event.key == Key.S &&
                (event.isCtrlPressed || event.isMetaPressed)
            if (isSave) Files.writeString(note, text)
            isSave
        }
    ) {
        BasicTextField(value = text, onValueChange = { text = it })
        Button(onClick = { clicks++ }) { Text(text = "Clicked $clicks times") }
        Button(onClick = { saveLater = true }) { Text(text = "Save later") }
        if (clicks > 0) Text(text = "Hello again")
        LazyColumn { items(count = 100) { Text(text = "Row $it") } }
    }
}

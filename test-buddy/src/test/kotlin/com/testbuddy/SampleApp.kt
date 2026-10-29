package com.testbuddy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.nio.file.Files

/**
 * A tiny app for Test Buddy's own tests: a note read from the case's data, a field to edit it, Mod+S and a button that
 * save it (the button a moment later, as apps save in the background), a counter and a long list.
 */
@OptIn(ExperimentalComposeUiApi::class)
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
        // A hidden twin first, as a panel being replaced keeps its text: clicks must reach the button on screen.
        Box(modifier = Modifier.size(size = 0.dp)) { Button(onClick = {}) { Text(text = "Clicked $clicks times") } }
        Button(onClick = { clicks++ }) { Text(text = "Clicked $clicks times") }
        Button(onClick = { saveLater = true }) { Text(text = "Save later") }
        Button(onClick = { clicks += 10 }, modifier = Modifier.semantics { contentDescription = "Add ten" }) {
            Text(text = "+10")
        }
        if (clicks > 0) Text(text = "Hello again")
        LinkAndMenu()
        LazyColumn { items(count = 100) { Text(text = "Row $it") } }
    }
}

/** A link inside a sentence, for clickWords, and a text with a right-click menu, for rightClick. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LinkAndMenu() {
    var linked by remember { mutableStateOf(false) }
    Text(
        text = buildAnnotatedString {
            append("Read the ")
            withLink(LinkAnnotation.Clickable(tag = "guide") { linked = true }) { append("guide") }
            append(" before you start, it is long enough to need a precise click on its words.")
        }
    )
    if (linked) Text(text = "Guide opened")
    var menu by remember { mutableStateOf(false) }
    Text(
        text = if (menu) "Menu open" else "Right-click me",
        modifier = Modifier.onPointerEvent(PointerEventType.Press) {
            if (it.buttons.isSecondaryPressed) menu = true
        }
    )
}

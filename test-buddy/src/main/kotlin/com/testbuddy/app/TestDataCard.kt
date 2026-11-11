package com.testbuddy.app

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.nio.file.Path

/**
 * What a manual case works on, as an instruction: each folder of test data it needs ([folders]), freshly copied into
 * [dataFolder] for this case, where it is on the disk (selectable), and how to open it in the app.
 */
@Composable
internal fun TestDataCard(
    appName: String,
    folders: List<String>,
    dataFolder: Path
) {
    val texts = LocalTesterTexts.current

    Card {
        SectionLabel(text = texts.testData)

        folders.forEach { name ->
            Text(
                text = texts.freshCopyOf(name),
                fontSize = 12.sp
            )

            SelectionContainer {
                Text(
                    text = dataFolder.resolve(name).toString(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = texts.openDataHint(appName),
            fontSize = 11.sp,
            color = AutoTestColors.Muted
        )
    }
}

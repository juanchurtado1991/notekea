package app.notekea.notes.presentation.files

import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

class FileIconsTest {
    @Test
    fun `pictures, sounds and videos have their own icon, whatever the case of the extension`() {
        assertEquals(
            expected = listOf(
                NotekeaIcons.ImageFile,
                NotekeaIcons.MediaFile,
                NotekeaIcons.MediaFile,
                NotekeaIcons.OtherFile,
                NotekeaIcons.OtherFile
            ),
            actual = listOf("a/Plate.PNG", "song.mp3", "Clip.MOV", "menu.pdf", "README").map {
                fileIconOf(path = RelativePath(value = it))
            }
        )
    }
}

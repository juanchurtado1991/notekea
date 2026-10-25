package app.notekea.core.presentation.theme

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.ArrowDownAZ
import com.composables.icons.lucide.ArrowUp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ClockArrowDown
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.FilePlus
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.FolderOpen
import com.composables.icons.lucide.FolderPlus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PanelLeftClose
import com.composables.icons.lucide.PanelLeftOpen
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.SquareArrowOutUpRight
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X

/**
 * Every icon of the app, named by what it means rather than how it looks, so a screen asks for [NewNote] and the
 * glyph can change here alone. They come from Lucide, the set Obsidian users already know (`LIBRARIES.md` §2).
 */
object NotekeaIcons {
    val Checked: ImageVector get() = Lucide.Check
    val Close: ImageVector get() = Lucide.X
    val Collapse: ImageVector get() = Lucide.ChevronDown
    val Delete: ImageVector get() = Lucide.Trash2
    val Expand: ImageVector get() = Lucide.ChevronRight
    val Folder: ImageVector get() = Lucide.Folder
    val FolderOpen: ImageVector get() = Lucide.FolderOpen
    val FindNext: ImageVector get() = Lucide.ArrowDown
    val FindPrevious: ImageVector get() = Lucide.ArrowUp
    val HideSidebar: ImageVector get() = Lucide.PanelLeftClose
    val More: ImageVector get() = Lucide.Ellipsis
    val NewFolder: ImageVector get() = Lucide.FolderPlus
    val NewNote: ImageVector get() = Lucide.FilePlus
    val Note: ImageVector get() = Lucide.FileText
    val Rename: ImageVector get() = Lucide.Pencil
    val Reveal: ImageVector get() = Lucide.SquareArrowOutUpRight
    val ShowSidebar: ImageVector get() = Lucide.PanelLeftOpen
    val SortByModified: ImageVector get() = Lucide.ClockArrowDown
    val SortByName: ImageVector get() = Lucide.ArrowDownAZ
}

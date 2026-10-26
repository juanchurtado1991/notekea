package app.notekea.core.presentation.theme

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.ListTree
import com.composables.icons.lucide.PanelRightClose
import com.composables.icons.lucide.PanelRightOpen
import com.composables.icons.lucide.ArrowDownAZ
import com.composables.icons.lucide.ArrowUp
import com.composables.icons.lucide.Bold
import com.composables.icons.lucide.Bug
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.CircleQuestionMark
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.ClockArrowDown
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.FilePlus
import com.composables.icons.lucide.File
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.FileX
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.FolderOpen
import com.composables.icons.lucide.FolderPlus
import com.composables.icons.lucide.FolderSearch
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Hash
import com.composables.icons.lucide.Heading
import com.composables.icons.lucide.ImagePlus
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Italic
import com.composables.icons.lucide.Link
import com.composables.icons.lucide.List
import com.composables.icons.lucide.ListOrdered
import com.composables.icons.lucide.ListTodo
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquareQuote
import com.composables.icons.lucide.PanelLeftClose
import com.composables.icons.lucide.PanelLeftOpen
import com.composables.icons.lucide.Paperclip
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Quote
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.SeparatorHorizontal
import com.composables.icons.lucide.SquareArrowOutUpRight
import com.composables.icons.lucide.SquareCode
import com.composables.icons.lucide.Table
import com.composables.icons.lucide.TextQuote
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import com.composables.icons.lucide.Zap

/**
 * Every icon of the app, named by what it means rather than how it looks, so a screen asks for [NewNote] and the
 * glyph can change here alone. They come from Lucide, the set Obsidian users already know (`LIBRARIES.md` §2).
 */
object NotekeaIcons {
    val AttachFile: ImageVector get() = Lucide.ImagePlus
    val Attachment: ImageVector get() = Lucide.Paperclip
    val Bold: ImageVector get() = Lucide.Bold
    val BulletList: ImageVector get() = Lucide.List
    val Callout: ImageVector get() = Lucide.MessageSquareQuote
    val CalloutAbstract: ImageVector get() = Lucide.ClipboardList
    val CalloutBug: ImageVector get() = Lucide.Bug
    val CalloutDanger: ImageVector get() = Lucide.Zap
    val CalloutExample: ImageVector get() = Lucide.List
    val CalloutFailure: ImageVector get() = Lucide.X
    val CalloutInfo: ImageVector get() = Lucide.Info
    val CalloutNote: ImageVector get() = Lucide.Pencil
    val CalloutQuestion: ImageVector get() = Lucide.CircleQuestionMark
    val CalloutQuote: ImageVector get() = Lucide.Quote
    val CalloutSuccess: ImageVector get() = Lucide.Check
    val CalloutTip: ImageVector get() = Lucide.Flame
    val CalloutTodo: ImageVector get() = Lucide.CircleCheck
    val CalloutWarning: ImageVector get() = Lucide.TriangleAlert
    val Checked: ImageVector get() = Lucide.Check
    val Checklist: ImageVector get() = Lucide.ListTodo
    val Close: ImageVector get() = Lucide.X
    val CodeBlock: ImageVector get() = Lucide.SquareCode
    val Collapse: ImageVector get() = Lucide.ChevronDown
    val Crash: ImageVector get() = Lucide.TriangleAlert
    val Delete: ImageVector get() = Lucide.Trash2
    val Divider: ImageVector get() = Lucide.SeparatorHorizontal
    val Expand: ImageVector get() = Lucide.ChevronRight
    val Find: ImageVector get() = Lucide.Search
    val FindNext: ImageVector get() = Lucide.ArrowDown
    val FindPrevious: ImageVector get() = Lucide.ArrowUp
    val ImageFile: ImageVector get() = Lucide.Image
    val Folder: ImageVector get() = Lucide.Folder
    val FolderOpen: ImageVector get() = Lucide.FolderOpen
    val Heading: ImageVector get() = Lucide.Heading
    val HideSidePanel: ImageVector get() = Lucide.PanelRightClose
    val HideSidebar: ImageVector get() = Lucide.PanelLeftClose
    val Italic: ImageVector get() = Lucide.Italic
    val Locate: ImageVector get() = Lucide.FolderSearch
    val MissingFile: ImageVector get() = Lucide.FileX
    val More: ImageVector get() = Lucide.Ellipsis
    val NewFolder: ImageVector get() = Lucide.FolderPlus
    val NewNote: ImageVector get() = Lucide.FilePlus
    val NewTag: ImageVector get() = Lucide.Plus
    val Note: ImageVector get() = Lucide.FileText
    val OtherFile: ImageVector get() = Lucide.File
    val NoteLink: ImageVector get() = Lucide.Link
    val NumberedList: ImageVector get() = Lucide.ListOrdered
    val Outline: ImageVector get() = Lucide.ListTree
    val Quote: ImageVector get() = Lucide.TextQuote
    val Rename: ImageVector get() = Lucide.Pencil
    val Reveal: ImageVector get() = Lucide.SquareArrowOutUpRight
    val ShowSidePanel: ImageVector get() = Lucide.PanelRightOpen
    val ShowSidebar: ImageVector get() = Lucide.PanelLeftOpen
    val SortByModified: ImageVector get() = Lucide.ClockArrowDown
    val SortByName: ImageVector get() = Lucide.ArrowDownAZ
    val Table: ImageVector get() = Lucide.Table
    val Tag: ImageVector get() = Lucide.Hash
    val WebLink: ImageVector get() = Lucide.Globe
}

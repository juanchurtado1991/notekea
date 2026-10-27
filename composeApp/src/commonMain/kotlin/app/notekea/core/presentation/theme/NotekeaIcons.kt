package app.notekea.core.presentation.theme

import app.notekea.core.domain.StringPatterns
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import com.composables.icons.lucide.Type
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.CalendarPlus
import com.composables.icons.lucide.Code
import com.composables.icons.lucide.Braces
import com.composables.icons.lucide.Binary
import com.composables.icons.lucide.SquareCheck
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.ChevronsDownUp
import com.composables.icons.lucide.ChevronsUpDown
import com.composables.icons.lucide.FolderTree
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ListTree
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
import com.composables.icons.lucide.LayoutTemplate
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
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
    private const val BOOKMARKED_NAME = "Bookmarked"
    private const val ICON_SIZE_DP = 24
    private const val ICON_VIEWPORT = 24f
    private const val STAR_STROKE = 2f
    private val STAR_PATH = listOf(
        "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904",
        "l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0",
        "L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906",
        "l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"
    ).joinToString(separator = StringPatterns.EMPTY)

    val AddProperty: ImageVector get() = Lucide.Plus
    val AttachFile: ImageVector get() = Lucide.ImagePlus
    val Attachment: ImageVector get() = Lucide.Paperclip
    val Bookmark: ImageVector get() = Lucide.Star

    /** The bookmark star filled, for what is bookmarked: Lucide's star drawn with a fill. */
    val Bookmarked: ImageVector by lazy {
        ImageVector.Builder(
            name = BOOKMARKED_NAME,
            defaultWidth = ICON_SIZE_DP.dp,
            defaultHeight = ICON_SIZE_DP.dp,
            viewportWidth = ICON_VIEWPORT,
            viewportHeight = ICON_VIEWPORT
        ).addPath(
            pathData = addPathNodes(pathStr = STAR_PATH),
            fill = SolidColor(value = Color.Black),
            stroke = SolidColor(value = Color.Black),
            strokeLineWidth = STAR_STROKE,
            strokeLineJoin = StrokeJoin.Round
        ).build()
    }
    val Bold: ImageVector get() = Lucide.Bold
    val BulletList: ImageVector get() = Lucide.List
    val Calendar: ImageVector get() = Lucide.Calendar
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
    val CollapseAll: ImageVector get() = Lucide.ChevronsDownUp
    val Crash: ImageVector get() = Lucide.TriangleAlert
    val Delete: ImageVector get() = Lucide.Trash2
    val Divider: ImageVector get() = Lucide.SeparatorHorizontal
    val Expand: ImageVector get() = Lucide.ChevronRight
    val ExpandAll: ImageVector get() = Lucide.ChevronsUpDown
    val Files: ImageVector get() = Lucide.FolderTree
    val Find: ImageVector get() = Lucide.Search
    val FindNext: ImageVector get() = Lucide.ArrowDown
    val FindPrevious: ImageVector get() = Lucide.ArrowUp
    val ImageFile: ImageVector get() = Lucide.Image
    val Minimize: ImageVector get() = Lucide.Minus
    val Folder: ImageVector get() = Lucide.Folder
    val FolderOpen: ImageVector get() = Lucide.FolderOpen
    val Heading: ImageVector get() = Lucide.Heading
    val InfoCreated: ImageVector get() = Lucide.CalendarPlus
    val InfoLinks: ImageVector get() = Lucide.Link
    val InfoModified: ImageVector get() = Lucide.Pencil
    val InfoReading: ImageVector get() = Lucide.Clock
    val InfoTags: ImageVector get() = Lucide.Hash
    val InfoTasks: ImageVector get() = Lucide.SquareCheck
    val InfoWords: ImageVector get() = Lucide.Type
    val HideSidebar: ImageVector get() = Lucide.PanelLeftClose
    val Italic: ImageVector get() = Lucide.Italic
    val Locate: ImageVector get() = Lucide.FolderSearch
    val MissingFile: ImageVector get() = Lucide.FileX
    val More: ImageVector get() = Lucide.Ellipsis
    val NewFolder: ImageVector get() = Lucide.FolderPlus
    val NewNote: ImageVector get() = Lucide.FilePlus
    val NewTab: ImageVector get() = Lucide.Plus
    val NewTag: ImageVector get() = Lucide.Plus
    val NextDay: ImageVector get() = Lucide.ChevronRight
    val NextMonth: ImageVector get() = Lucide.ChevronRight
    val Note: ImageVector get() = Lucide.FileText
    val OtherFile: ImageVector get() = Lucide.File
    val NoteLink: ImageVector get() = Lucide.Link
    val NumberedList: ImageVector get() = Lucide.ListOrdered
    val Outline: ImageVector get() = Lucide.ListTree
    val PreviousDay: ImageVector get() = Lucide.ChevronLeft
    val PreviousMonth: ImageVector get() = Lucide.ChevronLeft
    val Properties: ImageVector get() = Lucide.SlidersHorizontal
    val PropertyCheckbox: ImageVector get() = Lucide.SquareCheck
    val PropertyDate: ImageVector get() = Lucide.Calendar
    val PropertyList: ImageVector get() = Lucide.List
    val PropertyNumber: ImageVector get() = Lucide.Binary
    val PropertyOther: ImageVector get() = Lucide.Braces
    val PropertySource: ImageVector get() = Lucide.Code
    val PropertyText: ImageVector get() = Lucide.Type
    val Quote: ImageVector get() = Lucide.TextQuote
    val Rename: ImageVector get() = Lucide.Pencil
    val Reveal: ImageVector get() = Lucide.SquareArrowOutUpRight
    val ShowSidePanel: ImageVector get() = Lucide.PanelRightOpen
    val ShowSidebar: ImageVector get() = Lucide.PanelLeftOpen
    val SortByModified: ImageVector get() = Lucide.ClockArrowDown
    val SortByName: ImageVector get() = Lucide.ArrowDownAZ
    val Table: ImageVector get() = Lucide.Table
    val Today: ImageVector get() = Lucide.CalendarDays
    val Tag: ImageVector get() = Lucide.Hash
    val Template: ImageVector get() = Lucide.LayoutTemplate
    val WebLink: ImageVector get() = Lucide.Globe
}

package app.notekea.graph.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.GraphPalette
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.motionTween
import kotlin.math.max
import kotlin.math.min

private const val FIT_MARGIN = 0.42f
private const val MIN_ZOOM = 0.3f
private const val MAX_ZOOM = 4f
private const val ZOOM_STEP = 0.1f
private const val HIT_MARGIN = 8f
private const val PARALLAX_PER_DEPTH = 0.08f
private const val VAULT_LABEL_ZOOM = 1.6f
private const val MAX_SEMANTIC_DOTS = 120
private const val LABEL_BACKGROUND_ALPHA = 0.82f

/**
 * The map of the graph (`ROADMAP.md` 2.7): drag to pan, scroll to zoom, a click selects and a double click opens.
 * Hovering a note lights up its neighbors. Notes farther from the center are smaller, fainter and move a little less,
 * for a sense of depth, and every change moves notes to their new places ([GraphMotion]) and the map to its new
 * scale, unless motion is reduced.
 */
@Composable
internal fun GraphCanvas(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(value = IntSize.Zero) }
    var pan by remember { mutableStateOf(value = Offset.Zero) }
    var zoom by remember { mutableFloatStateOf(value = 1f) }
    var hovered by remember { mutableStateOf<RelativePath?>(value = null) }
    val scale = rememberFit(target = fitOf(state = state, size = size)) * zoom
    val motion = rememberMotion(state = state)
    val places = placesOf(dots = state.dots, motion = motion, size = size, pan = pan, scale = scale)
    val current by rememberUpdatedState(newValue = places)
    val dots by rememberUpdatedState(newValue = state.dots)
    val currentScale by rememberUpdatedState(newValue = scale)
    RevealNote(state.reveals, pan = { pan }, zoom = { zoom }) { newPan, newZoom ->
        pan = newPan
        zoom = newZoom
    }
    val hit = { at: Offset -> noteAt(point = at, dots = dots, places = current, scale = currentScale) }
    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .mapGestures(
                hit = hit,
                onIntent = onIntent,
                onPan = { pan += it },
                onScroll = { zoom = zoomedBy(zoom = zoom, scroll = it) },
                onHover = { hovered = it }
            )
    ) {
        Map(state = state, motion = motion, places = places, scale = scale, zoom = zoom, hovered = hovered)
        if (state.dots.size <= MAX_SEMANTIC_DOTS) NoteTargets(state, places, scale, onIntent)
    }
}

/** Drag to pan, scroll to zoom, a click selects, a double click opens, and hovering says which note is under. */
private fun Modifier.mapGestures(
    hit: (Offset) -> RelativePath?,
    onIntent: (GraphIntent) -> Unit,
    onPan: (Offset) -> Unit,
    onScroll: (Float) -> Unit,
    onHover: (RelativePath?) -> Unit
): Modifier = pointerInput(onIntent) {
    detectTapGestures(
        onTap = { at -> hit(at)?.let { onIntent(GraphIntent.OnNoteSelect(path = it)) } },
        onDoubleTap = { at -> hit(at)?.let { onIntent(GraphIntent.OnNoteOpen(path = it)) } }
    )
}.pointerInput(Unit) {
    detectDragGestures { _, drag -> onPan(drag) }
}.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.first()
            if (event.type == PointerEventType.Move) onHover(hit(change.position))
            if (event.type == PointerEventType.Exit) onHover(null)
            if (event.type == PointerEventType.Scroll) onScroll(change.scrollDelta.y)
        }
    }
}

private fun zoomedBy(
    zoom: Float,
    scroll: Float
): Float = (zoom * (1f - scroll * ZOOM_STEP)).coerceIn(minimumValue = MIN_ZOOM, maximumValue = MAX_ZOOM)

/** Moving to a note found by search also takes the map back to the middle, at its usual zoom. */
@Composable
private fun RevealNote(
    reveals: Int,
    pan: () -> Offset,
    zoom: () -> Float,
    onMove: (Offset, Float) -> Unit
) {
    val spec = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_LONGEST_MILLIS)
    LaunchedEffect(reveals) {
        if (reveals == 0) return@LaunchedEffect
        val startPan = pan()
        val startZoom = zoom()
        animate(initialValue = 0f, targetValue = 1f, animationSpec = spec) { value, _ ->
            onMove(lerp(start = startPan, stop = Offset.Zero, fraction = value), lerp(startZoom, 1f, value))
        }
    }
}

/** The scale that fits the notes, moving to a new one, except from the first, when the map had no size yet. */
@Composable
private fun rememberFit(
    target: Float
): Float {
    val fit = remember { Animatable(initialValue = target) }
    val spec = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_LONGEST_MILLIS)
    LaunchedEffect(target) {
        if (fit.value == 0f) fit.snapTo(targetValue = target) else fit.animateTo(target, animationSpec = spec)
    }
    return fit.value
}

/** The move that goes on now, a new one each time the notes change. */
@Composable
private fun rememberMotion(
    state: GraphState
): GraphMotion {
    var last by remember { mutableStateOf<GraphMotion?>(value = null) }
    val motion = remember(state.dots) { GraphMotion.after(dots = state.dots, previous = last, center = state.center) }
    val spec = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_LONGEST_MILLIS)
    SideEffect { last = motion }
    LaunchedEffect(motion) { motion.progress.animateTo(targetValue = 1f, animationSpec = spec) }
    return motion
}

/** Where each note is drawn now, those leaving too. */
private fun placesOf(
    dots: List<GraphDot>,
    motion: GraphMotion,
    size: IntSize,
    pan: Offset,
    scale: Float
): Map<RelativePath, Offset> {
    val middle = Offset(x = size.width / 2f, y = size.height / 2f)
    return (dots + motion.leaving).associate { dot ->
        dot.path to middle + motion.placeOf(dot = dot) * scale + pan * (1f - dot.depth * PARALLAX_PER_DEPTH)
    }
}

/** The scale that fits every note in [size], never above their own size. */
private fun fitOf(
    state: GraphState,
    size: IntSize
): Float {
    val reach = state.dots.maxOfOrNull { max(kotlin.math.abs(it.x), kotlin.math.abs(it.y)) } ?: 1f
    return min(a = if (reach <= 0f) 1f else min(size.width, size.height) * FIT_MARGIN / reach, b = 1f)
}

private fun noteAt(
    point: Offset,
    dots: List<GraphDot>,
    places: Map<RelativePath, Offset>,
    scale: Float
): RelativePath? = dots.lastOrNull { dot ->
    val place = places[dot.path]
    place != null && (place - point).getDistance() <= dot.radius * scale + HIT_MARGIN
}?.path

@Composable
private fun Map(
    state: GraphState,
    motion: GraphMotion,
    places: Map<RelativePath, Offset>,
    scale: Float,
    zoom: Float,
    hovered: RelativePath?
) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer(cacheSize = MAX_SEMANTIC_DOTS)
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = colors.onSurface)
    val paint = GraphPaint(
        places = places,
        scale = scale,
        hovered = hovered?.let { neighborhoodOf(path = it, lines = state.lines) }.orEmpty(),
        selected = state.selected,
        matches = remember(state.matches, state.query) {
            state.matches.toSet().takeIf { state.query.isNotBlank() }
        },
        showsAllLabels = state.scope == GraphScope.THIS_NOTE || zoom >= VAULT_LABEL_ZOOM,
        lineColor = colors.outline,
        accent = colors.primary,
        labelStyle = labelStyle,
        labelBackground = colors.surface.copy(alpha = LABEL_BACKGROUND_ALPHA),
        colorOf = { dot -> folderColor(folder = dot.folder, folders = state.folders, root = colors.onSurfaceVariant) }
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawDots(dots = motion.leaving, paint = paint, measurer = measurer, fade = motion.fade)
        drawLines(lines = state.lines, paint = paint)
        drawDots(dots = state.dots.sortedBy { -it.depth }, paint = paint, measurer = measurer, fade = 1f)
    }
}

/** The color of [folder] in the graph and its legend. */
internal fun folderColor(
    folder: String,
    folders: List<String>,
    root: Color
): Color = folders.indexOf(element = folder).takeIf { it >= 0 }?.let { GraphPalette.colorAt(index = it) } ?: root

private fun neighborhoodOf(
    path: RelativePath,
    lines: List<GraphLine>
): Set<RelativePath> = lines.filter { it.first == path || it.second == path }
    .flatMapTo(destination = hashSetOf(path)) { listOf(it.first, it.second) }

/** What a screen reader finds on the map, one target per note, in place: a canvas has no elements of its own. */
@Composable
private fun NoteTargets(
    state: GraphState,
    places: Map<RelativePath, Offset>,
    scale: Float,
    onIntent: (GraphIntent) -> Unit
) {
    val density = LocalDensity.current
    state.dots.forEach { dot ->
        val place = places[dot.path] ?: return@forEach
        val radius = dot.radius * scale
        val side = with(density) { (radius * 2).toDp() }
        Box(
            modifier = Modifier
                .offset { IntOffset(x = (place.x - radius).toInt(), y = (place.y - radius).toInt()) }
                .size(size = side)
                .semantics {
                    contentDescription = dot.name
                    role = Role.Button
                    selected = dot.path == state.selected
                    onClick {
                        onIntent(GraphIntent.OnNoteSelect(path = dot.path))
                        true
                    }
                }
        )
    }
}

@Preview
@Composable
private fun GraphCanvasPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(size = PREVIEW_SIZE_DP.dp)) {
            GraphCanvas(state = GraphPreviewData.state, onIntent = {}, modifier = Modifier.fillMaxSize())
        }
    }
}

private const val PREVIEW_SIZE_DP = 420

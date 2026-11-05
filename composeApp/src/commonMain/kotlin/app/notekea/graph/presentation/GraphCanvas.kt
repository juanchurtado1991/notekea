package app.notekea.graph.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.GraphPalette
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.motionTween
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

/**
 * The map of the graph (`ROADMAP.md` 2.7): drag to pan, scroll to zoom, a click selects and a double click opens.
 * Hovering a note lights up its neighbors. Notes farther from the center are smaller, fainter and move a little less,
 * for a sense of depth, and every change slides notes to their new places, unless motion is reduced. A note found
 * by search slides to the middle of the map.
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
    val places = rememberPlaces(state = state, size = size, pan = pan, zoom = zoom)
    val current by rememberUpdatedState(newValue = places)
    val dots by rememberUpdatedState(newValue = state.dots)
    RevealNote(state = state, scale = scaleOf(state, size, zoom), pan = { pan }, onPan = { pan = it })
    val hit = { at: Offset -> noteAt(point = at, dots = dots, places = current, scale = scaleOf(state, size, zoom)) }
    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(onIntent) {
                detectTapGestures(
                    onTap = { at -> hit(at)?.let { onIntent(GraphIntent.OnNoteSelect(path = it)) } },
                    onDoubleTap = { at -> hit(at)?.let { onIntent(GraphIntent.OnOpenSelected) } }
                )
            }
            .pointerInput(Unit) { detectDragGestures { _, drag -> pan += drag } }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        if (event.type == PointerEventType.Move) hovered = hit(change.position)
                        if (event.type == PointerEventType.Scroll) {
                            zoom = (zoom * (1f - change.scrollDelta.y * ZOOM_STEP))
                                .coerceIn(minimumValue = MIN_ZOOM, maximumValue = MAX_ZOOM)
                        }
                    }
                }
            }
    ) {
        Map(state = state, places = places, scale = scaleOf(state, size, zoom), zoom = zoom, hovered = hovered)
        if (state.dots.size <= MAX_SEMANTIC_DOTS) NoteTargets(state, places, scaleOf(state, size, zoom), onIntent)
    }
}

/** Brings [GraphState.revealed] to the middle of the map; around a note it is on its way to the center already. */
@Composable
private fun RevealNote(
    state: GraphState,
    scale: Float,
    pan: () -> Offset,
    onPan: (Offset) -> Unit
) {
    val spec = motionTween<Offset>(durationMillis = NotekeaDimensions.MOTION_LONGEST_MILLIS)
    LaunchedEffect(state.revealed) {
        val dot = state.dots.firstOrNull { it.path == state.revealed }
        if (state.revealed == null) return@LaunchedEffect
        val place = dot?.takeIf { state.scope == GraphScope.WHOLE_VAULT }?.let { Offset(x = it.x, y = it.y) }
        animate(
            typeConverter = Offset.VectorConverter,
            initialValue = pan(),
            targetValue = -(place ?: Offset.Zero) * scale,
            animationSpec = spec
        ) { value, _ -> onPan(value) }
    }
}

/** Where each note is drawn now, sliding from where it was. */
@Composable
private fun rememberPlaces(
    state: GraphState,
    size: IntSize,
    pan: Offset,
    zoom: Float
): Map<RelativePath, Offset> {
    val progress = remember { Animatable(initialValue = 1f) }
    var previous by remember { mutableStateOf<Map<RelativePath, Offset>>(value = emptyMap()) }
    val spec = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_LONGEST_MILLIS)
    LaunchedEffect(state.dots) {
        progress.snapTo(targetValue = 0f)
        progress.animateTo(targetValue = 1f, animationSpec = spec)
        previous = state.dots.associate { it.path to Offset(x = it.x, y = it.y) }
    }
    val scale = scaleOf(state, size, zoom)
    val middle = Offset(x = size.width / 2f, y = size.height / 2f)
    return state.dots.associate { dot ->
        val target = Offset(x = dot.x, y = dot.y)
        val from = previous[dot.path] ?: target
        val place = from + (target - from) * progress.value
        dot.path to middle + place * scale + pan * (1f - dot.depth * PARALLAX_PER_DEPTH)
    }
}

private fun scaleOf(
    state: GraphState,
    size: IntSize,
    zoom: Float
): Float {
    val reach = state.dots.maxOfOrNull { max(kotlin.math.abs(it.x), kotlin.math.abs(it.y)) } ?: 1f
    val fit = if (reach <= 0f) 1f else min(size.width, size.height) * FIT_MARGIN / reach
    return min(a = fit, b = 1f) * zoom
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
        colorOf = { dot -> folderColor(folder = dot.folder, folders = state.folders, root = colors.onSurfaceVariant) }
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLines(lines = state.lines, paint = paint)
        drawDots(
            dots = state.dots.sortedBy { -it.depth },
            paint = paint,
            measurer = measurer
        )
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

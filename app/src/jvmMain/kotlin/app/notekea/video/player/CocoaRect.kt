package app.notekea.video.player

import com.sun.jna.Structure

/** AppKit's CGRect, passed by value: a view's place in its parent, in points. */
@Structure.FieldOrder("x", "y", "width", "height")
internal class CocoaRect(
    @JvmField var x: Double = 0.0,
    @JvmField var y: Double = 0.0,
    @JvmField var width: Double = 0.0,
    @JvmField var height: Double = 0.0
) : Structure(),
    Structure.ByValue

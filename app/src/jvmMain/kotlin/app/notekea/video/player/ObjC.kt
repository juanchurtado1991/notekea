package app.notekea.video.player

import com.sun.jna.Function
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

/**
 * Objective-C's runtime through JNA, for the few AppKit and WebKit calls the YouTube player makes on macOS
 * ([MacWebView]): a class by its name, and a message sent to an object with its arguments as C passes them.
 */
internal object ObjC {
    private const val RUNTIME = "objc"
    private const val UTF_8 = "UTF-8"
    private const val BOOL_MASK = 0xFF
    private val runtime by lazy { NativeLibrary.getInstance(RUNTIME) }
    private val messageSend: Function by lazy { runtime.getFunction("objc_msgSend") }

    fun classOf(name: String): Pointer? = runtime
        .getFunction("objc_getClass")
        .invokePointer(arrayOf(name))

    /** Sends [selector] to [target] and gives the object it answers, null for nil. */
    fun send(
        target: Pointer,
        selector: String,
        vararg arguments: Any?
    ): Pointer? =
        messageSend.invokePointer(arrayOf(target, selectorOf(name = selector), *arguments))

    /** Sends [selector] to [target], which answers nothing. */
    fun tell(
        target: Pointer,
        selector: String,
        vararg arguments: Any?
    ) {
        messageSend.invoke(arrayOf(target, selectorOf(name = selector), *arguments))
    }

    /** Sends [selector] to [target], which answers yes or no. */
    fun ask(
        target: Pointer,
        selector: String,
        vararg arguments: Any?
    ): Boolean {
        val answer = messageSend.invokeInt(arrayOf(target, selectorOf(name = selector), *arguments))

        return answer and BOOL_MASK != 0
    }

    /** The objects of the NSArray [array], in its order. */
    fun items(array: Pointer): List<Pointer> {
        val count = messageSend.invokeLong(arrayOf(array, selectorOf(name = "count")))

        return (0 until count).mapNotNull { send(array, "objectAtIndex:", it) }
    }

    /** [text] as an NSString, autoreleased. */
    fun stringOf(text: String): Pointer? {
        val nsString = classOf(name = "NSString") ?: return null

        return send(nsString, "stringWithUTF8String:", text)
    }

    /** The text of the NSString [string]. */
    fun textOf(string: Pointer?): String? = string
        ?.let { send(it, "UTF8String") }
        ?.getString(0, UTF_8)

    /** The selector named [name], as `respondsToSelector:` takes it. */
    fun selectorOf(name: String): Pointer = runtime
        .getFunction("sel_registerName")
        .invokePointer(arrayOf(name))
}

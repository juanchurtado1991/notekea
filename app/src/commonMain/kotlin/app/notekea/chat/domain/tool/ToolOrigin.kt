package app.notekea.chat.domain.tool

/** Where a tool's result comes from: what it says may be reported, and only the person's own may be acted on. */
enum class ToolOrigin {
    /** The person's notes and documents. */
    VAULT,

    /** A web page or a clipped source: text someone else wrote, never an instruction nor an address to follow. */
    UNTRUSTED
}

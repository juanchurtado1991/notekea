package app.notekea.chat.domain

/** Which notes a question is asked of: the whole vault, the open note, or the open note's folder. */
enum class ChatScope {
    VAULT,
    NOTE,
    FOLDER
}

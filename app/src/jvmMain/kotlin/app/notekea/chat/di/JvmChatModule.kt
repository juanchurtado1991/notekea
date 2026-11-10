package app.notekea.chat.di

import app.notekea.chat.data.FileChatHistoryStore
import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.core.platform.domain.PlatformConventions
import org.koin.dsl.module
import java.nio.file.Path

/** The chat's history, one file per vault in the app data's `chat` folder. */
val jvmChatModule = module {
    single<ChatHistoryStore> {
        FileChatHistoryStore(
            folder = Path.of(get<PlatformConventions>().appDataDirectory, CHAT_FOLDER),
            dispatchers = get()
        )
    }
}

private const val CHAT_FOLDER = "chat"

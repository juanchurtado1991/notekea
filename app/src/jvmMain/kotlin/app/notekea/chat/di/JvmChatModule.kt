package app.notekea.chat.di

import app.notekea.chat.data.FileChatHistoryStore
import app.notekea.chat.data.SqliteStepMemory
import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.chat.domain.context.StepMemory
import app.notekea.core.platform.domain.PlatformConventions
import org.koin.dsl.module
import java.nio.file.Path

/** The chat's history, one file per vault in the app data's `chat` folder; the loop's steps kept in SQLite. */
val jvmChatModule = module {
    single<ChatHistoryStore> {
        FileChatHistoryStore(
            folder = Path.of(get<PlatformConventions>().appDataDirectory, CHAT_FOLDER),
            dispatchers = get()
        )
    }

    single<StepMemory> { SqliteStepMemory() }
}

private const val CHAT_FOLDER = "chat"

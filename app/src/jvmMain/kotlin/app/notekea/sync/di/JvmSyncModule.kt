package app.notekea.sync.di

import app.notekea.sync.data.JGitRepository
import app.notekea.sync.data.KeyringTokenStore
import app.notekea.sync.domain.GitRepository
import app.notekea.sync.domain.TokenStore
import org.koin.dsl.module

/** Git sync on the desktop: JGit for the repository, the system's keychain for the token. */
val jvmSyncModule = module {
    single<GitRepository> { JGitRepository(dispatchers = get()) }
    single<TokenStore> { KeyringTokenStore(dispatchers = get()) }
}

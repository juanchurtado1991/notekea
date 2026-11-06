package app.notekea.tasks.di

import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tasks.presentation.TasksViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The vault's tasks: listed from the note cache, written to their notes, with one view model per vault. */
val tasksModule = module {
    factoryOf(::ListTasksUseCase)
    factoryOf(::SetTaskDoneUseCase)
    viewModel { parameters ->
        TasksViewModel(
            vaultPath = parameters.get(),
            listTasks = get(),
            setTaskDone = get(),
            clock = get(),
            localCalendar = get(),
            dispatchers = get()
        )
    }
}

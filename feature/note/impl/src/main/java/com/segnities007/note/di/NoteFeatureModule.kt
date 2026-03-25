package com.segnities007.note.di

import com.segnities007.database.SecureDatabaseController
import com.segnities007.note.api.NoteVaultTransferPort
import com.segnities007.note.data.local.NoteLocalDataSource
import com.segnities007.note.data.local.NoteDatabaseProvider
import com.segnities007.note.data.repository.NoteRepositoryImpl
import com.segnities007.note.data.transfer.NoteVaultTransferPortImpl
import com.segnities007.note.domain.repository.NoteRepository
import com.segnities007.note.domain.usecase.DeleteNoteUseCase
import com.segnities007.note.domain.usecase.LoadNotesSnapshotUseCase
import com.segnities007.note.domain.usecase.SaveNoteUseCase
import com.segnities007.note.presentation.navigation.NoteNavigationEntryInstaller
import com.segnities007.note.presentation.viewmodel.NoteViewModel
import com.segnities007.navigation.NavigationEntryInstaller
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** ノート DB・リポジトリ・ボルト転送ポート・[NoteViewModel]・ナビインストーラ。 */
val noteFeatureModule = module {
    singleOf(::NoteNavigationEntryInstaller) bind NavigationEntryInstaller::class
    single { NoteDatabaseProvider(get()) }
    single<SecureDatabaseController> { get<NoteDatabaseProvider>() }
    single { NoteLocalDataSource(get()) }
    single<NoteRepository> { NoteRepositoryImpl(get(), get(), get()) }
    single<NoteVaultTransferPort> {
        NoteVaultTransferPortImpl(
            context = get(),
            dataCipher = get(),
            authRepository = get(),
            sensitiveOperationGuard = get()
        )
    }
    singleOf(::LoadNotesSnapshotUseCase)
    singleOf(::SaveNoteUseCase)
    singleOf(::DeleteNoteUseCase)
    viewModel { NoteViewModel(get(), get(), get(), get(), get()) }
}

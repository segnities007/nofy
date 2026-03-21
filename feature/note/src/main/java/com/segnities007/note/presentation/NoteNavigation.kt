package com.segnities007.note.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.navigation.Route
import com.segnities007.note.domain.repository.NoteRepository

fun EntryProviderScope<NavKey>.noteEntry(
    noteRepository: NoteRepository,
    authRepository: AuthRepository,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    entry<Route.NoteList> {
        NoteScreen(
            noteRepository = noteRepository,
            authRepository = authRepository,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

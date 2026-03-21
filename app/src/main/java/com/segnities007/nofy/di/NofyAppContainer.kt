package com.segnities007.nofy.di

import android.content.Context
import com.segnities007.auth.data.repository.AuthRepositoryImpl
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.crypto.DataCipher
import com.segnities007.crypto.PasswordHasher
import com.segnities007.database.DatabaseProvider
import com.segnities007.datastore.SecureAuthStorage
import com.segnities007.datastore.SecureUiSettingsRepository
import com.segnities007.note.data.repository.NoteRepositoryImpl
import com.segnities007.note.domain.repository.NoteRepository
import com.segnities007.settings.UiSettingsRepository

class NofyAppContainer(
    context: Context
) {
    private val appContext = context.applicationContext
    private val secureAuthStorage = SecureAuthStorage(appContext)
    private val passwordHasher = PasswordHasher()
    private val databaseProvider = DatabaseProvider(appContext)
    private val dataCipher = DataCipher()

    val uiSettingsRepository: UiSettingsRepository =
        SecureUiSettingsRepository(secureAuthStorage)

    val authRepository: AuthRepository =
        AuthRepositoryImpl(secureAuthStorage, passwordHasher, databaseProvider)

    val noteRepository: NoteRepository =
        NoteRepositoryImpl(databaseProvider, dataCipher)
}

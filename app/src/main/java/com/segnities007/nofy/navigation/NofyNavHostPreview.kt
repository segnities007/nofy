package com.segnities007.nofy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.api.LoginRoute
import com.segnities007.navigation.AppNavigator
import com.segnities007.navigation.NavigationEntryInstaller
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@NofyPreview
@Composable
internal fun NofyNavHostPreview() {
    NofyPreviewSurface {
        NofyNavHost(
            authRepository = PreviewAuthRepository,
            entryInstallers = listOf(previewNavigationEntryInstaller())
        )
    }
}

internal fun previewNavigationEntryInstaller(): NavigationEntryInstaller {
    return NavigationEntryInstaller { scope: EntryProviderScope<NavKey>, _: AppNavigator ->
        with(scope) {
            entry<LoginRoute.Login> {
                NofySurface {
                    NofyText(text = "Navigation preview")
                }
            }
        }
    }
}

private object PreviewAuthRepository : AuthRepository {
    override fun isRegistered(): Flow<Boolean> = flowOf(true)

    override fun isBiometricEnabled(): Flow<Boolean> = flowOf(false)

    override fun isLocked(): Flow<Boolean> = flowOf(true)

    override suspend fun verifyPassword(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun lock(): Result<Unit> = Result.success(Unit)

    override suspend fun unlock(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun unlockWithBiometric(decryptedPassword: String): Result<Unit> = Result.success(Unit)

    override suspend fun registerPassword(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun saveBiometricSecret(
        encryptedSecret: ByteArray,
        iv: ByteArray
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getBiometricSecret(): Pair<ByteArray, ByteArray>? = null

    override suspend fun clearBiometricSecret(): Result<Unit> = Result.success(Unit)

    override suspend fun reset(currentPassword: String): Result<Unit> = Result.success(Unit)

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = Result.success(Unit)

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = Result.success(Unit)

    override suspend fun adoptImportedVault(password: String): Result<Unit> = Result.success(Unit)
}

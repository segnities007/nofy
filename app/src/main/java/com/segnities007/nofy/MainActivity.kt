package com.segnities007.nofy

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.nofy.navigation.NofyNavHost
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.settings.UiSettingsRepository
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val uiSettingsRepository: UiSettingsRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val navigationEntryInstallers by lazy {
        getKoin().getAll<NavigationEntryInstaller>()
    }
    private val appLockObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            lifecycleScope.launch {
                authRepository.lock()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLockObserver)
        setContent {
            val uiSettings by uiSettingsRepository.settings.collectAsStateWithLifecycle()

            NofyTheme(settings = uiSettings) {
                NofyNavHost(
                    authRepository = authRepository,
                    entryInstallers = navigationEntryInstallers,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLockObserver)
        super.onDestroy()
    }
}

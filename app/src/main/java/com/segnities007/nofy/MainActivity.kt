package com.segnities007.nofy

import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.auth.domain.usecase.LockApplicationUseCase
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.nofy.navigation.NofyNavHost
import com.segnities007.nofy.security.RiskyEnvironment
import com.segnities007.nofy.security.RiskyEnvironmentDetector
import com.segnities007.nofy.security.RiskyEnvironmentSnapshotHolder
import com.segnities007.nofy.security.RiskyEnvironmentScreen
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.settings.UiSettingsRepository
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 単一 Activity。危険環境ブロック、バックグラウンド時の自動ロック、[NofyNavHost] による Navigation3 を束ねる。
 */
class MainActivity : FragmentActivity() {
    private val uiSettingsRepository: UiSettingsRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val lockApplicationUseCase: LockApplicationUseCase by inject()
    private val riskyEnvironmentDetector: RiskyEnvironmentDetector by inject()
    private val riskyEnvironmentSnapshotHolder: RiskyEnvironmentSnapshotHolder by inject()
    private var riskyEnvironment by mutableStateOf<RiskyEnvironment?>(null)
    private var idleLockJob: Job? = null
    private var riskyEnvironmentMonitorJob: Job? = null
    private val navigationEntryInstallers by lazy {
        getKoin().getAll<NavigationEntryInstaller>()
    }
    private val appLockObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            lifecycleScope.launch {
                lockApplicationUseCase()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setHideOverlayWindows(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
        enableEdgeToEdge()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLockObserver)
        refreshRiskyEnvironment()
        lifecycleScope.launch {
            uiSettingsRepository.settings.collect {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    restartIdleLockTimer()
                }
            }
        }
        setContent {
            val uiSettings by uiSettingsRepository.settings.collectAsStateWithLifecycle()
            val currentRiskyEnvironment = riskyEnvironment

            NofyTheme(settings = uiSettings) {
                if (currentRiskyEnvironment != null) {
                    RiskyEnvironmentScreen(
                        environment = currentRiskyEnvironment,
                        onCloseApp = ::finishAffinity,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    NofyNavHost(
                        authRepository = authRepository,
                        entryInstallers = navigationEntryInstallers,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRiskyEnvironment()
        restartRiskyEnvironmentMonitor()
        restartIdleLockTimer()
    }

    override fun onStop() {
        cancelIdleLockTimer()
        cancelRiskyEnvironmentMonitor()
        super.onStop()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        restartIdleLockTimer()
    }

    override fun onDestroy() {
        cancelIdleLockTimer()
        cancelRiskyEnvironmentMonitor()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLockObserver)
        super.onDestroy()
    }

    private fun refreshRiskyEnvironment() {
        val detectedEnvironment = riskyEnvironmentDetector.detect()
        riskyEnvironmentSnapshotHolder.publish(detectedEnvironment)
        val wasRisky = riskyEnvironment != null
        val becameRisky = riskyEnvironment == null && detectedEnvironment != null
        riskyEnvironment = detectedEnvironment
        if (becameRisky) {
            cancelIdleLockTimer()
            lifecycleScope.launch {
                lockApplicationUseCase()
            }
            return
        }

        if (wasRisky && detectedEnvironment == null) {
            restartIdleLockTimer()
        }
    }

    private fun restartIdleLockTimer() {
        cancelIdleLockTimer()
        if (riskyEnvironment != null) {
            return
        }

        val millis = uiSettingsRepository.settings.value.idleLockTimeout.timeoutMillis
        idleLockJob = lifecycleScope.launch {
            delay(millis)
            lockApplicationUseCase()
        }
    }

    private fun restartRiskyEnvironmentMonitor() {
        cancelRiskyEnvironmentMonitor()
        riskyEnvironmentMonitorJob = lifecycleScope.launch {
            while (true) {
                delay(RiskyEnvironmentCheckIntervalMillis)
                refreshRiskyEnvironment()
            }
        }
    }

    private fun cancelRiskyEnvironmentMonitor() {
        riskyEnvironmentMonitorJob?.cancel()
        riskyEnvironmentMonitorJob = null
    }

    private fun cancelIdleLockTimer() {
        idleLockJob?.cancel()
        idleLockJob = null
    }

    private companion object {
        const val RiskyEnvironmentCheckIntervalMillis = 1_000L
    }
}

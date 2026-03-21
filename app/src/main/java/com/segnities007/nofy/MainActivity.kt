package com.segnities007.nofy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.nofy.di.NofyAppContainer
import com.segnities007.nofy.navigation.NofyNavHost

class MainActivity : FragmentActivity() {
    private val appContainer by lazy { NofyAppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val uiSettings by appContainer.uiSettingsRepository.settings.collectAsStateWithLifecycle()

            NofyTheme(settings = uiSettings) {
                NofyNavHost(
                    appContainer = appContainer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

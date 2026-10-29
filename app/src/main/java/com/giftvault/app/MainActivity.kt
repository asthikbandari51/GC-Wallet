package com.giftvault.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.giftvault.app.core.security.BiometricAuthManager
import com.giftvault.app.core.theme.ThemeManager
import com.giftvault.app.ui.navigation.GiftVaultBottomNavBar
import com.giftvault.app.ui.navigation.GiftVaultNavGraph
import com.giftvault.app.ui.onboarding.OnboardingScreen
import com.giftvault.app.ui.security.AppLockScreen
import com.giftvault.app.ui.theme.GiftVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by themeManager.themeMode.collectAsState()
            val isOnboardingCompleted by themeManager.isOnboardingCompleted.collectAsState()
            val isAppLockEnabled by themeManager.isAppLockEnabled.collectAsState()
            val isPrivacyMode by themeManager.isPrivacyModeEnabled.collectAsState()
            val userName by themeManager.userName.collectAsState()
            val appPin by themeManager.appPin.collectAsState()

            var isAppLocked by remember { mutableStateOf(isAppLockEnabled || appPin != null) }

            // Strict Data Security & Sandboxing: Prevent screen capture/recent apps preview in Privacy Mode or when Locked
            LaunchedEffect(isPrivacyMode, isAppLocked) {
                if (isPrivacyMode || isAppLocked) {
                    window.setFlags(
                        android.view.WindowManager.LayoutParams.FLAG_SECURE,
                        android.view.WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            GiftVaultTheme(themeMode = themeMode) {
                when {
                    !isOnboardingCompleted -> {
                        OnboardingScreen(
                            themeManager = themeManager,
                            onCompleted = { isAppLocked = false }
                        )
                    }
                    isAppLocked -> {
                        AppLockScreen(
                            userName = userName,
                            savedPin = appPin,
                            biometricAuthManager = biometricAuthManager,
                            onUnlocked = { isAppLocked = false }
                        )
                    }
                    else -> {
                        val navController = rememberNavController()

                        LaunchedEffect(intent) {
                            if (intent?.action == "com.giftvault.ACTION_INGEST_VOUCHER") {
                                navController.navigate(com.giftvault.app.ui.navigation.Screen.ADD_CARD.route)
                            }
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = { GiftVaultBottomNavBar(navController) }
                        ) { innerPadding ->
                            GiftVaultNavGraph(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

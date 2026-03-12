package com.dev.habitwallpaper

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dev.habitwallpaper.core.background.QuoteWorkScheduler
import com.dev.habitwallpaper.domain.repository.UserPreferencesRepository
import com.dev.habitwallpaper.features.habit.presentation.screen.MainScreen
import com.dev.habitwallpaper.ui.theme.HabitWallpaperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var quoteWorkScheduler: QuoteWorkScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start the periodic quote worker once; WorkManager deduplicates automatically.
        quoteWorkScheduler.scheduleQuoteNotifications()

        setContent {
            HabitWallpaperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()

                    // ── Post-onboarding notification permission flow ───────
                    // Only relevant on Android 13+; silently skipped on older versions.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val scope = rememberCoroutineScope()

                        val isOnboardingCompleted by userPreferencesRepository
                            .isOnboardingCompleted
                            .collectAsState(initial = false)

                        val permissionAlreadyRequested by userPreferencesRepository
                            .notificationPermissionRequested
                            .collectAsState(initial = true) // default true = don't ask until we know

                        var showRationaleDialog by remember { mutableStateOf(false) }

                        val permissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { _ ->
                            // We don't force the user — result is intentionally ignored.
                            scope.launch {
                                userPreferencesRepository.markNotificationPermissionRequested()
                            }
                        }

                        // Once onboarding is done AND we haven't asked yet, show the rationale.
                        LaunchedEffect(isOnboardingCompleted, permissionAlreadyRequested) {
                            if (isOnboardingCompleted && !permissionAlreadyRequested) {
                                showRationaleDialog = true
                            }
                        }

                        if (showRationaleDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    showRationaleDialog = false
                                    scope.launch {
                                        userPreferencesRepository.markNotificationPermissionRequested()
                                    }
                                },
                                title = { Text("Stay Motivated") },
                                text = {
                                    Text(
                                        "Enable notifications to receive daily motivational quotes " +
                                        "and reminders that keep your habit streak alive."
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showRationaleDialog = false
                                        permissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    }) { Text("Allow") }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showRationaleDialog = false
                                        scope.launch {
                                            userPreferencesRepository.markNotificationPermissionRequested()
                                        }
                                    }) { Text("Not Now") }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

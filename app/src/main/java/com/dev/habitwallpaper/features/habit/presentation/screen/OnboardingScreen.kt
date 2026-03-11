package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.habitwallpaper.R
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // ── Navigation side-effect (unchanged) ─────────────────────────────────
    LaunchedEffect(uiState.isNavigateToHome) {
        if (uiState.isNavigateToHome) {
            viewModel.onNavigationHandled()
            onNavigateToHome()
        }
    }

    // Derive overlay colors from MaterialTheme so we stay theme-consistent.
    // primary is ForestDeep (#1B4332), a dark green — perfect for the gradient.
    val overlayColor = MaterialTheme.colorScheme.primary

    // ── Root: full-screen Box layers background → gradient → content ────────
    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 1 — Hero background image (crops to fill every pixel)
        Image(
            painter = painterResource(id = R.drawable.onboarding_plant),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2 — Vertical gradient overlay: transparent at top → themed
        //           deep green at bottom so text has a solid readable backdrop.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Transparent,
                                0.30f to overlayColor.copy(alpha = 0.10f),
                                0.55f to overlayColor.copy(alpha = 0.65f),
                                0.75f to overlayColor.copy(alpha = 0.88f),
                                1.00f to overlayColor.copy(alpha = 0.97f)
                            )
                        )
                    )
                }
        )

        // Layer 3 — Foreground content, anchored to the bottom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // ── Title ──────────────────────────────────────────────────────
            Text(
                text = "Welcome to WallHabit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Subtitle ───────────────────────────────────────────────────
            Text(
                text = "Build consistency one day at a time",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Name input label ───────────────────────────────────────────
            Text(
                text = "What should we call you?",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            // ── Name input field ───────────────────────────────────────────
            // Uses a semi-transparent dark container so it's always readable
            // on top of the hero image regardless of the image region.
            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = { viewModel.onNameChanged(it) },
                placeholder = {
                    Text(
                        "Your name",
                        color = Color.White.copy(alpha = 0.40f)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.30f),
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    // Semi-transparent dark container for readability over the image
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onContinue()
                    }
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Continue button ────────────────────────────────────────────
            val isNameValid = uiState.nameInput.trim().isNotBlank()
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onContinue()
                },
                enabled = isNameValid && !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

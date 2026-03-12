package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.core.wallpaper.WallpaperManager
import com.dev.habitwallpaper.domain.model.WallpaperCustomization
import com.dev.habitwallpaper.domain.usecase.GenerateWallpaperStateUseCase
import com.dev.habitwallpaper.domain.usecase.GetWallpaperCustomizationUseCase
import com.dev.habitwallpaper.domain.usecase.ObserveWallpaperHabitUseCase
import com.dev.habitwallpaper.domain.usecase.UpdateWallpaperCustomizationUseCase
import com.dev.habitwallpaper.domain.usecase.WallpaperState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperCustomizationViewModel @Inject constructor(
    private val getWallpaperCustomizationUseCase: GetWallpaperCustomizationUseCase,
    private val updateWallpaperCustomizationUseCase: UpdateWallpaperCustomizationUseCase,
    private val observeWallpaperHabitUseCase: ObserveWallpaperHabitUseCase,
    private val generateWallpaperStateUseCase: GenerateWallpaperStateUseCase,
    private val wallpaperManager: WallpaperManager
) : ViewModel() {

    private val _customization = MutableStateFlow(WallpaperCustomization.Default)
    val customization: StateFlow<WallpaperCustomization> = _customization.asStateFlow()

    private val _previewState = MutableStateFlow<WallpaperState?>(null)
    val previewState: StateFlow<WallpaperState?> = _previewState.asStateFlow()

    init {
        viewModelScope.launch {
            getWallpaperCustomizationUseCase().collect {
                _customization.value = it
            }
        }

        observeWallpaperHabitUseCase()
            .onEach { habit ->
                _previewState.value = generateWallpaperStateUseCase(habit)
            }
            .launchIn(viewModelScope)
    }

    fun updateCustomization(newCustomization: WallpaperCustomization) {
        _customization.value = newCustomization
    }

    fun applyWallpaper() {
        viewModelScope.launch {
            updateWallpaperCustomizationUseCase(_customization.value)
            wallpaperManager.triggerUpdate()
        }
    }

    fun resetToDefault() {
        _customization.value = WallpaperCustomization.Default
    }
}

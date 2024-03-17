package io.github.auag0.applist.appdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.github.auag0.applist.main.AppItem

class AppDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val appItem = savedStateHandle.getStateFlow<AppItem?>("appItem", null)

    init {
    }
}
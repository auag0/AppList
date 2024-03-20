package io.github.auag0.applist.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.auag0.applist.core.utils.AppPrefsManager
import io.github.auag0.applist.core.utils.AppPrefsManager.AppSort.ByLastUpdateTime
import io.github.auag0.applist.core.utils.AppPrefsManager.AppSort.ByName
import io.github.auag0.applist.repository.domain.AppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appsRepository: AppsRepository
) : ViewModel() {
    private var _appList: List<AppItem> = emptyList()

    private val _filteredAppList = MutableStateFlow<List<AppItem>>(emptyList())
    val filteredAppList = _filteredAppList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var searchQuery: String? = null

    init {
        loadAppList()
    }

    fun setSearchQuery(searchQuery: String?) {
        this.searchQuery = searchQuery
        filterAndSortAppList()
    }

    fun loadAppList() {
        if (_isLoading.value) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.emit(true)
            _appList = emptyList()
            try {
                _appList = appsRepository.getInstalledApp()
                filterAndSortAppList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.emit(false)
            }
        }
    }

    fun filterAndSortAppList() {
        viewModelScope.launch(Dispatchers.Default) {
            var appList = _appList
            val predicate: ((AppItem) -> Boolean) = when (AppPrefsManager.appFilter) {
                AppPrefsManager.AppFilter.UserApps -> { appItem -> !appItem.isSystem }
                AppPrefsManager.AppFilter.SystemApps -> { appItem -> appItem.isSystem }
                AppPrefsManager.AppFilter.AllApps -> { _ -> true }
            }
            appList = appList.filter(predicate)

            val isSearching = !searchQuery.isNullOrBlank()
            if (isSearching) {
                appList = appList.filter { it.name.contains(searchQuery!!, true) }
            }

            val comparator: Comparator<AppItem> = if (isSearching) {
                compareBy { it.name.indexOf(searchQuery!!) }
            } else {
                when (AppPrefsManager.appSort) {
                    ByName -> Comparator { a, b ->
                        Collator.getInstance(Locale.getDefault()).compare(a.name, b.name)
                    }

                    ByLastUpdateTime -> compareByDescending { it.lastUpdateTime }
                }
            }
            appList = appList.sortedWith(comparator)
            _filteredAppList.emit(appList)
        }
    }
}
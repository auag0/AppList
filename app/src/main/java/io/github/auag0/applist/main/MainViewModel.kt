package io.github.auag0.applist.main

import android.app.Application
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageManagerHidden
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.rikka.tools.refine.Refine
import io.github.auag0.applist.utils.AppPrefsManager
import io.github.auag0.applist.utils.AppPrefsManager.AppSort.ByLastUpdateTime
import io.github.auag0.applist.utils.AppPrefsManager.AppSort.ByName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

class MainViewModel(app: Application) : AndroidViewModel(app) {
    data class Progress(
        val current: Int,
        val max: Int
    )

    private val pm = app.packageManager

    private var _appList: List<AppItem> = emptyList()

    private val _filteredAppList = MutableStateFlow<List<AppItem>>(emptyList())
    val filteredAppList = _filteredAppList.asStateFlow()

    private val _progress: MutableStateFlow<Progress?> = MutableStateFlow(null)
    val progress = _progress.asStateFlow()

    private var searchQuery: String? = null

    init {
        loadAppList()
    }

    fun setSearchQuery(searchQuery: String?) {
        this.searchQuery = searchQuery
        filterAndSortAppList()
    }

    fun loadAppList() {
        viewModelScope.launch(Dispatchers.IO) {
            _progress.emit(null)
            _appList = emptyList()
            try {
                val hiddenPM = Refine.unsafeCast<PackageManagerHidden>(pm)
                val installedPackages = hiddenPM.getInstalledPackagesAsUser(0, Process.myUserHandle().hashCode())
                val max = installedPackages.size
                _progress.emit(Progress(0, max))
                val appList = installedPackages.mapIndexed { index, packageInfo ->
                    _progress.emit(_progress.value?.copy(current = index))
                    val appInfo = packageInfo.applicationInfo
                    AppItem(
                        packageInfo = packageInfo,
                        name = appInfo.loadLabel(pm),
                        packageName = packageInfo.packageName,
                        isSystem = appInfo.flags and FLAG_SYSTEM == FLAG_SYSTEM,
                        lastUpdateTime = packageInfo.lastUpdateTime
                    )
                }
                _appList = appList
                filterAndSortAppList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _progress.emit(null)
            }
        }
    }

    fun filterAndSortAppList() {
        viewModelScope.launch(Dispatchers.IO) {
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
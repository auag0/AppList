package io.github.auag0.applist.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import io.github.auag0.applist.R
import io.github.auag0.applist.utils.AppPrefsManager.AppFilter.AllApps
import io.github.auag0.applist.utils.AppPrefsManager.AppFilter.SystemApps
import io.github.auag0.applist.utils.AppPrefsManager.AppFilter.UserApps
import io.github.auag0.applist.utils.AppPrefsManager.AppSort.ByLastUpdateTime
import io.github.auag0.applist.utils.AppPrefsManager.AppSort.ByName
import io.github.auag0.applist.utils.AppPrefsManager.appFilter
import io.github.auag0.applist.utils.AppPrefsManager.appSort
import io.github.auag0.applist.views.SearchBar
import io.github.auag0.applist.core.base.BaseActivity
import io.github.auag0.applist.core.utils.AppPrefsManager.AppFilter.AllApps
import io.github.auag0.applist.core.utils.AppPrefsManager.AppFilter.SystemApps
import io.github.auag0.applist.core.utils.AppPrefsManager.AppFilter.UserApps
import io.github.auag0.applist.core.utils.AppPrefsManager.AppSort.ByLastUpdateTime
import io.github.auag0.applist.core.utils.AppPrefsManager.AppSort.ByName
import io.github.auag0.applist.core.utils.AppPrefsManager.appFilter
import io.github.auag0.applist.core.utils.AppPrefsManager.appSort
import io.github.auag0.applist.core.views.SearchBar
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val searchBar: SearchBar = findViewById(R.id.searchBar)
        setSupportActionBar(searchBar)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = AppAdapter(viewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        FastScrollerBuilder(recyclerView)
            .useMd2Style()
            .setPopupTextProvider(adapter)
            .build()

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
                adapter.submitSearchQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val progressIndicator: LinearProgressIndicator = findViewById(R.id.linearProgressIndicator)
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        val progressBgColor = MaterialColors.getColor(
            swipeRefreshLayout,
            com.google.android.material.R.attr.colorPrimaryContainer
        )
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressBgColor)
        val progressColor = MaterialColors.getColor(
            swipeRefreshLayout,
            com.google.android.material.R.attr.colorPrimary
        )
        swipeRefreshLayout.setColorSchemeColors(progressColor)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadAppList()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredAppList.collect { appList ->
                        adapter.submitList(appList) {
                            recyclerView.scrollToPosition(0)
                        }
                    }
                }

                launch {
                    viewModel.progress.collect { progress ->
                        if (progress == null) {
                            progressIndicator.visibility = View.GONE
                            swipeRefreshLayout.isRefreshing = false
                        } else {
                            progressIndicator.visibility = View.VISIBLE
                            progressIndicator.max = progress.max
                            progressIndicator.setProgressCompat(progress.current, true)
                            swipeRefreshLayout.isRefreshing = true
                        }
                    }
                }
            }
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main, menu)
                menu.findItem(
                    when (appFilter) {
                        UserApps -> R.id.userApps
                        SystemApps -> R.id.systemApps
                        AllApps -> R.id.allApps
                    }
                ).isChecked = true

                menu.findItem(
                    when (appSort) {
                        ByName -> R.id.byName
                        ByLastUpdateTime -> R.id.byLastUpdateTime
                    }
                ).isChecked = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.userApps, R.id.systemApps, R.id.allApps -> {
                        menuItem.setChecked(!menuItem.isChecked)
                        appFilter = when (menuItem.itemId) {
                            R.id.userApps -> UserApps
                            R.id.systemApps -> SystemApps
                            R.id.allApps -> AllApps
                            else -> throw Exception()
                        }
                        viewModel.filterAndSortAppList()
                        true
                    }

                    R.id.byName, R.id.byLastUpdateTime -> {
                        menuItem.setChecked(!menuItem.isChecked)
                        appSort = when (menuItem.itemId) {
                            R.id.byName -> ByName
                            R.id.byLastUpdateTime -> ByLastUpdateTime
                            else -> throw Exception()
                        }
                        viewModel.filterAndSortAppList()
                        true
                    }

                    else -> false
                }
            }
        })
    }
}
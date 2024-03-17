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
import com.google.android.material.color.MaterialColors
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
import io.github.auag0.applist.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.searchBar)

        val adapter = AppAdapter(viewModel)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        FastScrollerBuilder(binding.recyclerView)
            .useMd2Style()
            .setPopupTextProvider(adapter)
            .build()

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
                adapter.submitSearchQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val progressBgColor = MaterialColors.getColor(
            binding.swipeRefreshLayout,
            com.google.android.material.R.attr.colorPrimaryContainer
        )
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressBgColor)
        val progressColor = MaterialColors.getColor(
            binding.swipeRefreshLayout,
            com.google.android.material.R.attr.colorPrimary
        )
        binding.swipeRefreshLayout.setColorSchemeColors(progressColor)
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadAppList()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredAppList.collect { appList ->
                        adapter.submitList(appList) {
                            binding.recyclerView.scrollToPosition(0)
                        }
                    }
                }

                launch {
                    viewModel.progress.collect { progress ->
                        if (progress == null) {
                            binding.linearProgressIndicator.visibility = View.GONE
                            binding.swipeRefreshLayout.isRefreshing = false
                        } else {
                            binding.linearProgressIndicator.visibility = View.VISIBLE
                            binding.linearProgressIndicator.max = progress.max
                            binding.linearProgressIndicator.setProgressCompat(
                                progress.current,
                                true
                            )
                            binding.swipeRefreshLayout.isRefreshing = true
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
package io.github.auag0.applist.appdetails

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.github.auag0.applist.core.base.BaseActivity
import io.github.auag0.applist.databinding.ActivityAppDetailsBinding
import io.github.auag0.applist.main.AppItem
import kotlinx.coroutines.launch

class AppDetailsActivity : BaseActivity() {
    private val viewModel: AppDetailsViewModel by viewModels()
    private lateinit var binding: ActivityAppDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        lifecycleScope.launch {
            launch {
                viewModel.appItem.collect { appItem ->
                    appItem?.let {
                        updateAppDetails(appItem)
                    } ?: finish()
                }
            }
        }
    }

    private fun updateAppDetails(appItem: AppItem) {
        Glide.with(this).asDrawable().load(appItem.packageInfo)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.toolbar.logo = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        binding.toolbar.title = appItem.name
        binding.toolbar.subtitle = appItem.packageName
    }
}
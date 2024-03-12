package io.github.auag0.applist

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.util.ObjectsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import io.github.auag0.applist.PrefManager.AppSort.ByLastUpdateTime
import io.github.auag0.applist.PrefManager.AppSort.ByName
import io.github.auag0.applist.PrefManager.appSort
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.util.Locale

class AppAdapter(
    private val viewModel: MainViewModel
) : ListAdapter<AppItem, AppAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<AppItem>() {
        override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.packageName.equals(newItem.packageName, true)
        }

        override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return ObjectsCompat.equals(oldItem, newItem)
        }
    }
), PopupTextProvider {
    private var currentAppSort: PrefManager.AppSort = ByName
    private var currentSearchQuery: String? = null

    private fun updateCurrentValue() {
        currentAppSort = appSort
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitSearchQuery(searchQuery: String) {
        currentSearchQuery = searchQuery
        notifyDataSetChanged()
    }

    override fun submitList(list: List<AppItem>?) {
        updateCurrentValue()
        super.submitList(list)
    }

    override fun submitList(list: List<AppItem>?, commitCallback: Runnable?) {
        updateCurrentValue()
        super.submitList(list, commitCallback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        private val appName: MaterialTextView = itemView.findViewById(R.id.app_name)
        private val appPackageName: MaterialTextView = itemView.findViewById(R.id.app_package_name)
        fun bind(appItem: AppItem) {
            Glide.with(appIcon).load(appItem.packageInfo).into(appIcon)
            appPackageName.text = appItem.packageName

            val name = appItem.name.toString().lowercase()
            val searchQuery = currentSearchQuery?.lowercase()
            if (searchQuery.isNullOrBlank()) {
                appName.text = name
            } else {
                val index = name.indexOf(searchQuery, ignoreCase = true)
                if (index != -1) {
                    val color = MaterialColors.getColor(
                        appName,
                        com.google.android.material.R.attr.colorPrimary
                    )
                    val spannable = SpannableStringBuilder(name)
                    spannable.setSpan(
                        ForegroundColorSpan(color),
                        index,
                        index + searchQuery.length,
                        0
                    )
                    appName.text = spannable
                } else {
                    appName.text = name
                }
            }

            val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.list_app_item)
            itemView.startAnimation(anim)
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    override fun getPopupText(view: View, position: Int): CharSequence {
        val item = getItem(position)
        return when (currentAppSort) {
            ByName -> item.name.first().uppercase(Locale.getDefault())
            ByLastUpdateTime -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = item.lastUpdateTime
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH) + 1
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                return "%04d/%02d/%02d".format(year, month, day)
            }
        }
    }
}
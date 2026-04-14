package com.hujiayucc.hook.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.hujiayucc.hook.R
import com.hujiayucc.hook.application.XYApplication
import com.hujiayucc.hook.data.Item2
import com.hujiayucc.hook.databinding.AppRuleBinding
import com.hujiayucc.hook.ui.activity.AppInfoActivity
import io.github.libxposed.service.XposedService
import java.text.Collator
import java.util.*

class AppListAdapter2(private var appList: List<Item2>) : BaseAdapter(), Filterable {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val appNameCollator = Collator.getInstance(Locale.CHINA)
    private var displayList: List<Item2> = appList
    private var filteredList: List<Item2> = appList
    private var currentFilterQuery: String = ""

    init {
        sortByScope()
        applyFilter(currentFilterQuery)
    }

    fun updateData(newList: List<Item2>) {
        appList = newList
        refreshSorted()
    }

    override fun getCount(): Int = filteredList.size
    override fun getItem(position: Int): Item2 = filteredList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    private fun sortByScope(scopedPackages: Set<String>? = null) {
        val currentScoped = scopedPackages ?: XYApplication.mService?.scope?.filterNotNull()?.toSet().orEmpty()
        displayList = appList.sortedWith { a, b ->
            val scopeCompare = compareValues(a.packageName !in currentScoped, b.packageName !in currentScoped)
            if (scopeCompare != 0) {
                scopeCompare
            } else {
                appNameCollator.compare(a.appName, b.appName)
            }
        }
    }

    private fun applyFilter(query: String) {
        val filterPattern = query.lowercase(Locale.getDefault()).trim()
        filteredList = if (filterPattern.isEmpty()) {
            displayList
        } else {
            displayList.filter { item ->
                item.appName.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        item.packageName.lowercase(Locale.getDefault()).contains(filterPattern)
            }
        }
    }

    private fun refreshSorted(scopedPackages: Set<String>? = null) {
        val refreshAction = Runnable {
            sortByScope(scopedPackages)
            applyFilter(currentFilterQuery)
            notifyDataSetChanged()
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            refreshAction.run()
        } else {
            mainHandler.post(refreshAction)
        }
    }

    private fun currentScopeSet(): MutableSet<String> {
        return XYApplication.mService?.scope?.filterNotNull()?.toMutableSet() ?: mutableSetOf()
    }

    private fun predictedScopeWithout(packageName: String): Set<String> {
        return currentScopeSet().apply { remove(packageName) }
    }

    private fun approvedScopeSet(approved: List<String?>): Set<String> {
        val scoped = currentScopeSet()
        scoped.addAll(approved.filterNotNull())
        return scoped
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding = convertView?.let {
            AppRuleBinding.bind(it)
        } ?: run {
            val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.app_rule, parent, false)
            AppRuleBinding.bind(view)
        }

        val rule = getItem(position)
        binding.apply {
            appIcon.setImageDrawable(rule.appIcon)
            appName.text = rule.appName
            appPackage.text = rule.packageName
            action.text = rule.action
            XYApplication.mService?.apply {
                val packageName = rule.packageName
                switchButton.visibility = View.VISIBLE
                switchButton.setOnClickListener(null)
                switchButton.isChecked = (rule.packageName in scope)
                switchButton.setOnClickListener {
                    if (switchButton.isChecked) {
                        switchButton.isChecked = false
                        requestScope(listOf(packageName), object : XposedService.OnScopeEventListener {
                            override fun onScopeRequestApproved(approved: List<String?>) {
                                refreshSorted(approvedScopeSet(approved))
                            }

                            override fun onScopeRequestFailed(message: String) {
                                refreshSorted()
                                Toast.makeText(parent?.context, message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        removeScope(listOf(packageName))
                        refreshSorted(predictedScopeWithout(packageName))
                    }
                }
            } ?: run {
                switchButton.setOnClickListener(null)
                switchButton.visibility = View.GONE
            }
            root.setOnClickListener {
                try {
                    val intent = Intent(parent?.context, AppInfoActivity::class.java)
                    intent.putExtra("packageName", rule.packageName)
                    intent.putExtra("appName", rule.appName)
                    parent?.context?.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return binding.root
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val query = constraint?.toString() ?: ""
                val base = displayList
                val filterPattern = query.lowercase(Locale.getDefault()).trim()
                val filtered = if (filterPattern.isEmpty()) {
                    base
                } else {
                    base.filter { item ->
                        item.appName.lowercase(Locale.getDefault()).contains(filterPattern) ||
                                item.packageName.lowercase(Locale.getDefault()).contains(filterPattern)
                    }
                }
                results.values = filtered
                results.count = filtered.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<Item2> ?: displayList
                currentFilterQuery = constraint?.toString() ?: ""
                notifyDataSetChanged()
            }
        }
    }
}
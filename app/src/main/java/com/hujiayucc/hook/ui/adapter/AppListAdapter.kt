package com.hujiayucc.hook.ui.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.hujiayucc.hook.R
import com.hujiayucc.hook.application.XYApplication
import com.hujiayucc.hook.data.Item
import com.hujiayucc.hook.databinding.AppRuleBinding
import io.github.libxposed.service.XposedService
import java.text.Collator
import java.util.Locale

class AppListAdapter(private val appList: List<Item>) : BaseAdapter() {
    private class ViewHolder(val binding: AppRuleBinding)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val appNameCollator = Collator.getInstance(Locale.CHINA)
    private var displayList: List<Item> = appList

    init {
        sortByScope()
    }

    override fun getCount(): Int = displayList.size
    override fun getItem(position: Int): Item = displayList[position]
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

    private fun refreshSorted(scopedPackages: Set<String>? = null) {
        val refreshAction = Runnable {
            sortByScope(scopedPackages)
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
        val holder = if (convertView != null) {
            convertView.tag as ViewHolder
        } else {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_rule, parent, false)
            val binding = AppRuleBinding.bind(view)
            ViewHolder(binding).also { binding.root.tag = it }
        }
        val binding = holder.binding
        val context = parent?.context ?: binding.root.context

        val rule = getItem(position)
        val version = if (rule.versions.isNotEmpty()) rule.versions.contentToString() else "通用"
        binding.apply {
            appIcon.setImageDrawable(rule.appIcon)
            appName.text = rule.appName
            appPackage.text = rule.packageName
            action.text = "${rule.action} $version"
            XYApplication.mService?.apply {
                val packageName = rule.packageName
                switchButton.setOnClickListener(null)
                switchButton.isChecked = packageName in scope
                switchButton.setOnClickListener {
                    if (switchButton.isChecked) {
                        switchButton.isChecked = false
                        requestScope(listOf(packageName), object : XposedService.OnScopeEventListener {
                            override fun onScopeRequestApproved(approved: List<String?>) {
                                refreshSorted(approvedScopeSet(approved))
                            }

                            override fun onScopeRequestFailed(message: String) {
                                refreshSorted()
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        removeScope(listOf(packageName))
                        refreshSorted(predictedScopeWithout(packageName))
                    }
                }
            } ?: run { switchButton.visibility = View.GONE }
            root.setOnClickListener {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(rule.packageName)
                if (launchIntent == null) {
                    Toast.makeText(parent?.context, "Open ${rule.appName} failed.", Toast.LENGTH_SHORT).show()
                } else {
                    context.startActivity(launchIntent)
                }
            }
        }

        return binding.root
    }
}
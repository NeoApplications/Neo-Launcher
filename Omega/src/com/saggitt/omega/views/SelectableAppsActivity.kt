/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.views

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.AppFilter
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.OmegaAppFilter
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.preferences.SelectableAppsAdapter
import com.saggitt.omega.settings.SettingsActivity

class SelectableAppsActivity : SettingsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun createLaunchFragment(intent: Intent): Fragment {
        return Fragment.instantiate(this, SelectionFragment::class.java.name, intent.extras)
    }

    override fun shouldShowSearch(): Boolean {
        return false
    }

    class SelectionFragment : RecyclerViewFragment(), SelectableAppsAdapter.Callback {

        private var selection: Set<String> = emptySet()
        private var changed = false

        override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
            val arguments = arguments!!
            val isWork = if (arguments.containsKey(KEY_FILTER_IS_WORK))
                arguments.getBoolean(KEY_FILTER_IS_WORK) else null
            selection = HashSet(arguments.getStringArrayList(KEY_SELECTION))

            val context = recyclerView.context
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = SelectableAppsAdapter.ofProperty(activity!!,
                    ::selection, this, createAppFilter(context, DrawerTabs.getWorkFilter(isWork)))
        }

        override fun onDestroy() {
            super.onDestroy()

            val receiver = arguments?.getParcelable<Parcelable>(KEY_CALLBACK) as ResultReceiver
            if (changed) {
                receiver.send(Activity.RESULT_OK, Bundle(1).apply {
                    putStringArrayList(KEY_SELECTION, ArrayList(selection))
                })
            } else {
                receiver.send(Activity.RESULT_CANCELED, null)
            }
        }

        override fun onResume() {
            super.onResume()

            updateTitle(selection.size)
        }

        override fun onSelectionsChanged(newSize: Int) {
            changed = true
            updateTitle(newSize)
        }

        private fun updateTitle(size: Int) {
            activity?.title = getString(R.string.selected_count, size)
        }
    }

    companion object {

        private const val KEY_SELECTION = "selection"
        private const val KEY_CALLBACK = "callback"
        private const val KEY_FILTER_IS_WORK = "filterIsWork"

        fun start(context: Context, selection: Collection<ComponentKey>,
                  callback: (Collection<ComponentKey>?) -> Unit, filterIsWork: Boolean? = null) {
            val intent = Intent(context, SelectableAppsActivity::class.java).apply {
                putStringArrayListExtra(KEY_SELECTION, ArrayList(selection.map { it.toString() }))
                putExtra(KEY_CALLBACK, object : ResultReceiver(Handler()) {

                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == Activity.RESULT_OK) {
                            callback(resultData!!.getStringArrayList(KEY_SELECTION)!!.map {
                                ComponentKey(ComponentName(context, it), Process.myUserHandle())
                            })
                        } else {
                            callback(null)
                        }
                    }
                })
                filterIsWork?.let { putExtra(KEY_FILTER_IS_WORK, it) }
            }
            context.startActivity(intent)
        }

        private fun createAppFilter(context: Context, predicate: (ComponentKey) -> Boolean): AppFilter {
            return object : AppFilter() {

                val base = OmegaAppFilter(context)

                override fun shouldShowApp(app: ComponentName, user: UserHandle?): Boolean {
                    if (!base.shouldShowApp(app, user)) {
                        return false
                    }
                    return predicate(ComponentKey(app, user ?: Process.myUserHandle()))
                }
            }
        }
    }
}

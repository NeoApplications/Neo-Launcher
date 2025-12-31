/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023 Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.allapps.search

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.launcher3.ExtendedEditText
import com.android.launcher3.Insettable
import com.android.launcher3.R
import com.android.launcher3.allapps.ActivityAllAppsContainerView
import com.android.launcher3.allapps.BaseAllAppsAdapter
import com.android.launcher3.allapps.SearchUiManager
import com.android.launcher3.allapps.search.AllAppsSearchBarController
import com.android.launcher3.search.SearchCallback
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.Nut
import com.neoapps.neolauncher.compose.icons.phosphor.X
import com.neoapps.neolauncher.compose.navigation.Routes
import com.neoapps.neolauncher.launcher
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.PreferenceActivity
import com.neoapps.neolauncher.search.SearchProviderController
import com.neoapps.neolauncher.theme.OmegaAppTheme
import com.neoapps.neolauncher.util.openURLInBrowser
import com.neoapps.neolauncher.util.prefs

open class ComposeSearchLayout(context: Context, attrs: AttributeSet? = null) :
    AbstractComposeView(context, attrs), SearchUiManager, Insettable,
    SearchCallback<BaseAllAppsAdapter.AdapterItem> {

    val mContext = context
    protected var prefs: NeoPrefs = mContext.prefs

    private val verticalOffset =
        resources.getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset)
    private var spController = SearchProviderController.getInstance(mContext)
    private val searchAlgorithm = NeoAppSearchAlgorithm(mContext, true)
    private val mSearchBarController: AllAppsSearchBarController = AllAppsSearchBarController()
    private var mAppsView: ActivityAllAppsContainerView<*>? = null
    private lateinit var focusManager: FocusManager
    private lateinit var textFieldFocusRequester: FocusRequester
    private var keyboardController: SoftwareKeyboardController? = null
    val query = mutableStateOf("")

    init {
        // Si la búsqueda en drawer está habilitada mostramos la vista, si no la ocultamos con GONE
        // para que no deje espacio en el layout.
        visibility = if (prefs.searchDrawerEnabled.getValue()) {
            VISIBLE
        } else {
            GONE
        }
    }

    @Composable
    override fun Content() {
        OmegaAppTheme {
            focusManager = LocalFocusManager.current
            keyboardController = LocalSoftwareKeyboardController.current
            textFieldFocusRequester = remember { FocusRequester() }
            val searchProviderSelector by spController.searchProviderSelector.collectAsState(0)
            val searchProviders by spController.searchProvidersState.collectAsState()
            val searchProvider =
                remember { derivedStateOf { searchProviders[searchProviderSelector] } }
            val searchIcon = rememberAsyncImagePainter(searchProvider.value.iconId)
            /*val micIcon = rememberDrawablePainter(
                drawable = if (searchProvider.supportsAssistant) searchProvider.assistantIcon
                else searchProvider.voiceIcon
            )*/

            var radius by remember { mutableFloatStateOf(0f) }
            val radiusPrefs = prefs.searchBarRadius.get().collectAsState(initial = 0f)
            radius = radiusPrefs.value.coerceAtLeast(8f)

            var textFieldValue by query

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        onQueryChanged(it)
                    },
                    modifier = Modifier
                        .weight(1f, true)
                        .focusRequester(textFieldFocusRequester)
                        .onFocusChanged {
                            when {
                                !it.isFocused && query.value.isEmpty() -> {
                                    mAppsView?.animateToSearchState(false, 0)
                                    keyboardController?.hide()
                                }

                                !it.isFocused -> {
                                    keyboardController?.hide()
                                }

                                it.isFocused && query.value.isNotEmpty() -> {
                                    mAppsView?.animateToSearchState(true, 0)
                                }
                            }
                        },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(radius.dp),
                    leadingIcon = {
                        FilledIconButton(
                            modifier = Modifier.height(IntrinsicSize.Max),
                            shape = RoundedCornerShape(radius.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.Transparent
                            ),
                            onClick = { spController.changeSearchProvider() }
                        ) {
                            Image(
                                modifier = Modifier.size(24.dp),
                                painter = searchIcon,
                                contentDescription = stringResource(id = R.string.label_search),
                            )
                        }
                    },
                    trailingIcon = {
                        Row {
                            AnimatedVisibility(visible = textFieldValue.isNotEmpty()) {
                                IconButton(onClick = {
                                    query.value = ""
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    resetSearch()
                                }) {
                                    Icon(
                                        imageVector = Phosphor.X,
                                        contentDescription = stringResource(id = R.string.widgets_full_sheet_cancel_button_description),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            /*AnimatedVisibility(visible = searchProvider.supportsVoiceSearch) {
                                IconButton(onClick = {
                                    if (searchProvider.supportsAssistant) {
                                        searchProvider.startAssistant { intent ->
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        searchProvider.startVoiceSearch { intent ->
                                            context.startActivity(intent)
                                        }
                                    }
                                }) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = micIcon,
                                        contentDescription = stringResource(id = R.string.label_voice_search),
                                    )
                                }
                            }*/
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.widgets_full_sheet_search_bar_hint),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSubmitSearch(query.value) },
                    ),
                )
                IconButton(onClick = {
                    mContext.startActivity(
                        PreferenceActivity.navigateIntent(mContext, Routes.PREFS_SEARCH)
                    )
                }) {
                    Icon(
                        imageVector = Phosphor.Nut,
                        contentDescription = stringResource(id = R.string.widgets_full_sheet_cancel_button_description),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    private fun onQueryChanged(query: String) {
        if (query.isEmpty()) {
            searchAlgorithm.cancel(true)
            clearSearchResult()
        } else {
            searchAlgorithm.cancel(false)
            searchAlgorithm.doSearch(query, this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        keyboardController?.hide()
    }

    override fun initializeSearch(containerView: ActivityAllAppsContainerView<*>?) {
        mAppsView = containerView
        mSearchBarController.initialize(
            NeoAppSearchAlgorithm(mContext, true),
            null,
            mContext.launcher,
            this
        )
    }

    override fun resetSearch() {
        clearSearchResult()
    }

    override fun onSearchResult(
        query: String?,
        items: ArrayList<BaseAllAppsAdapter.AdapterItem>?
    ) {
        if (items != null) {
            mAppsView?.setSearchResults(items)
        }
    }

    override fun onSearchResult(
        query: String?,
        items: ArrayList<BaseAllAppsAdapter.AdapterItem>?,
        suggestion: ArrayList<String>?
    ) {
        if (items != null) {
            mAppsView?.setSearchResults(items)
        }
    }

    override fun clearSearchResult() {
        mAppsView?.setSearchResults(null)
        query.value = ""
        mAppsView?.onClearSearchResult()
    }

    fun startSearch() {
        startSearch("")
    }

    fun startSearch(searchQuery: String) {
        query.value = searchQuery.trim()
        textFieldFocusRequester.requestFocus()
    }

    override fun getEditText(): ExtendedEditText? = null

    fun onSubmitSearch(query: String?): Boolean =
        if (spController.activeSearchProvider.searchUrl.isNotEmpty()) {
            openURLInBrowser(context, spController.activeSearchProvider.searchUrl.format(query))
            true
        } else {
            context.launcher.appsView.mainAdapterProvider.launchHighlightedItem()
            false
        }

    override fun setInsets(insets: Rect) {
        val mlp = layoutParams as MarginLayoutParams
        val isEnabled = prefs.searchDrawerEnabled.getValue()
        if (isEnabled) {
            mlp.topMargin = insets.top
        } else {
            mlp.topMargin = verticalOffset
        }

        requestLayout()
    }
}
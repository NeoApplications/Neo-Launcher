package com.neoapps.neolauncher.compose.pages.preferences

import android.webkit.URLUtil
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.DialogNegativeButton
import com.neoapps.neolauncher.compose.components.DialogPositiveButton
import com.neoapps.neolauncher.data.SearchProviderRepository
import com.neoapps.neolauncher.data.models.SearchProvider
import com.neoapps.neolauncher.preferences.NeoPrefs
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun SearchProviderDialogUI(
    repositoryId: Long,
    openDialogCustom: MutableState<Boolean>,
    onDelete: (Long?) -> Unit,
    onSave: (SearchProvider?) -> Unit,
) {
    val prefs = NeoPrefs.getInstance()
    val providerState = getKoin().get<SearchProviderRepository>().getFlow(repositoryId)
        .collectAsState(initial = null)
    val provider by remember {
        derivedStateOf {
            providerState.value
        }
    }

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    var nameValue by remember(provider) {
        mutableStateOf(provider?.name.orEmpty())
    }
    var searchUrlValue by remember(provider) {
        mutableStateOf(provider?.searchUrl.orEmpty())
    }
    var suggestionUrlValue by remember(provider) {
        mutableStateOf(provider?.suggestionUrl.orEmpty())
    }

    val searchUrlValidity = remember { mutableStateOf(false) }
    val suggestionUrlValidity = remember { mutableStateOf(false) }

    LaunchedEffect(searchUrlValue, suggestionUrlValue) {
        invalidateAddress(searchUrlValidity, searchUrlValue)
        if (suggestionUrlValue.isNotEmpty()) {
            invalidateAddress(suggestionUrlValidity, suggestionUrlValue)
        } else suggestionUrlValidity.value = true
    }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.edit_search_provider),
                style = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                value = nameValue,
                onValueChange = { nameValue = it },
                label = { Text(text = stringResource(id = R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = searchUrlValue,
                onValueChange = { searchUrlValue = it },
                label = { Text(text = stringResource(id = R.string.search_provider_search_url)) },
                supportingText = { Text(text = stringResource(id = R.string.search_provider_url_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    errorBorderColor = MaterialTheme.colorScheme.errorContainer,
                    errorSupportingTextColor = MaterialTheme.colorScheme.errorContainer,
                ),
                isError = !searchUrlValidity.value,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = suggestionUrlValue,
                onValueChange = { suggestionUrlValue = it },
                label = { Text(text = stringResource(id = R.string.search_provider_suggestion_url)) },
                supportingText = { Text(text = stringResource(id = R.string.search_provider_url_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    errorBorderColor = MaterialTheme.colorScheme.errorContainer,
                    errorSupportingTextColor = MaterialTheme.colorScheme.errorContainer,
                ),
                isError = !suggestionUrlValidity.value,
                modifier = Modifier.fillMaxWidth()
            )
            // TODO add icon selector
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                DialogNegativeButton(
                    textId = R.string.delete,
                    cornerRadius = cornerRadius,
                    onClick = {
                        onDelete(provider?.id)
                        openDialogCustom.value = false
                    },
                )
                Spacer(Modifier.weight(1f))
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
                DialogPositiveButton(
                    cornerRadius = cornerRadius,
                    onClick = {
                        if (searchUrlValidity.value && suggestionUrlValidity.value) {
                            onSave(
                                provider?.copy(
                                    name = nameValue,
                                    searchUrl = searchUrlValue,
                                    suggestionUrl = suggestionUrlValue
                                )
                            )
                            openDialogCustom.value = false
                        }
                    }
                )
            }
        }
    }
}

private fun invalidateAddress(
    validity: MutableState<Boolean>,
    address: String,
) {
    validity.value = URLUtil.isNetworkUrl(address) && address.contains("%s")
}

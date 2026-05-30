package com.docesforg.bura.place.picker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.place.Place
import com.docesforg.bura.place.saved.SavedPlaceItem
import com.docesforg.bura.place.search.SearchedPlaceItem

private val horizontalPadding = 16.dp

@Composable
fun PlacePickerResults(
    state: PlacePickerState,
    onPlaceClick: (Place) -> Unit,
    onPlaceDeleteClick: (Place) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        ContentLoadingIndicator(state.loading) { isVisible ->
            if (isVisible) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        when (val results = state.results) {
            is PlacePickerResults.SavedPlaces -> {
                SavedPlaces(
                    state = results,
                    onPlaceClick = onPlaceClick,
                    onPlaceDeleteClick = onPlaceDeleteClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PlacePickerResults.SearchedPlaces -> {
                SearchedPlaces(
                    state = results,
                    onPlaceClick = onPlaceClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> Unit
        }
    }
}

@Composable
private fun SavedPlaces(
    state: PlacePickerResults.SavedPlaces,
    onPlaceClick: (Place) -> Unit,
    onPlaceDeleteClick: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    var deleteCandidate: Place? by remember { mutableStateOf(null) }
    deleteCandidate?.let {
        DeletePlaceDialog(
            name = it.name,
            onDismiss = {
                deleteCandidate = null
            },
            onConfirm = {
                onPlaceDeleteClick(it)
                deleteCandidate = null
            }
        )
    }

    Column(modifier) {
        Text(
            text = stringResource(id = R.string.place_picker_title_saved_places),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = horizontalPadding, top = 24.dp)
        )
        val places = state.places
        when {
            places.isEmpty() -> Text(
                text = stringResource(R.string.place_picker_error_no_saved_places),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = horizontalPadding, top = 8.dp)
            )

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(places) { idx, item ->
                    SavedPlaceItem(
                        state = item,
                        onClick = { onPlaceClick(item.place) },
                        onLongClick = { deleteCandidate = item.place },
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding, vertical = 16.dp)
                            .fillMaxWidth()
                    )
                    if (idx != places.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DeletePlaceDialog(name: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.delete_place_value, name),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(text = stringResource(id = R.string.delete_place_description))
        },
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.general_btn_dialog_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.general_btn_dialog_confirm))
            }
        }
    )
}

@Composable
private fun SearchedPlaces(
    state: PlacePickerResults.SearchedPlaces,
    onPlaceClick: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(id = R.string.place_picker_title_search_results),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = horizontalPadding, top = 24.dp)
        )
        val places = state.places
        when {
            places == null -> {
                Text(
                    text = stringResource(id = R.string.place_picker_error_no_internet),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = horizontalPadding, top = 8.dp)
                )
            }

            places.isEmpty() -> {
                Text(
                    text = stringResource(
                        R.string.place_picker_error_value_no_results_for,
                        state.query
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = horizontalPadding, top = 8.dp)
                )
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(places) { idx, item ->
                        SearchedPlaceItem(
                            state = item,
                            onClick = { onPlaceClick(item) },
                            modifier = Modifier
                                .padding(horizontal = horizontalPadding, vertical = 16.dp)
                                .fillMaxWidth()
                        )
                        HorizontalDivider()
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.credit_geocoding),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = horizontalPadding,
                                vertical = 16.dp
                            )
                        )
                    }
                }
            }
        }
    }
}
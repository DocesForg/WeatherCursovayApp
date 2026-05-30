package com.docesforg.bura.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.docesforg.bura.R


private val DialogPadding = 24.dp
private val TitlePadding = PaddingValues(bottom = 16.dp)
private val ContentPadding = PaddingValues(bottom = 24.dp)
private val ButtonSpacing = 8.dp

@Composable
fun MultipleChoiceDialog(
    title: String,
    choices: List<String>,
    selectedIdx: Int,
    onSelect: (idx: Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newlySelectedIdx by remember(selectedIdx) { mutableIntStateOf(selectedIdx) }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
            modifier = modifier
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        start = DialogPadding,
                        end = DialogPadding,
                        top = DialogPadding,
                        bottom = TitlePadding.calculateBottomPadding()
                    )
                )
                LazyColumn(modifier = Modifier.padding(ContentPadding)) {
                    itemsIndexed(choices) { idx, item ->
                        Choice(
                            label = item,
                            selected = idx == newlySelectedIdx,
                            onClick = { newlySelectedIdx = idx },
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = DialogPadding)
                                .fillMaxWidth()
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = DialogPadding, end = DialogPadding, bottom = DialogPadding)
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.general_btn_dialog_cancel))
                    }
                    Spacer(modifier = Modifier.width(ButtonSpacing))
                    TextButton(onClick = { onSelect(newlySelectedIdx) }) {
                        Text(stringResource(R.string.general_btn_dialog_confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun Choice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
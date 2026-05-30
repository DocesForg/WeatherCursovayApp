package com.docesforg.bura.place.search

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.docesforg.bura.place.Place

@Composable
fun SearchedPlaceItem(state: Place, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .then(modifier)
    ) {
        Text(text = listOf(state.name, state.countryName ?: state.countryCode).joinToString(", "))
        val adminList = remember(state.admin1, state.admin2, state.admin3, state.admin4) {
            listOfNotNull(state.admin1, state.admin2, state.admin3, state.admin4)
        }
        if (adminList.isNotEmpty()) {
            Text(
                text = adminList.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
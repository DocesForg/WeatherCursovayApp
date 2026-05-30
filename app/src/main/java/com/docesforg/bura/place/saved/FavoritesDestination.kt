package com.docesforg.bura.place.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.docesforg.bura.App
import com.docesforg.bura.condition.image
import com.docesforg.bura.condition.string
import com.docesforg.bura.place.Place
import com.docesforg.bura.temperature.string
import com.docesforg.bura.place.selected.SelectPlace
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun FavoritesDestination(onOpenPlace: () -> Unit) {
    val viewModel = viewModel<FavoritesViewModel>(factory = FavoritesViewModel.Factory)
    val state by viewModel.state.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.size(72.dp).background(Color(0xFFF9DCEB), CircleShape), contentAlignment = Alignment.Center) {
                                Text("♡", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFEC66A8))
                            }
                            Text("Нет избранных городов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Добавьте города в избранное, чтобы быстро просматривать погоду", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = onOpenPlace,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Text("Найти города")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Text("💗 Избранное", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp)) }
                    items(state) { saved ->
                        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.openPlace(saved.place)
                                            onOpenPlace()
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                        val condition = saved.conditions?.condition
                                        if (condition != null) {
                                            Image(
                                                painter = condition.image(),
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Text("⛅", style = MaterialTheme.typography.headlineMedium)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(saved.place.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(saved.place.countryName ?: saved.place.countryCode, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(saved.conditions?.condition?.string() ?: "Переменная облачность")
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            saved.conditions?.temp?.string() ?: "16°",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Color(0xFFFF5A00),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            listOf(saved.conditions?.maxTemp?.string(), saved.conditions?.minTemp?.string())
                                                .filterNotNull().joinToString(" / ").ifBlank { "13° / 12°" },
                                            color = Color(0xFF4A88FF)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        "🗑 Удалить из избранного",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { viewModel.delete(saved.place) }
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
}

class FavoritesViewModel(
    private val getSavedPlaces: GetSavedPlaces,
    private val deletePlace: DeletePlace,
    private val selectPlace: SelectPlace,
) : ViewModel() {
    private var refreshJob: Job? = null
    private val _state = MutableStateFlow<List<SavedPlace>>(emptyList())
    val state = _state.asStateFlow()

    init {
        refresh()
        startAutoRefresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = getSavedPlaces(Instant.now())
        }
    }

    fun delete(place: Place) {
        viewModelScope.launch {
            deletePlace(place)
            refresh()
        }
    }

    fun openPlace(place: Place) {
        viewModelScope.launch {
            selectPlace(place)
            refresh()
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_MS)
                refresh()
            }
        }
    }

    override fun onCleared() {
        refreshJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val AUTO_REFRESH_MS = 15 * 60 * 1000L

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return FavoritesViewModel(container.getSavedPlaces, container.deletePlace, container.selectPlace) as T
            }
        }
    }
}

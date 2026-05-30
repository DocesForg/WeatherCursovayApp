package com.docesforg.bura.radio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.docesforg.bura.App
import com.docesforg.bura.place.Place
import com.docesforg.bura.place.search.SearchPlaces
import com.docesforg.bura.platform.local.RadioSignalTestEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioSignalDestination() {
    val viewModel = viewModel<RadioSignalViewModel>(factory = RadioSignalViewModel.Factory)
    val history by viewModel.history.collectAsState()
    val result by viewModel.latestResult.collectAsState()
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF347CF3), Color(0xFF6D2DFF)))

    Scaffold(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📡 Тест радиосигнала", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Проверьте качество радиосвязи между двумя городами.")
                }
            }
            item {
                Card(modifier = Modifier.padding(horizontal = 14.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CityAutocompleteField("Первый город", viewModel.cityA, viewModel.cityASuggestions, viewModel::updateCityA, viewModel::selectCityA)
                        CityAutocompleteField("Второй город", viewModel.cityB, viewModel.cityBSuggestions, viewModel::updateCityB, viewModel::selectCityB)
                        Button(
                            onClick = { viewModel.run() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White)
                        ) {
                            Box(modifier = Modifier.background(gradient, RoundedCornerShape(12.dp)).padding(horizontal = 18.dp, vertical = 10.dp)) {
                                Text("Рассчитать качество связи", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            result?.let { test ->
                item {
                    Card(modifier = Modifier.padding(horizontal = 14.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("✨ Результат тестирования", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.fillMaxWidth().background(gradient, RoundedCornerShape(12.dp)).padding(14.dp)) {
                                Text("${test.cityA} ↔ ${test.cityB}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricTile("📏 Расстояние", "${"%.0f".format(test.distanceKm)} км", Modifier.weight(1f), Color(0xFF4993FF))
                                MetricTile("📶 Качество", "${qualityPercent(test)}%", Modifier.weight(1f), Color(0xFF343434))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricTile("🕒 Задержка", "${"%.3f".format(test.latencyMs)} мс", Modifier.weight(1f), Color(0xFFA05BFF))
                                MetricTile("⚡ Скорость", "${"%.0f".format(test.speedMbps)} Мбит/с", Modifier.weight(1f), Color(0xFFFF7A2F))
                            }
                        }
                    }
                }
            }

            item { Text("📜 История тестов", modifier = Modifier.padding(horizontal = 14.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(history) { test ->
                Card(modifier = Modifier.padding(horizontal = 14.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${test.cityA} → ${test.cityB}", fontWeight = FontWeight.SemiBold)
                        Text("${"%.0f".format(test.distanceKm)} км • ${"%.3f".format(test.latencyMs)} мс • ${"%.0f".format(test.speedMbps)} Мбит/с")
                        Text(test.createdAt, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(title: String, value: String, modifier: Modifier = Modifier, valueColor: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityAutocompleteField(
    label: String,
    query: String,
    suggestions: List<Place>,
    onQueryChange: (String) -> Unit,
    onSelectPlace: (Place) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded && suggestions.isNotEmpty(), onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            value = query,
            onValueChange = { onQueryChange(it); expanded = true },
            label = { Text(label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded && suggestions.isNotEmpty(), onDismissRequest = { expanded = false }) {
            suggestions.take(7).forEach { place ->
                DropdownMenuItem(
                    text = { Text(listOf(place.name, place.countryName ?: place.countryCode).joinToString(", ")) },
                    onClick = { onSelectPlace(place); expanded = false }
                )
            }
        }
    }
}

private fun qualityPercent(test: RadioSignalTestEntity): Int =
    calculateSignalQualityPercent(test.pathLossDb)

internal fun calculateSignalQualityPercent(pathLossDb: Double): Int {
    val normalizedLoss = (pathLossDb - excellentSignalLossDb) / (unusableSignalLossDb - excellentSignalLossDb)
    return (maxSignalQualityPercent - normalizedLoss * signalQualityRangePercent)
        .roundToInt()
        .coerceIn(minSignalQualityPercent, maxSignalQualityPercent)
}

private const val excellentSignalLossDb = 90.0
private const val unusableSignalLossDb = 170.0
private const val minSignalQualityPercent = 1
private const val maxSignalQualityPercent = 99
private const val signalQualityRangePercent = maxSignalQualityPercent - minSignalQualityPercent

class RadioSignalViewModel(
    private val repository: RadioSignalRepository,
    private val searchPlaces: SearchPlaces,
) : ViewModel() {
    private val languageCode = Locale.getDefault().language

    var cityA by mutableStateOf("")
        private set
    var cityB by mutableStateOf("")
        private set
    var cityASuggestions by mutableStateOf<List<Place>>(emptyList())
        private set
    var cityBSuggestions by mutableStateOf<List<Place>>(emptyList())
        private set
    private var selectedCityA: Place? = null
    private var selectedCityB: Place? = null

    private val _history = MutableStateFlow<List<RadioSignalTestEntity>>(emptyList())
    val history = _history.asStateFlow()

    private val _latestResult = MutableStateFlow<RadioSignalTestEntity?>(null)
    val latestResult = _latestResult.asStateFlow()

    init {
        loadHistory()
    }

    fun updateCityA(query: String) {
        cityA = query
        selectedCityA = null
        searchA(query)
    }

    fun updateCityB(query: String) {
        cityB = query
        selectedCityB = null
        searchB(query)
    }

    fun selectCityA(place: Place) {
        selectedCityA = place
        cityA = place.name
        cityASuggestions = emptyList()
    }

    fun selectCityB(place: Place) {
        selectedCityB = place
        cityB = place.name
        cityBSuggestions = emptyList()
    }

    private fun searchA(query: String) {
        if (query.length < 2) {
            cityASuggestions = emptyList()
            return
        }
        viewModelScope.launch {
            cityASuggestions = searchPlaces(query.trim(), languageCode).orEmpty()
        }
    }

    private fun searchB(query: String) {
        if (query.length < 2) {
            cityBSuggestions = emptyList()
            return
        }
        viewModelScope.launch {
            cityBSuggestions = searchPlaces(query.trim(), languageCode).orEmpty()
        }
    }

    fun run() {
        val a = selectedCityA ?: return
        val b = selectedCityB ?: return
        viewModelScope.launch {
            _latestResult.value = repository.run(
                cityA = a.name,
                cityB = b.name,
                latitudeA = a.location.coordinates.latitude,
                longitudeA = a.location.coordinates.longitude,
                latitudeB = b.location.coordinates.latitude,
                longitudeB = b.location.coordinates.longitude,
            )
            loadHistory()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.history()
            _latestResult.value = _history.value.firstOrNull()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return RadioSignalViewModel(container.radioSignalRepository, container.searchPlaces) as T
            }
        }
    }
}

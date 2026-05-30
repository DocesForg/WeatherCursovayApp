package com.docesforg.bura.graphs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.docesforg.bura.R
import com.docesforg.bura.common.FailedToDownloadErrorScreen
import com.docesforg.bura.common.NoSelectedPlaceErrorScreen
import com.docesforg.bura.common.OutdatedErrorScreen
import com.docesforg.bura.common.animateShimmerColorAsState
import com.docesforg.bura.graphs.common.GraphArgs
import com.docesforg.bura.graphs.common.GraphsPagerIndicator
import com.docesforg.bura.graphs.common.GraphsPagerIndicatorSkeleton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssentialGraphsScreen(
    initialDay: LocalDate?,
    state: EssentialGraphsState,
    onTryAgainClick: () -> Unit,
    onSelectPlaceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cond_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Crossfade(
            targetState = state,
            modifier = Modifier.padding(contentPadding),
            label = "State crossfade"
        ) {
            when (it) {
                is EssentialGraphsState.Success -> Pager(
                    state = it,
                    initialDay = initialDay,
                    modifier = Modifier.fillMaxSize()
                )

                EssentialGraphsState.Loading -> EssentialGraphsLoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )

                EssentialGraphsState.FailedToDownload -> FailedToDownloadErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                EssentialGraphsState.Outdated -> OutdatedErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                EssentialGraphsState.NoSelectedPlace -> NoSelectedPlaceErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSelectPlaceClick = onSelectPlaceClick
                )
            }
        }
    }
}

@Composable
private fun Pager(
    state: EssentialGraphsState.Success,
    initialDay: LocalDate?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val summaries = state.tempGraphSummaries
        val tempGraphs = state.tempGraphs
        val dates = remember(summaries) { summaries.map { it.day } }
        val pagerState = rememberPagerState(initialPage = initialDay?.let { dates.indexOf(it) } ?: 0) { summaries.size }
        val pagerPage by remember { derivedStateOf { pagerState.currentPage } }
        val scope = rememberCoroutineScope()
        GraphsPagerIndicator(
            state = dates,
            selected = pagerPage,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(dates.indexOf(it))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        var itemIndex by remember { mutableIntStateOf(0) }
        var itemScrollOffset by remember { mutableIntStateOf(0) }
        HorizontalPager(state = pagerState) { page ->
            val listState = rememberLazyListState()
            LaunchedEffect(itemIndex, itemScrollOffset, page, pagerPage) {
                if (page != pagerPage) {
                    listState.scrollToItem(itemIndex, itemScrollOffset)
                }
            }
            LaunchedEffect(listState) {
                snapshotFlow { listState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect {
                        if (page == pagerPage) {
                            itemIndex = it
                        }
                    }
            }
            LaunchedEffect(listState) {
                snapshotFlow { listState.firstVisibleItemScrollOffset }
                    .distinctUntilChanged()
                    .collect {
                        if (page == pagerPage) {
                            itemScrollOffset = it
                        }
                    }
            }
            EssentialGraphPage(
                listState = listState,
                summary = summaries[page],
                temperatureGraph = tempGraphs.graphs[page],
                minTemp = tempGraphs.minTemp,
                maxTemp = tempGraphs.maxTemp,
                temperatureArgs = GraphArgs.rememberTemperatureArgs(),
                popGraph = state.popGraphs[page],
                popArgs = GraphArgs.rememberPopArgs(),
                precipGraph = state.precipGraphs.graphs[page],
                precipMax = state.precipGraphs.max,
                precipArgs = GraphArgs.rememberPrecipitationArgs(),
                precipitationTotal = state.precipTotals[page]
            )
        }
    }
}

@Composable
private fun EssentialGraphsLoadingIndicator(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val shimmerColor = animateShimmerColorAsState()
        GraphsPagerIndicatorSkeleton(
            color = shimmerColor,
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        EssentialGraphPageLoadingIndicator(
            shimmerColor = shimmerColor,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
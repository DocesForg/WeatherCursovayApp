package com.docesforg.bura.forecast

sealed interface ForecastResult<out T> {
    data class Success<T>(val data: T) : ForecastResult<T>
    data object FailedToDownload : ForecastResult<Nothing>
    data object Outdated : ForecastResult<Nothing>
}
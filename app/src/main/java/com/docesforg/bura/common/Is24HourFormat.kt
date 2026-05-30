package com.docesforg.bura.common

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

private fun is24HourFormatFlow(context: Context): Flow<Boolean> = callbackFlow {
    val callback = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun deliverSelfNotifications() = true
        override fun onChange(selfChange: Boolean) {
            trySendBlocking(DateFormat.is24HourFormat(context))
        }
    }
    val uri = android.provider.Settings.System.getUriFor(android.provider.Settings.System.TIME_12_24)
    context.contentResolver.registerContentObserver(uri, false, callback)
    awaitClose { context.contentResolver.unregisterContentObserver(callback) }
}.distinctUntilChanged()

@Composable
fun rememberIs24HourFormat(): Boolean {
    val context = LocalContext.current
    val flow = remember(context) { is24HourFormatFlow(context) }
    return flow.collectAsState(initial = DateFormat.is24HourFormat(context)).value
}
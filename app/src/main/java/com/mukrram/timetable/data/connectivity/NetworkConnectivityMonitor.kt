package com.mukrram.timetable.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

@Composable
fun rememberIsOnline(): Boolean {
    val context = LocalContext.current.applicationContext
    var online by remember { mutableStateOf(context.isCurrentlyOnline()) }

    DisposableEffect(context) {
        val cm = context.getSystemService<ConnectivityManager>() ?: return@DisposableEffect onDispose { }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                online = true
            }

            override fun onLost(network: Network) {
                online = context.isCurrentlyOnline()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                online = hasInternet
            }
        }
        cm.registerNetworkCallback(request, callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    return online
}

private fun Context.isCurrentlyOnline(): Boolean {
    val cm = getSystemService<ConnectivityManager>() ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

package com.example.asknshare.repo

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkMonitor(private val context: Context) {
    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unknown)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            checkConnectionQuality()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _networkStatus.value = NetworkStatus.Disconnected
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            checkConnectionQuality()
        }
    }

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        checkConnectionQuality()
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun checkConnectionQuality() {
        val network = connectivityManager.activeNetwork ?: run {
            _networkStatus.value = NetworkStatus.Disconnected
            return
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: run {
            _networkStatus.value = NetworkStatus.Disconnected
            return
        }

        _networkStatus.value = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                NetworkStatus.Connected(ConnectionQuality.Good)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                if (isFastConnection(capabilities)) {
                    NetworkStatus.Connected(ConnectionQuality.Moderate)
                } else {
                    NetworkStatus.Connected(ConnectionQuality.Slow)
                }
            else -> NetworkStatus.Connected(ConnectionQuality.Unknown)
        }
    }

    private fun isFastConnection(capabilities: NetworkCapabilities): Boolean {
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                capabilities.linkDownstreamBandwidthKbps > 500 // 500 Kbps threshold
    }

    sealed class NetworkStatus {
        object Unknown : NetworkStatus()
        object Disconnected : NetworkStatus()
        data class Connected(val quality: ConnectionQuality) : NetworkStatus()
    }

    enum class ConnectionQuality {
        Good, Moderate, Slow, Unknown
    }
}
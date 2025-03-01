package com.example.asknshare.repo

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi

class NetworkMonitor(private val context: Context) {

    private var isInternetSlow = false
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun startMonitoring(onSlowConnection: () -> Unit) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Check if the connection is fast enough
                if (!isFastConnection()) {
                    isInternetSlow = true
                    onSlowConnection()
                } else {
                    isInternetSlow = false
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Handle lost connection
                isInternetSlow = true
                onSlowConnection()
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun isFastConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
}
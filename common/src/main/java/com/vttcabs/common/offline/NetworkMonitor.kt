package com.vttcabs.common.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Network connectivity monitor
 * Detects when device goes online/offline and emits state changes
 */
class NetworkMonitor(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isOnline = MutableStateFlow(checkConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _connectionType = MutableStateFlow(getConnectionType())
    val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()
    
    private val _lastOnlineTime = MutableStateFlow(System.currentTimeMillis())
    val lastOnlineTime: StateFlow<Long> = _lastOnlineTime.asStateFlow()
    
    private val _lastOfflineTime = MutableStateFlow<Long?>(null)
    val lastOfflineTime: StateFlow<Long?> = _lastOfflineTime.asStateFlow()
    
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    enum class ConnectionType {
        WIFI,
        CELLULAR,
        ETHERNET,
        NONE
    }
    
    /**
     * Start monitoring network changes
     */
    fun startMonitoring() {
        if (networkCallback != null) return
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                _isOnline.value = true
                _connectionType.value = getConnectionType()
                _lastOnlineTime.value = System.currentTimeMillis()
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                _isOnline.value = false
                _lastOfflineTime.value = System.currentTimeMillis()
            }
            
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val type = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                    else -> ConnectionType.NONE
                }
                _connectionType.value = type
                Log.d(TAG, "Connection type changed: $type")
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, networkCallback!!)
        Log.d(TAG, "Network monitoring started")
    }
    
    /**
     * Stop monitoring network changes
     */
    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
        networkCallback = null
        Log.d(TAG, "Network monitoring stopped")
    }
    
    /**
     * Check current connectivity status
     */
    fun checkConnectivity(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Get current connection type
     */
    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.NONE
        }
    }
    
    /**
     * Get time since last online (in milliseconds)
     */
    fun getTimeSinceLastOnline(): Long {
        return if (_isOnline.value) {
            0
        } else {
            _lastOfflineTime.value?.let {
                System.currentTimeMillis() - it
            } ?: 0
        }
    }
    
    /**
     * Observe connectivity as Flow
     */
    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Emit current state
        trySend(checkConnectivity())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    
    companion object {
        private const val TAG = "NetworkMonitor"
        
        @Volatile
        private var instance: NetworkMonitor? = null
        
        fun getInstance(context: Context): NetworkMonitor {
            return instance ?: synchronized(this) {
                instance ?: NetworkMonitor(context.applicationContext).also { instance = it }
            }
        }
    }
}

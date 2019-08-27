package com.nesstech.loginauthmvvm.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.nesstech.loginauthmvvm.App

 class NetworkMonitor : BroadcastReceiver() {

    companion object {
        lateinit var connectivityReceiverListener: ConnectivityReceiverListener

        fun isConnected(): Boolean {
            val cm = App.getInstance().applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        connectivityReceiverListener.onNetworkConnectionChanged(isConnected())
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }
}
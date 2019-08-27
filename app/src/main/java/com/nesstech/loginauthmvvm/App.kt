package com.nesstech.loginauthmvvm

import android.app.Application
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.nesstech.loginauthmvvm.network.NetworkMonitor

class App: Application(), NetworkMonitor.ConnectivityReceiverListener {

    private var isConnected: Boolean = false

    init {
        mInstance = this
    }

    companion object{
        private lateinit var mInstance: App

        @Synchronized
        fun getInstance(): App {
            return mInstance as App
        }
    }

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(mInstance)
        AppEventsLogger.activateApp(mInstance)
        registerConnectionReceiver()//1
        setConnectivityListener(this)//2
    }

    private fun registerConnectionReceiver() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val receiver = NetworkMonitor()
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            registerReceiver(receiver,intentFilter)
        }
    }

    private fun setConnectivityListener(listener: NetworkMonitor.ConnectivityReceiverListener) {
        NetworkMonitor.connectivityReceiverListener = listener
    }

    fun isConnected():Boolean{
        return isConnected
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        this.isConnected = isConnected
        val networkStatus: String?
        networkStatus = if(isConnected){
            "Network Connected"
        }else{
            "Network disconnected"
        }
        Toast.makeText(mInstance,networkStatus,Toast.LENGTH_SHORT).show()
    }

}

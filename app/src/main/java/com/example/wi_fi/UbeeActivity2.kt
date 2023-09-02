package com.example.wi_fi

import WifiAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UbeeActivity2 : AppCompatActivity() {
    protected lateinit var wifiManager: WifiManager
    protected lateinit var wifiRecyclerView: RecyclerView

    protected val wifiScanResults = ArrayList<ScanResult>()
    protected val wifiScanReceiver = WifiScanReceiver()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubee)

        wifiRecyclerView = findViewById(R.id.wifiRecyclerView)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
        } else {
            startWifiScan()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected fun startWifiScan() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (wifiManager.isWifiEnabled) {
                wifiManager.startScan()
            } else {
                // El Wi-Fi está desactivado, manejar el caso según sea necesario
            }
        }
    }

    protected fun showWifiNetworks() {
        val wifiAdapter = WifiAdapter(this, wifiScanResults, object : WifiAdapter.OnItemClickListener {
            override fun onItemClick(scanResult: ScanResult) {
                val selectedSsid = scanResult.SSID
                Toast.makeText(this@UbeeActivity2, "Seleccionaste: $selectedSsid", Toast.LENGTH_SHORT).show()
                // Aquí puedes mostrar más detalles de la red si lo deseas
            }
        })
        wifiRecyclerView.layoutManager = LinearLayoutManager(this)
        wifiRecyclerView.adapter = wifiAdapter
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiScanReceiver)
    }

    inner class WifiScanReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            wifiScanResults.clear()
            wifiScanResults.addAll(wifiManager.scanResults)
            showWifiNetworks()
        }
    }
}

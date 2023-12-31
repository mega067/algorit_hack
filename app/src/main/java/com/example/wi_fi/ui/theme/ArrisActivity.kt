package com.example.wi_fi.ui.theme

import WifiAdapter
import com.example.wi_fi.R
import com.example.wi_fi.ui.theme.WifiDetailsActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.*
import android.view.View
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ArrisActivity : AppCompatActivity() {

    protected lateinit var wifiManager: WifiManager
    protected lateinit var wifiRecyclerView: RecyclerView
    protected val wifiScanResults = ArrayList<ScanResult>()
    private val wifiScanReceiver = WifiScanReceiver()
    private val REFRESH_INTERVAL = 30000L // 30 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arris)

        wifiRecyclerView = findViewById(R.id.arrisWifiRecyclerView)

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
            startWifiScanPeriodically()
        }

        val scanButton: ImageButton = findViewById(R.id.arrisScanButton)
        scanButton.setOnClickListener {
            startWifiScan()
        }

        val backButton: ImageButton = findViewById(R.id.arrisBackButton)
        backButton.setOnClickListener {
            onBackPressed()
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

    private fun showWifiNetworks() {
        val wifiAdapter = WifiAdapter(this, wifiScanResults, object : WifiAdapter.OnItemClickListener {
            override fun onItemClick(scanResult: ScanResult) {
                val selectedSsid = scanResult.SSID
                val selectedBssid = scanResult.BSSID
                val selectedCapabilities = scanResult.capabilities
                val selectedPassword = getPassword(selectedSsid, selectedBssid) // Obtener la contraseña según la lógica implementada

                // Mostrar los detalles de la red en la nueva pantalla
                val intent = Intent(this@ArrisActivity, arrisWifiDetailsActivity::class.java)
                intent.putExtra("SSID", selectedSsid)
                intent.putExtra("BSSID", selectedBssid)
                intent.putExtra("CAPABILITIES", selectedCapabilities)
                intent.putExtra("PASSWORD", selectedPassword)
                startActivity(intent)

                // Intentar conectarse a la red Wi-Fi
                connectToWifi(selectedSsid, selectedPassword)
            }
        })

        wifiRecyclerView.layoutManager = LinearLayoutManager(this)
        wifiRecyclerView.adapter = wifiAdapter
    }

    protected fun getPassword(ssid: String, bssid: String): String {
        // Obtener la primera parte de la contraseña del BSSID
        val bssidDigits = bssid.replace(":", "").substring(0, 6).toUpperCase()

        // Variable decimal que va de "99" a "00"

        // Obtener la segunda parte de la contraseña del SSID
        val ssidDigits = ssid.replace("-", "").replace(".", "").substring(5, 9).toUpperCase()

        // Combinar todas las partes para formar la contraseña
        return bssidDigits + "???" + ssidDigits
    }

    @SuppressLint("MissingPermission")
    protected fun connectToWifi(ssid: String, password: String) {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"$ssid\""
        wifiConfig.preSharedKey = "\"$password\""

        val networkId = wifiManager.addNetwork(wifiConfig)
        if (networkId != -1) {
            wifiManager.disconnect()
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()
        } else {
            // No se pudo agregar la red Wi-Fi, manejar el caso según sea necesario
        }
    }

    private fun startWifiScanPeriodically() {
        startWifiScan()

        val handler = Handler()
        handler.postDelayed({
            startWifiScan()
            handler.postDelayed({
                startWifiScanPeriodically()
            }, REFRESH_INTERVAL)
        }, REFRESH_INTERVAL)
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

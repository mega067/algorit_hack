package com.example.wi_fi.ui.theme

import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.wi_fi.R

class WifiDetailsActivity : AppCompatActivity() {
    protected lateinit var ssid: String
    protected lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_details)

        val ssidTextView: TextView = findViewById(R.id.ssidTextView)
        val passwordTextView: TextView = findViewById(R.id.passwordTextView)

        val intent = intent
        if (intent != null) {
            ssid = intent.getStringExtra("SSID") ?: ""
            password = intent.getStringExtra("PASSWORD") ?: ""

            ssidTextView.text = "NOMBRE (SSID): $ssid"
            passwordTextView.text = "CONTRASEÑA: $password "
        }

        val ssidLayout: LinearLayout = findViewById(R.id.ssidLayout)
        val passwordLayout: LinearLayout = findViewById(R.id.passwordLayout)
        val connectButton: ImageButton = findViewById(R.id.connectButton)

        ssidLayout.setOnClickListener {
            copyToClipboard(ssidTextView.text.toString(), "SSID")
        }

        passwordLayout.setOnClickListener {
            copyToClipboard(passwordTextView.text.toString(), "Contraseña")
        }

        connectButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToWifi(ssid, password)
            } else {
                connectToWifiLegacy(ssid, password)
            }
        }

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val cleanedText = text.substringAfter(": ").trim()
        val clip = android.content.ClipData.newPlainText("Copied Text", cleanedText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copiado", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun connectToWifi(ssid: String, password: String) {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // La red WiFi está disponible
                // Realiza las acciones necesarias aquí
                Toast.makeText(this@WifiDetailsActivity, "Conectando a $ssid", Toast.LENGTH_SHORT).show()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                // La red WiFi no está disponible
                // Realiza las acciones necesarias aquí
                Toast.makeText(this@WifiDetailsActivity, "No se puede conectar a $ssid", Toast.LENGTH_SHORT).show()
            }
        }

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(request, networkCallback)
    }

    protected fun connectToWifiLegacy(ssid: String, password: String) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager

        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"$ssid\""
        wifiConfig.preSharedKey = "\"$password\""

        val networkId = wifiManager.addNetwork(wifiConfig)
        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()

        Toast.makeText(this, "Conectándose a $ssid...", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

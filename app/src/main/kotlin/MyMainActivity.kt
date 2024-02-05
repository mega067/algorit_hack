package com.example.wi_fi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.wi_fi.ui.theme.ArrisActivity
import com.example.wi_fi.ui.theme.UbeeActivity

import java.io.BufferedReader
import java.io.InputStreamReader

class MyMainActivity : AppCompatActivity() {
    private lateinit var wifiButton: ImageButton
    private lateinit var ubeeButton: Button
    private lateinit var arrisButton: Button
    private lateinit var infoButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiButton = findViewById<ImageButton>(R.id.wifiButton)
        ubeeButton = findViewById<Button>(R.id.ubeeButton)
        arrisButton = findViewById<Button>(R.id.arrisButton)
        infoButton = findViewById<ImageButton>(R.id.infoButton)

        wifiButton.setOnClickListener {
            // No es necesario verificar permisos de root
            showAdditionalButtons()
        }

        ubeeButton.setOnClickListener {
            val intent = Intent(this, UbeeActivity::class.java)
            startActivity(intent)
        }

        arrisButton.setOnClickListener {
            val intent = Intent(this, ArrisActivity::class.java)
            startActivity(intent)
        }

        infoButton.setOnClickListener {
            showDialog()
        }
    }

    private fun showAdditionalButtons() {
        ubeeButton.visibility = Button.VISIBLE
        arrisButton.visibility = Button.VISIBLE
    }

    private fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Información importante")
            .setMessage(
                "Esta app es una herramienta para realizar pruebas de ciberseguridad en redes Wi-Fi mediante la generación de contraseñas de forma algorítmica para los módems o routers Ubee y Arris.\n" +
                        "\n" +
                        "No nos responsabilizamos por el uso inapropiado de esta herramienta y no tenemos ninguna conexión con las marcas mencionadas anteriormente.\n" +
                        "\n" +
                        "Esta app no requiere permisos de superusuario (ROOT).\n" +
                        "\n" +
                        "Hecho por: Ángel de Jesús Juárez Román\n"
            )
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }
            .setNegativeButton("Contactar") { _, _ ->
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto:megacorp067l@gmail.com")
                this@MyMainActivity.startActivity(emailIntent)
            }

        val dialog = dialogBuilder.create()
        val dialogView = dialog.layoutInflater.inflate(R.layout.dialog_layout, null)
        dialogView.findViewById<ImageView>(R.id.logo_corp)
            .setImageResource(R.drawable.corp_2)
        dialog.setView(dialogView)
        dialog.show()
    }
}
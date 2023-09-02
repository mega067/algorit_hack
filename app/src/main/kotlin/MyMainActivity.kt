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
            checkRootPermissions()
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

    private fun checkRootPermissions() {
        if (hasRootAccess()) {
            showAdditionalButtons()
        } else {
            showRootPermissionDialog()
        }
    }

    private fun hasRootAccess(): Boolean {
        val process = Runtime.getRuntime().exec("su")
        val outputStream = process.outputStream
        outputStream.close()

        val exitCode = process.waitFor()
        return exitCode == 0
    }

    private fun showRootPermissionDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Solicitud de permisos root")
            .setMessage("Esta aplicación requiere permisos root. ¿Desea otorgar los permisos?")
            .setPositiveButton("Sí") { dialog, which ->
                executeRootCommand()
            }
            .setNegativeButton("No") { dialog, which ->
                Toast.makeText(this, "Permisos root denegados.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAdditionalButtons() {
        ubeeButton.visibility = Button.VISIBLE
        arrisButton.visibility = Button.VISIBLE

    }

    private fun executeRootCommand() {
        val command = "ls /data" // comando root de prurba

        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            outputStream.write((command + "\n").toByteArray())
            outputStream.flush()
            outputStream.close()

            val inputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val output = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            process.waitFor()

            val result = output.toString()
            // Procesar el resultado del comando root aquí
            Toast.makeText(this, "¡Gracias por darnos permisos root!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al ejecutar el comando root.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Información importante")
            .setMessage(
                "Esta app es una herramienta para realizar pruebas de ciberseguridad en redes Wi-Fi mediante la generación de contraseñas de forma algorítmica para los módems o routers Ubee y Arris.\n" +
                        "\n" +
                        "No nos responsabilizamos por el uso inapropiado de esta herramienta y no tenemos ninguna conexión con las marcas mencionadas anteriormente.\n" +
                        "\n" +
                        "Esta app requiere que el dispositivo esté rooteado y que la aplicación tenga acceso de superusuario (ROOT).\n" +
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

package com.example.wi_fi

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wifiButton = findViewById<Button>(R.id.wifiButton)
        wifiButton.setOnClickListener {
            // Acción a realizar cuando se presione el botón WI-FI
            Toast.makeText(this, "Botón WI-FI presionado", Toast.LENGTH_SHORT).show()
        }
    }
}

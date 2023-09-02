import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wi_fi.R

class WifiAdapter(
    private val context: Context,
    private val wifiScanResults: ArrayList<ScanResult>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<WifiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_wifi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanResult = wifiScanResults[position]
        holder.bind(scanResult)
    }

    override fun getItemCount(): Int {
        return wifiScanResults.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ssidTextView: TextView = itemView.findViewById(R.id.ssidTextView)
        private val bssidTextView: TextView = itemView.findViewById(R.id.bssidTextView)
        private val signalImageView: ImageView = itemView.findViewById(R.id.signalImageView)
        private val icono: ImageView = itemView.findViewById(R.id.icono) // Agrega el ImageView correspondiente
        private val otroIcono: ImageView = itemView.findViewById(R.id.icono)

        fun bind(scanResult: ScanResult) {
            ssidTextView.text = scanResult.SSID
            bssidTextView.text = scanResult.BSSID

            val rssiLevel = WifiManager.calculateSignalLevel(scanResult.level, 5) // 5 niveles

            val signalImageResource = when (rssiLevel) {
                0 -> R.drawable.ic_wifi_signal_0
                1 -> R.drawable.ic_wifi_signal_1
                2 -> R.drawable.ic_wifi_signal_2
                3 -> R.drawable.ic_wifi_signal_3
                4 -> R.drawable.ic_wifi_signal_4
                else -> R.drawable.ic_wifi_signal_0
            }

            signalImageView.setImageResource(signalImageResource)

            // Verificar si la SSID contiene las palabras deseadas
            val undesiredWords = arrayOf("Ubee", "ubee", "UBEE", "Ube", "ube","Arris")
            val containsUndesiredWords = undesiredWords.any { scanResult.SSID.contains(it) }

            if (containsUndesiredWords) {
                icono.visibility = View.VISIBLE // Mostrar el ImageView "no"
                icono.setImageResource(R.drawable.si_red)

                itemView.setOnClickListener {
                     listener.onItemClick(scanResult)
                }
            } else {
                otroIcono.visibility = View.VISIBLE // Mostrar el segundo ImageView
                otroIcono.setImageResource(R.drawable.x) // Asignar la imagen deseada al segundo ImageView

                itemView.setOnClickListener {
                    showNetworkNotSupportedDialog()
                }
            }
        }

        private fun showNetworkNotSupportedDialog() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Error: 303")
            builder.setMessage("La red no es compatible.\n"+
                    "\n"+
                    "La red no cumple con los requisitos para generar la contraseña.\n"+
                    "\n"+
                    "Asegúrate de que la red sea Ubee o Arris. Si el error persiste, comunícate con nosotros:\n"+
                    "Gmail: megacorp067l@gmail.com"
            )
            builder.setPositiveButton("Contactar") { _, _ ->
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto:megacorp067l@gmail.com")
                context.startActivity(emailIntent)
            }
            builder.setNegativeButton("Aceptar", null)
            val dialog = builder.create()
            dialog.show()

            // Alinear los botones en el cuadro de diálogo
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            params.setMargins(0, 0, 100, 0) // Ajusta los márgenes según sea necesario
            positiveButton.layoutParams = params
            negativeButton.layoutParams = params
        }

    }

    interface OnItemClickListener {
        fun onItemClick(scanResult: ScanResult)
    }
}

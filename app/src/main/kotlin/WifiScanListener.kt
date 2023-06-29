import android.net.wifi.ScanResult
interface WifiScanListener {


    fun onWifiScanResultsAvailable(scanResults: List<ScanResult>)
}

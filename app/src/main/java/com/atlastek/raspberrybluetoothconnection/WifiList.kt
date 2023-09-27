package com.atlastek.raspberrybluetoothconnection

import android.Manifest
import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.atlastek.raspberrybluetoothconnection.databinding.ActivityPasswordSendBinding
import com.atlastek.raspberrybluetoothconnection.databinding.ActivityWifiListBinding

class WifiList : AppCompatActivity() {

    private lateinit var wifiListBinding: ActivityWifiListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiListBinding = ActivityWifiListBinding.inflate(layoutInflater)
        setContentView(wifiListBinding.root)

        val wifiArrayList = ArrayList<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE
            ))
        }


        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess(wifiManager,wifiArrayList)
                } else {
                    scanFailure(wifiManager)
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        this.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            scanFailure(wifiManager)
        }


    }

    private fun scanSuccess(wifiManager :WifiManager, wifiArrayList : ArrayList<String>) {
        val results = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("permission denied")
            return
        } else {
            println("")
        }

        var wifilist = wifiManager.scanResults
        var wifilistSize = wifilist.size
        for(wifi in wifilist){
            println(wifi)
            var contain = false
            var ssid = wifi.SSID
            for(wifiadded in wifiArrayList) {
                if(ssid == wifiadded)
                {
                    contain = true
                }
            }
            if(contain == false){
                wifiArrayList.add(ssid)
            }




        }
        println(wifiArrayList)
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, wifiArrayList)
        wifiListBinding?.wifiList?.adapter   = arrayAdapter
        wifiListBinding?.wifiList?.setOnItemClickListener { parent, _, position, _ ->


            val selectedItem = parent.getItemAtPosition(position) as String

            val intent = Intent(this, PasswordSend::class.java)
            intent.putExtra("selectedItem", selectedItem)
            this.startActivity(intent)
        }
    }

    private fun scanFailure(wifiManager :WifiManager) {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("permission denied")
            return
        } else {
            println("")
        }
        wifiManager.scanResults
        println()
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test", "${it.key} = ${it.value}")
            }
        }
}
package com.atlastek.raspberrybluetoothconnection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.atlastek.raspberrybluetoothconnection.databinding.ActivityMainBinding
import java.io.IOException
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var mainbinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainbinding.root)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        val mmSocket : BluetoothSocket? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE
            ))
        }

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            println("Device doesn't support Bluetooth")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        mainbinding.scanPairDevices.setOnClickListener{
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("Bluetooth izni yok!")
            }
            else{
                println("Bluetooth izni alındı...")
                ConnectThread(this@MainActivity,bluetoothAdapter, mainbinding, mmSocket).run()
            }
        }

    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
            println("Bluetooth OK")
        }else{
            //deny
            println("Bluetooth deny")
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test", "${it.key} = ${it.value}")
            }
        }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        println("requestCode : " + requestCode)
    }

    public class ConnectThread(context : Context, bluetoothAdapter: BluetoothAdapter?, mainbinding: ActivityMainBinding?, mmSocket : BluetoothSocket?) : Thread() {
        val bluetoothAdapter = bluetoothAdapter
        val context = context
        val mainbinding = mainbinding
        var mmSocket = mmSocket

        val bluetoothPairedList = ArrayList<String>()
        val bluetoothDeviceList = ArrayList<BluetoothDevice>()
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        override fun run() {
            val REQUEST_ENABLE_BT = 1
            var bluetoothdevice : BluetoothDevice? = null

            val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                println("Device doesn't support Bluetooth")
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address

                println("deviceName: " + deviceName)
                println("deviceHardwareAddress: " + deviceHardwareAddress)

                bluetoothPairedList.add((deviceName + "  /  " + deviceHardwareAddress))
                bluetoothDeviceList.add(device)
            }
            println("stop")
            println("bluetoothPairedList" + bluetoothPairedList)

            val arrayAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, bluetoothPairedList)
            mainbinding?.bluetoothList?.adapter   = arrayAdapter

            mainbinding?.bluetoothList?.setOnItemClickListener { parent, _, position, _ ->

                ConnectDeviceThread(context,bluetoothDeviceList,mainbinding,position,parent).run()


                //val selectedItem = parent.getItemAtPosition(position) as String
                //println(bluetoothDeviceList.get(position).name)
                //bluetoothdevice = bluetoothDeviceList.get(position)

                //val intent = Intent(context, PasswordSend::class.java)
                //intent.putExtra("bluetoothdevice", bluetoothdevice)
                //context.startActivity(intent)

            }



        }

        public class ConnectDeviceThread(val context: Context,
                                         val bluetoothDeviceList: ArrayList<BluetoothDevice>?,
                                         val mainbinding: ActivityMainBinding?,
                                         val position: Int, val parent: AdapterView<*>
        ) : Thread() {
            override fun run() {
                val selectedItem = parent.getItemAtPosition(position) as String
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                println(bluetoothDeviceList?.get(position)?.name)
                val bluetoothdevice = bluetoothDeviceList?.get(position)

                val intent = Intent(context, PasswordSend::class.java)
                intent.putExtra("bluetoothdevice", bluetoothdevice)
                context.startActivity(intent)

            }

        }


        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }


}



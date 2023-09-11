package com.atlastek.raspberrybluetoothconnection

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.atlastek.raspberrybluetoothconnection.databinding.ActivityPasswordSendBinding
import java.io.IOException
import java.util.UUID


class PasswordSend : AppCompatActivity() {

    private lateinit var passwordBinding: ActivityPasswordSendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passwordBinding = ActivityPasswordSendBinding.inflate(layoutInflater)
        setContentView(passwordBinding.root)

        passwordBinding.connectText.text = "Connecting..."
        var mmBuffer: ByteArray = ByteArray(1024)

        val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var mmSocket: BluetoothSocket? = null

        val bluetoothDevice = intent.extras!!.getParcelable<BluetoothDevice>("bluetoothdevice")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mmSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(MY_UUID)

        mmSocket?.let { socket ->
            try {
                mmSocket!!.connect()
            } catch (connectException: IOException) {
                try {
                    mmSocket!!.close()
                } catch (closeException: IOException) {
                    passwordBinding.connectText.text = "Not Connected."
                    Log.e(TAG, "Could not close the client socket", closeException)
                }
            }
            println("success")
            passwordBinding.connectText.text = "Connected."
        }

        passwordBinding.sendWifi.setOnClickListener{
            var wifiname = passwordBinding.wifiName.text.toString()
            var wifipassword = passwordBinding.wifiPassword.text.toString()
            var message = wifiname  + " " + wifipassword
            mmSocket?.outputStream?.write(message.toByteArray())
            while (true){
                try {
                    mmSocket?.inputStream?.read(mmBuffer)
                    passwordBinding.deviceIp.text = String(mmBuffer)
                    println(String(mmBuffer))
                    break;
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                }
            }

        }





    }
}
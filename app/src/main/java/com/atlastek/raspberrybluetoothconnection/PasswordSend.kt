package com.atlastek.raspberrybluetoothconnection

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.atlastek.raspberrybluetoothconnection.databinding.ActivityPasswordSendBinding
import org.json.JSONObject
import java.io.IOException
import java.util.UUID


class PasswordSend : AppCompatActivity() {

    private lateinit var passwordBinding: ActivityPasswordSendBinding
    var urlString : String = "https://www.google.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passwordBinding = ActivityPasswordSendBinding.inflate(layoutInflater)
        setContentView(passwordBinding.root)



        val selectedItem = intent.extras!!.getString("selectedItem")

        //passwordBinding.comingBluetoothData.visibility = View.INVISIBLE
        passwordBinding.connectText.text = "Bağlanıyor... Lütfen Bekleyiniz..."
        passwordBinding.connectText.setTextColor(Color.parseColor("#b00202"))


        var mmSocket: BluetoothSocket? = null


        passwordBinding.wifiName.setOnClickListener{

            val intent = Intent(this, WifiList::class.java)
            this.startActivity(intent)
        }



        val threadDeviceConnect = Thread {

            val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

            val bluetoothDevice = intent.extras!!.getParcelable<BluetoothDevice>("bluetoothdevice")


            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("permit not")
            }
            mmSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(MY_UUID)

            mmSocket?.let { socket ->
                try {
                    mmSocket!!.connect()
                    runOnUiThread {
                        passwordBinding.connectText.text = "Bağlandı."
                        passwordBinding.connectText.setTextColor(Color.parseColor("#018008"))
                    }


                } catch (connectException: IOException) {
                    try {
                        mmSocket!!.close()
                    } catch (closeException: IOException) {
                        runOnUiThread {
                            passwordBinding.connectText.text = "Bağlanamadı!"
                            passwordBinding.connectText.setTextColor(Color.parseColor("#b00202"))
                        }

                        Log.e(TAG, "Could not close the client socket", closeException)
                    }
                }

                val threadLamda = Thread {
                    while (true) {
                        try {
                            var mmBuffer: ByteArray = ByteArray(1024)
                            mmSocket?.inputStream?.read(mmBuffer)
                            //runOnUiThread { passwordBinding.comingBluetoothData.visibility = View.VISIBLE }
                            val json = JSONObject(String(mmBuffer))
                            println("**********" + json)
                            val threadText = Thread {
                                messageParsing(mmBuffer)
                            }
                            threadText.start()
                        } catch (e: IOException) {
                            Log.d(TAG, "Input stream was disconnected", e)
                            break;
                        }
                    }
                }
                threadLamda.start();
            }
        }

        threadDeviceConnect.start()

        passwordBinding.sendWifi.setOnClickListener {
            var wifiname = passwordBinding.wifiName.text.toString()
            var wifipassword = passwordBinding.wifiPassword.text.toString()
            var message = wifiname + " " + wifipassword
            mmSocket?.outputStream?.write(message.toByteArray())
        }

        passwordBinding.gosite.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.chrome")
            try {
                this.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                // Chrome browser presumably not installed so allow user to choose instead
                intent.setPackage(null)
                this.startActivity(intent)
            }
        }
    }


    fun messageParsing(mmBuffer: ByteArray){
        val json = JSONObject(String(mmBuffer))
        println(json)
        val command = json.getString("Command")

        if(command == "IP")
        {
            val data = json.getString("Data")
            runOnUiThread { passwordBinding.deviceIp.text = data.toString() }
            runOnUiThread { passwordBinding.yazilimVersion.text = "v1.0.23" }

        }
        else if(command == "URL")
        {
            val data = json.getString("Data")
            urlString = data
            runOnUiThread { passwordBinding.url.text = data.toString() }

        }
        else if(command == "PRINTER")
        {
            val data = json.getString("Data")
            val jsonData = JSONObject(data)
            val isAvaible = jsonData.getString("isAvaible")
            var connect = "Bağlanamadı!"
            if(isAvaible == "false"){
                connect = "Bağlanamadı!"
            } else if(isAvaible == "true"){
                connect = "Bağlandı"
            }
            runOnUiThread { passwordBinding.printingStatus.text = connect.toString() }
        }
        else if(command == "DBYS_CONNECTION")
        {


        }
        else if(command == "SERIAL_NUMBER")
        {
            val data = json.getString("Data")
            runOnUiThread { passwordBinding.seriNo.text = data.toString() }
        }


    }
}
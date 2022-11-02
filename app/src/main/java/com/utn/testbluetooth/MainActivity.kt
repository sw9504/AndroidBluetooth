package com.utn.testbluetooth

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.ingenieriajhr.blujhr.BluJhr
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var blue : BluJhr
    var devicesBluetooth = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blue = BluJhr(this)
        blue.onBluetooth()

        listDeviceBluetooth.setOnItemClickListener { adapterView, view, i, l ->
            if (devicesBluetooth.isNotEmpty()) {
                blue.connect(devicesBluetooth[i])
                blue.setDataLoadFinishedListener(object:BluJhr.ConnectedBluetooth{
                    override fun onConnectState(state: BluJhr.Connected) {
                        when(state) {

                            BluJhr.Connected.True->{
                                Toast.makeText(applicationContext,"True",Toast.LENGTH_SHORT).show()
                                listDeviceBluetooth.visibility = View.GONE
                                viewConn.visibility = View.VISIBLE
                                rxReceived()
                            }

                            BluJhr.Connected.Pending->{
                                Toast.makeText(applicationContext,"Pending",Toast.LENGTH_SHORT).show()

                            }

                            BluJhr.Connected.False->{
                                Toast.makeText(applicationContext,"False",Toast.LENGTH_SHORT).show()
                            }

                            BluJhr.Connected.Disconnect->{
                                Toast.makeText(applicationContext,"Disconnect",Toast.LENGTH_SHORT).show()
                                listDeviceBluetooth.visibility = View.VISIBLE
                                viewConn.visibility = View.GONE
                            }

                        }
                    }
                })
            }
        }
    }

    private fun rxReceived() {
        blue.loadDateRx(object:BluJhr.ReceivedData{
            override fun rxDate(rx: String) {
                //consola.text = consola.text.toString()+rx
                //consola.text = rx
                //sensorShow.text = rx

                var list : List<String> = rx.split(",")

                if (list.isNullOrEmpty() || list.size < 7)
                    //sensorShow.text = "ERROR"
                else if ((list[0] != "$" && list[6] != "#"))
                    //sensorShow.text = "ERROR"
                else if (list[1] == "S1") {
                    sensorShow.text = "#SENSOR1: T = ${list[3]}°C H = ${list[5]} \n"
                }
                else if (list[1] == "S2") {
                    sensorShow.text = sensorShow.text.toString() + "#SENSOR2: T = ${list[3]}°C H = ${list[5]} \n"
                }
                else
                    sensorShow.text = "ERROR"
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (blue.checkPermissions(requestCode,grantResults)){
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                blue.initializeBluetooth()
            }else{
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
package com.example.pidkonfig

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.InputType
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.pidkonfig.ui.main.SectionsPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CommunicationHandler (context: Context, bluetoothManager: BluetoothManager, setting: Setting, sectionsPagerAdapter: SectionsPagerAdapter, sharedPref: SharedPreferences, mainLayout: CoordinatorLayout) {
    val context = context
    val bluetoothManager = bluetoothManager
    val setting = setting
    val sectionsPagerAdapter = sectionsPagerAdapter
    val sharedPref = sharedPref
    val mainLayout = mainLayout

    var connecting = false
    var connected = false
    var menuItem : MenuItem? = null


    @SuppressLint("CheckResult")
    fun connectDevice(mac: String) {
        bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ connectedDevice: BluetoothSerialDevice ->
                onConnected(
                    connectedDevice
                )
            }) { error: Throwable -> onError(error) }
    }
    fun disconnect() {
        bluetoothManager.close();
        connected = false
        if (menuItem != null) {
            menuItem?.icon = context.getDrawable(R.drawable.ic_baseline_bluetooth_24)
        }
        val mySnackbar = Snackbar.make(mainLayout,
            R.string.bluetooth_disconnect , Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    var sendLock = false
    var deviceInterface : SimpleBluetoothDeviceInterface? = null

    fun tryConnect() : Unit {
        connecting = true
        fun nameTest(deviceName : String) : String {
            var mac : String = ""
            val pairedDevices: Collection<BluetoothDevice> =
                bluetoothManager.pairedDevicesList
            var matches = 0
            for (device in pairedDevices) {
                if (device.name == deviceName) {
                    mac = device.address
                    matches++
                }
            }
            if (matches == 0){
                with (sharedPref.edit()) {
                    putString("bt_name", "")
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putString("bt_name", deviceName)
                    apply()
                }
            }
            if(matches != 1){
                if (matches == 0 ) {tryConnect()}
                Toast.makeText(context, when(matches) {0 -> {
                    R.string.no_device_with_name
                }
                    else -> R.string.more_devices_with_name
                }, Toast.LENGTH_LONG)
                    .show() // Replace context with your context instance.

            }
            return mac
        }
        if (BluetoothAdapter.getDefaultAdapter().isEnabled){
            val mySnackbar = Snackbar.make(mainLayout,
                R.string.bluetooth_connecting , Snackbar.LENGTH_LONG)
            mySnackbar.show()
            var deviceName = sharedPref.getString("bt_name", "")
            if (deviceName == "") {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.title_dialog_devicename)
                val input = EditText(context)

                input.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                builder.setView(input)

                builder.setPositiveButton(
                    R.string.ok
                ) { dialog, which -> run {
                    deviceName = input.text.toString()
                    val isItMac = nameTest(deviceName!!)
                    if (isItMac == ""){
                        tryConnect()
                    }else{
                        connectDevice(isItMac)
                    }
                } }
                builder.setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
                builder.show()
            }else{
                val isItMac = nameTest(deviceName!!)
                if (isItMac == ""){
                    tryConnect()
                }else{
                    connectDevice(isItMac)
                }
            }
        }else{
            val mySnackbar = Snackbar.make(mainLayout,
                R.string.enable_bluetooth , Snackbar.LENGTH_SHORT)
            mySnackbar.show()
            connecting = false
        }



    }

    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        deviceInterface?.setListeners(
            { message: String ->
                onMessageReceived(
                    message
                )
            },
            { message: String -> onMessageSent(message) },

            // Listen to bluetooth events
            { error: Throwable -> onError(error) })
        sendLock = false
        connected = true
        connecting = false
        if (menuItem != null) {
            menuItem?.icon = context.getDrawable(R.drawable.ic_baseline_bluetooth_connected_24)
        }
        val mySnackbar = Snackbar.make(mainLayout,
            context.getString(R.string.connect_success) + ("\ud83d\ude01") , Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    fun send(message: String) {
        // Let's send a message:
        if (connected && !sendLock) {
            deviceInterface!!.sendMessage(message)
            sendLock = true
        }
    }

    private fun onMessageSent(message: String) {
        sendLock = false
    }

    var onConfigReceived : (() -> Unit)? = null
    private var receivingConfig = false
    private fun onMessageReceived(message: String) {
        // We received a message! Handle it here.
        if (message == "cnf=1") {
            receivingConfig = true
        }else if (message == "cnf=0" && receivingConfig) {
            receivingConfig = false
            if (onConfigReceived != null) {
                onConfigReceived?.invoke()
            }
            val mySnackbar = Snackbar.make(mainLayout,
                R.string.config_received, Snackbar.LENGTH_SHORT)
            mySnackbar.show()
            sectionsPagerAdapter.callOnSettingChange()
        }else{
            val splitted = message.split("=")
            if (splitted.size == 2) {
                val cmdName = splitted[0]
                val value = splitted[1]
                when (cmdName) {
                    "mpe" -> setting.M_pidEnable = when(value) { "0" -> false
                        "1" -> true
                        else -> false
                    }
                    "mkp" -> setting.mKp = value.toFloat()
                    "mki" -> setting.mKi = value.toFloat()
                    "mbs" -> setting.mbs = value.toFloat()
                    "lkp" -> setting.Kp = value.toFloat()
                    "lkd" -> setting.Kd = value.toFloat()
                    "lki" -> setting.Ki = value.toFloat()
                    "lkl" -> setting.K_lineLost = value.toFloat()
                    "ltp" -> setting.target_linepos = value.toFloat()
                }
            }
        }

    }

    private fun onError(error: Throwable) {
        if (connecting) connecting = false
        val mySnackbar = Snackbar.make(mainLayout,
            R.string.bluetooth_error , Snackbar.LENGTH_LONG)
        mySnackbar.show()
    }
}
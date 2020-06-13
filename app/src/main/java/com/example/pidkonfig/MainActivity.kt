package com.example.pidkonfig

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.example.pidkonfig.ui.main.PlaceholderFragment
import com.example.pidkonfig.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.harrysoft.androidbluetoothserial.BluetoothManager

data class Setting(var saved: Boolean,
                   var Kp: Float,
                   var Kd: Float,
                   var Ki: Float,
                   var K_lineLost:Float,
                   var target_linepos: Float,

                   var M_pidEnable : Boolean,
                   var mKp: Float, var mKi: Float,
                   var mbs: Float)

class MainActivity : AppCompatActivity() {
    var comHandler : CommunicationHandler? = null

    var setting = Setting(false,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f,
        true, 0.0f, 0.0f, 0.0f)
    var bluetoothManager : BluetoothManager? = null
    var sharedPref : SharedPreferences? = null
    var menuItem : MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setSupportActionBar(findViewById(R.id.toolbar))

        // Setup our BluetoothManager
        // Setup our BluetoothManager
        bluetoothManager =
            BluetoothManager.getInstance()
        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(this, "Bluetooth not available.", Toast.LENGTH_LONG)
                .show() // Replace context with your context instance.
            finish()
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        comHandler = CommunicationHandler(
            context = this,
            bluetoothManager = bluetoothManager!!,
            setting = setting,
            sharedPref = sharedPref!!,
            mainLayout = findViewById<CoordinatorLayout>(R.id.main_layout)
        )

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menuItem = menu.getItem(0)
        comHandler?.menuItem = menuItem
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                val i = Intent(this, SettingsActivity::class.java)
                this.startActivity(i)
                true
            }
            R.id.bluetooth_button -> {
                if (comHandler?.connected == false && comHandler?.connecting == false) {
                    comHandler?.tryConnect()
                }else if(comHandler?.connected == true) {
                    comHandler?.disconnect()
                    comHandler = null
                    bluetoothManager =
                        BluetoothManager.getInstance()
                    comHandler = CommunicationHandler(
                        context = this,
                        bluetoothManager = bluetoothManager!!,
                        setting = setting,
                        sharedPref = sharedPref!!,
                        mainLayout = findViewById<CoordinatorLayout>(R.id.main_layout)
                    )
                    comHandler?.menuItem = menuItem
                }
                true
            }
            R.id.reload -> {
                comHandler?.send("get=0\n")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(comHandler?.connected == true) {
            comHandler?.disconnect()
        }
    }
}
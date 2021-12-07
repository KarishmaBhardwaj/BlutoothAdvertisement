package com.karishma.blutoothadvertisement.activity

import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.SyncStateContract
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.karishma.blutoothadvertisement.fragment.AdvertiserFragment
import com.karishma.blutoothadvertisement.fragment.ScannerFragment
import com.karishma.blutoothadvertisement.utils.Constants

class MainActivity : FragmentActivity() {
    private var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.karishma.blutoothadvertisement.R.layout.activity_main)


        setTitle(R.string.activity_main_title)

        if (savedInstanceState == null) {
            mBluetoothAdapter =
                (getSystemService<Any>(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                    .adapter

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter!!.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter!!.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        setupFragments()
                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(com.karishma.blutoothadvertisement.R.string.bt_ads_not_supported)
                    }
                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT)
                }
            } else {

                // Bluetooth is not supported.
                showErrorText(com.karishma.blutoothadvertisement.R.string.bt_not_supported)
            }
        }
    }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                SyncStateContract.Constants.REQUEST_ENABLE_BT -> {
                    if (resultCode == RESULT_OK) {

                        // Bluetooth is now Enabled, are Bluetooth Advertisements supported on
                        // this device?
                        if (mBluetoothAdapter!!.isMultipleAdvertisementSupported) {

                            // Everything is supported and enabled, load the fragments.
                            setupFragments()
                        } else {

                            // Bluetooth Advertisements are not supported.
                            showErrorText(R.string.bt_ads_not_supported)
                        }
                    } else {

                        // User declined to enable Bluetooth, exit the app.
                        Toast.makeText(
                            this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    super.onActivityResult(requestCode, resultCode, data)
                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        }

        private fun setupFragments() {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val scannerFragment = ScannerFragment()
            // Fragments can't access system services directly, so pass it the BluetoothAdapter
            scannerFragment.setBluetoothAdapter(mBluetoothAdapter)
            transaction.replace(R.id.scanner_fragment_container, scannerFragment)
            val advertiserFragment = AdvertiserFragment()
            transaction.replace(R.id.advertiser_fragment_container, advertiserFragment)
            transaction.commit()
        }

        private fun showErrorText(messageId: Int) {
            val view = findViewById<View>(R.id.error_textview) as TextView
            view.text = getString(messageId)
        }

}

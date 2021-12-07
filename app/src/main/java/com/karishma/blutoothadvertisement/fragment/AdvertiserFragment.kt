package com.karishma.blutoothadvertisement.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.karishma.blutoothadvertisement.R
import android.content.BroadcastReceiver

import android.widget.Switch
import android.widget.Toast

import com.karishma.blutoothadvertisement.service.AdvertiserService

import android.bluetooth.le.AdvertiseCallback
import android.content.Context

import android.content.Intent
import android.content.IntentFilter

class AdvertiserFragment : Fragment(), View.OnClickListener {

    private var mSwitch: Switch? = null

    /**
     * Listens for notifications that the `AdvertiserService` has failed to start advertising.
     * This Receiver deals with Fragment UI elements and only needs to be active when the Fragment
     * is on-screen, so it's defined and registered in code instead of the Manifest.
     */
    private var advertisingFailureReceiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        advertisingFailureReceiver = object : BroadcastReceiver() {
            /**
             * Receives Advertising error codes from `AdvertiserService` and displays error messages
             * to the user. Sets the advertising toggle to 'false.'
             */
            override fun onReceive(context: Context?, intent: Intent) {
                val errorCode =
                    intent.getIntExtra(AdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1)
                mSwitch!!.isChecked = false
                var errorMessage = getString(R.string.start_error_prefix)
                when (errorCode) {
                    AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> errorMessage += " " + getString(
                        R.string.start_error_already_started
                    )
                    AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> errorMessage += " " + getString(
                        R.string.start_error_too_large
                    )
                    AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> errorMessage += " " + getString(
                        R.string.start_error_unsupported
                    )
                    AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> errorMessage += " " + getString(
                        R.string.start_error_internal
                    )
                    AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> errorMessage += " " + getString(
                       R.string.start_error_too_many
                    )
                    AdvertiserService.ADVERTISING_TIMED_OUT -> errorMessage =
                        " " + getString(R.string.advertising_timedout)
                    else -> errorMessage += " " + getString(R.string.start_error_unknown)
                }
                Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View = inflater.inflate(R.layout.fragment_advertiser, container, false)

        mSwitch = view.findViewById<View>(R.id.advertise_switch) as Switch
        mSwitch?.setOnClickListener(this)

        return view;
    }

    override fun onResume() {
        super.onResume()
        if (AdvertiserService.running) {
            mSwitch!!.isChecked = true
        } else {
            mSwitch!!.isChecked = false
        }
        val failureFilter = IntentFilter(AdvertiserService.ADVERTISING_FAILED)
        activity?.registerReceiver(advertisingFailureReceiver, failureFilter)
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(advertisingFailureReceiver)
    }

    private fun getServiceIntent(c: Context): Intent? {
        return Intent(c, AdvertiserService::class.java)
    }


    override fun onClick(v: View?) {
        val on = (v as Switch).isChecked

        if (on) {
            startAdvertising()
        } else {
            stopAdvertising()
        }
    }

    private fun startAdvertising() {
        val c: Context? = activity
        c!!.startService(getServiceIntent(c))
    }

    /**
     * Stops BLE Advertising by stopping `AdvertiserService`.
     */
    private fun stopAdvertising() {
        val c: Context? = activity
        c!!.stopService(getServiceIntent(c))
        mSwitch!!.isChecked = false
    }


}
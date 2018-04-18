package ciandt.com.navigation.view

import android.app.Notification
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import ciandt.com.navigation.BuildConfig
import ciandt.com.navigation.R
import ciandt.com.navigation.model.BeaconTo
import ciandt.com.navigation.model.NotificationCreator
import ciandt.com.navigation.view.main.BeaconAdapter
import ciandt.com.navigation.view.main.BeaconAdapter.ItemClickListener
import com.estimote.coresdk.common.config.EstimoteSDK
import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials
import com.estimote.proximity_sdk.proximity.ProximityObserver
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder
import com.estimote.proximity_sdk.trigger.ProximityTriggerBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_beacon_rv.*

class MainActivity : AppCompatActivity() {

    // Estimote proximity ini
    private lateinit var notification: Notification
    private lateinit var proximityObserver: ProximityObserver
    private var proximityObservationHandler: ProximityObserver.Handler? = null
    private val cloudCredentials = EstimoteCloudCredentials(BuildConfig.ESTIMOTE_APP_ID_DEBUG, BuildConfig.ESTIMOTE_APP_TOKEN_DEBUG)
    // Estimote proximity end

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                hideMock()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
                message.setText("")
                showMock()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_directions -> {
                message.setText(R.string.title_directions)
                hideMock()

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun createHistoryMock() {
        val histories = ArrayList<BeaconTo>(10)
        for (i in 0 until 10) {
            histories.add(BeaconTo("name $i", "Lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum"))
        }

        val itemClickListener = object : ItemClickListener {
            override fun onClick(beacon: BeaconTo) {
                Toast.makeText(this@MainActivity, beacon.name, Toast.LENGTH_SHORT).show()
            }
        }

        recyclerview.setLayoutManager(LinearLayoutManager(this))
        recyclerview.setAdapter(BeaconAdapter(this, itemClickListener, histories, emptyview))
    }

    fun showMock() {
        recyclerview.visibility = VISIBLE
    }

    fun hideMock() {
        recyclerview.visibility = INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Estimote ini
        EstimoteSDK.initialize(applicationContext, BuildConfig.ESTIMOTE_APP_ID_DEBUG, BuildConfig.ESTIMOTE_APP_TOKEN_DEBUG)
        // Estimote end

        // Estimote proximity ini
        notification = NotificationCreator().createNotification(this)

        proximityObserver =
                ProximityObserverBuilder(applicationContext, cloudCredentials)
                        .withScannerInForegroundService(notification)
                        .withBalancedPowerMode()
                        .build()
        // Estimote proximity end

        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        proximityObservationHandler = proximityObserver
                //.addProximityZones(venueZone, mintDeskZone, blueberryDeskZone)
                .start()

        createHistoryMock()
    }

    // Estimote proximity
    override fun onDestroy() {
        super.onDestroy()
        proximityObservationHandler?.stop()
    }

    // Estimote proximity
    private fun showTriggerSetupDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "ProximityTrigger works only on devices with Android 8.0+", Toast.LENGTH_SHORT).show()
        } else {
            createTriggerDialog().show()
        }
    }

    private fun createTriggerDialog() =
            AlertDialog.Builder(this)
                    .setTitle("ProximityTrigger setup")
                    .setMessage("The ProximityTrigger will display your notification when the user" +
                            " has entered the proximity of beacons. " +
                            "You can leave your beacons range, enable the trigger, kill your app, " +
                            "and go back - see what happens!")
                    .setPositiveButton("Enable", { _, _ ->
                        val notification = NotificationCreator().createTriggerNotification(this)
                        ProximityTriggerBuilder(this)
                                .displayNotificationWhenInProximity(notification)
                                .build()
                                .start()
                        Toast.makeText(this, "Trigger enabled!", Toast.LENGTH_SHORT).show()
                    })
                    .setNegativeButton("Disable", { _, _ ->
                        val notification = NotificationCreator().createTriggerNotification(this)
                        ProximityTriggerBuilder(this).displayNotificationWhenInProximity(notification)
                                .build()
                                .start().stop()
                        Toast.makeText(this, "Trigger disabled.", Toast.LENGTH_SHORT).show()
                    }).create()
}
package ciandt.com.navigation.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import ciandt.com.navigation.R
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.recognition.packets.Beacon
import com.estimote.coresdk.service.BeaconManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val SCAN_PERIOD = (10 * 1000).toLong()
    private val SCAN_INTERVAL = (5 * 1000).toLong()

    // 23B
    private val IDENTIFIER_23B = "prédio 23B"
    private val UUID_23B = "B9407F30-F5F8-466E-AFF9-25556B57FE6D"
    private val BEACON_MAJOR_23B_RECEPTION = 15673
    private val BEACON_MINOR_23B_RECEPTION = 2504

    // MALL
    private val IDENTIFIER_MALL = "alameda Ci&T"
    private val UUID_MALL = "687DBC06-BE1C-424C-B0EC-942B2A729674"
    private val BEACON_MAJOR_ENTRANCE_MALL = 52381
    private val BEACON_MINOR_ENTRANCE_MALL = 22058

    // String
    private val DOUBLE_DOT = ":"

    private lateinit var beaconManager: BeaconManager
    private lateinit var region23B: BeaconRegion
    private lateinit var regionMall: BeaconRegion

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
                message.setText(R.string.title_history)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_directions -> {
                message.setText(R.string.title_directions)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        beaconManager = BeaconManager(this)
        // Scan period and interval
        beaconManager.setBackgroundScanPeriod(SCAN_PERIOD, SCAN_INTERVAL)

        beaconManager.setRangingListener(BeaconManager.BeaconRangingListener { region, list ->
            if (!list.isEmpty()) {
                val nearestBeacon = list[0]
                val places = placesNearBeacon(nearestBeacon)

                val text = "Região: " + region.identifier + " " + places.toString()
                //mTextMessage.setText(text)

                Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()


                Log.d("DEBUG: ", text)
            }
        })

        region23B = BeaconRegion(IDENTIFIER_23B,
                UUID.fromString(UUID_23B), null, null)
        regionMall = BeaconRegion(IDENTIFIER_MALL,
                UUID.fromString(UUID_MALL), null, null)
    }

    override fun onResume() {
        super.onResume()

        SystemRequirementsChecker.checkWithDefaultDialogs(this)
        beaconManager.connect(BeaconManager.ServiceReadyCallback {
            beaconManager.startRanging(region23B)
            beaconManager.startRanging(regionMall)
        })
    }

    override fun onPause() {
        beaconManager.stopRanging(region23B)
        beaconManager.stopRanging(regionMall)

        super.onPause()
    }

    private fun placesNearBeacon(beacon: Beacon): List<String>? {
        var placesByBeacons = HashMap<String, List<String>>()

        placesByBeacons.put(
                (BEACON_MAJOR_23B_RECEPTION).toString() +
                        DOUBLE_DOT +
                        BEACON_MINOR_23B_RECEPTION, object : ArrayList<String>() {
            init {
                add("Recepção")
                add("Garagem")
                add("Marketing")
                add("Recursos Humanos")
            }
        })
        placesByBeacons.put(
                ((BEACON_MAJOR_ENTRANCE_MALL).toString() +
                        DOUBLE_DOT +
                        BEACON_MINOR_ENTRANCE_MALL), object : ArrayList<String>() {
            init {
                add("Mesas")
                add("Bilhar")
                add("Pimbolim")
            }
        })

        val beaconKey = String.format("%d:%d", beacon.major, beacon.minor)

        return if (placesByBeacons.containsKey(beaconKey)) {
            placesByBeacons.get(beaconKey)
        } else emptyList()
    }
}
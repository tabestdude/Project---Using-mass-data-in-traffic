package feri.aplikacijaprojekt

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import feri.aplikacijaprojekt.databinding.ActivityMainBinding
import kotlin.math.pow
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private lateinit var mGyroscope: Sensor

    private lateinit var accXTextView: TextView
    private lateinit var accYTextView: TextView
    private lateinit var accZTextView: TextView

    private lateinit var giroXTextView: TextView
    private lateinit var giroYTextView: TextView
    private lateinit var giroZTextView: TextView

    private var previousPosition: FloatArray? = null
    private var currentPosition: FloatArray? = null
    private var previousTimestamp: Long = 0

    private lateinit var speedTextView: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // referenca na sensorManager na napravi
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // findView za elemente
        accXTextView = findViewById<TextView>(R.id.accX2)
        accYTextView = findViewById<TextView>(R.id.accY2)
        accZTextView = findViewById<TextView>(R.id.accZ2)

        giroXTextView = findViewById<TextView>(R.id.giroX)
        giroYTextView = findViewById<TextView>(R.id.giroY)
        giroZTextView = findViewById<TextView>(R.id.giroZ)

        speedTextView = findViewById<TextView>(R.id.speed)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        latitudeTextView = findViewById<TextView>(R.id.latitude)
        longitudeTextView = findViewById<TextView>(R.id.longitude)

        // inicializacija senzorjev
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val infoButtonClick = findViewById<Button>(R.id.infoButton)
        infoButtonClick.setOnClickListener {
            val intent = Intent(this@MainActivity, InfoActivity::class.java)
            startActivity(intent)
        }

        val mapButtonClick = findViewById<Button>(R.id.mapButton)
        mapButtonClick.setOnClickListener {
            val intent2 = Intent(this@MainActivity, MapActivity::class.java)
            startActivity(intent2)
        }

        Log.d("MainActivity", "Test log.")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onStart() {
        super.onStart()

        // Check if the user has granted permission to access the location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request the last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Handle the new location
                if (location != null) {
                    // Use the location data
                    val latitude = location.latitude
                    val longitude = location.longitude
                    latitudeTextView.text = "Latitude: ${location.latitude}"
                    longitudeTextView.text = "Longitude: ${location.longitude}"
                    Log.d("MainActivity", "Latitude: $latitude, Longitude: $longitude")
                }
            }
        } else {
            // Request permission to access the location
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, call the onStart() function again
                onStart()
            } else {
                // Permission denied, show an error message to the user
                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // registracija senzorjev
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()

        // odregistracija senzorjev
        mSensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // x y z TextView se posodobi
            accXTextView.text = "X: ${event.values[0]}"
            accYTextView.text = "Y: ${event.values[1]}"
            accZTextView.text = "Z: ${event.values[2]}"

            // Calculate the speed
            val currentTime = System.currentTimeMillis()
            if (previousPosition != null && previousTimestamp != 0L) {
                currentPosition = event.values.clone()
                val timeDelta = (currentTime - previousTimestamp) / 1000.0f // Convert to seconds
                val displacement =
                    calculateDisplacement(currentPosition!!, previousPosition!!, timeDelta)
                val distance = calculateDistance(displacement)
                val speed = distance / timeDelta
                speedTextView.text = "Speed: $speed m/s"
            }
            previousPosition = event.values.clone()
            previousTimestamp = currentTime
        } else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            // x y z TextView se posodobi
            giroXTextView.text = "X: ${event.values[0]}"
            giroYTextView.text = "Y: ${event.values[1]}"
            giroZTextView.text = "Z: ${event.values[2]}"
        }
    }

    private fun calculateDisplacement(
        currentPosition: FloatArray,
        previousPosition: FloatArray,
        timeDelta: Float
    ): FloatArray {
        val displacement = FloatArray(3)
        displacement[0] = (currentPosition[0] - previousPosition[0]) * timeDelta * timeDelta / 2
        displacement[1] = (currentPosition[1] - previousPosition[1]) * timeDelta * timeDelta / 2
        displacement[2] = (currentPosition[2] - previousPosition[2]) * timeDelta * timeDelta / 2
        return displacement
    }

    private fun calculateDistance(displacement: FloatArray): Float {
        val distance = Math.sqrt(displacement[0].toDouble().pow(2) + displacement[1].toDouble().pow(2) + displacement[2].toDouble().pow(2)).toFloat()
        return distance
    }

}
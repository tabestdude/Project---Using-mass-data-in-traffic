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
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

var globalGiroX = 0.0;
var globalGiroY = 0.0;
var globalGiroZ = 0.0;

var globalAccX = 0.0;
var globalAccY = 0.0;
var globalAccZ = 0.0;

var globalLongitude = 0.0;
var globalLatitude = 0.0;

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val handler = Handler()

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

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onStart() {
        super.onStart()
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
        startRunnable()
        sendToDatabase()
        // registracija senzorjev
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun startRunnable() {
        handler.postDelayed({
            // Code to run every 5 seconds
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

                        globalLatitude = location.latitude.toDouble()
                        globalLongitude = location.longitude.toDouble()

                        //Log.d("MainActivity", "Latitude: $latitude, Longitude: $longitude")
                    }
                }
            } else {
                // Request permission to access the location
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
            startRunnable() // Schedule the code to run again after 5 seconds
        }, 5000)
    }

    override fun onPause() {
        super.onPause()

        // odregistracija senzorjev
        mSensorManager.unregisterListener(this)
    }

    private var lastUpdateTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            val currentTime1 = System.currentTimeMillis()

            if (currentTime1 - lastUpdateTime >= 5000) {
                // x y z TextView se posodobi
                accXTextView.text = "X: ${event.values[0]}"
                accYTextView.text = "Y: ${event.values[1]}"
                accZTextView.text = "Z: ${event.values[2]}"

                globalAccX = event.values[0].toDouble()
                globalAccY = event.values[1].toDouble()
                globalAccZ = event.values[2].toDouble()
            }
        } else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 5000) {
                // x y z TextView se posodobi
                giroXTextView.text = "X: ${event.values[0]}"
                giroYTextView.text = "Y: ${event.values[1]}"
                giroZTextView.text = "Z: ${event.values[2]}"

                globalGiroX = event.values[0].toDouble()
                globalGiroY = event.values[1].toDouble()
                globalGiroZ = event.values[2].toDouble()

                lastUpdateTime = currentTime
            }
        }
    }

    private val client = OkHttpClient()

    private fun sendDataToServer(data: JSONObject) {
        Log.d("MainActivity", "Sending data to server 1.")

        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = data.toString().toRequestBody(jsonMediaType)
        Log.d("MainActivity", "Sending data to server 2.")
        val request = Request.Builder()
            .url("http://192.168.1.130:3000/api/data")
            .post(requestBody)
            .build()
        Log.d("MainActivity", "Sending data to server 3.")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d("MainActivity", "Sending data to server 4.")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("MainActivity", "Server response: $response")
                    Log.d("MainActivity", "Sending data to server 5.")
                    throw IOException("Unexpected code $response")
                } else {
                    val responseData = response.body?.string()
                    Log.d("MainActivity", "Server response: $responseData")
                    Log.d("MainActivity", "Sending data to server 6.")
                }
            }
        })
        Log.d("MainActivity", "Sending data to server 7.")
    }

    private fun sendToDatabase() {
        handler.postDelayed({
            // Code to run every 5 seconds

            // Create a new JSONObject with the sensor data
            val dataToSend = JSONObject()
            dataToSend.put("giroX", globalGiroX)
            dataToSend.put("giroY", globalGiroY)
            dataToSend.put("giroZ", globalGiroZ)
            dataToSend.put("accX", globalAccX)
            dataToSend.put("accY", globalAccY)
            dataToSend.put("accZ", globalAccZ)
            dataToSend.put("longitude", globalLongitude)
            dataToSend.put("latitude", globalLatitude)
            dataToSend.put("ownerId", null) // Set the ownerId to null for now

            sendDataToServer(dataToSend)

            //Log.d("MainActivity", "Sending data to server.")

        }, 5000)
    }

}



/*val message =
    "GiroX: $globalGiroX\nGiroY: $globalGiroY\nGiroZ: $globalGiroZ\nAccX: $globalAccX\nAccY: $globalAccY\nAccZ: $globalAccZ\nLongitude: $globalLongitude\nLatitude: $globalLatitude\n"
dialogBuilder.setMessage(message)
dialogBuilder.setPositiveButton("OK", null)
val dialog = dialogBuilder.create()
dialog.show()
val dialogBuilder = AlertDialog.Builder(this)
dialogBuilder.setTitle("Variables sent to the Database.")
*/ // Schedule the code to run again after 5 seconds
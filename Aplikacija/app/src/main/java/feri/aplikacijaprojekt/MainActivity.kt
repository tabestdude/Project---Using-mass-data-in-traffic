package feri.aplikacijaprojekt

import android.Manifest
import android.content.ContentValues.TAG
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

var globalLongitude = 0.0;
var globalLatitude = 0.0;

private var wasDataSent = false

private var isRunning = false

private var sendToggleSwitch = false;

class MainActivity : AppCompatActivity() {

    private val handler = Handler()

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var mSensorManager: SensorManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView

    private lateinit var runningDisplayTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // referenca na sensorManager na napravi
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        latitudeTextView = findViewById<TextView>(R.id.latitude)
        longitudeTextView = findViewById<TextView>(R.id.longitude)

        runningDisplayTextView = findViewById<TextView>(R.id.runningDisplay)

        val infoButtonClick = findViewById<Button>(R.id.infoButton)
        infoButtonClick.setOnClickListener {
            val intent = Intent(this@MainActivity, InfoActivity::class.java)
            startActivity(intent)
        }

        val toggleButtonClick = findViewById<Button>(R.id.toggleButton)
        toggleButtonClick.setOnClickListener {
            isRunning = !isRunning
            runningDisplayTextView.text = if (isRunning) "Enabled" else "Disabled"
            sendToggleSwitch = true
        }

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
        CoroutineScope(Dispatchers.Main).launch {
            sendToBoardServer()
        }
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
                        latitudeTextView.text = "Latitude: ${location.latitude}"
                        longitudeTextView.text = "Longitude: ${location.longitude}"

                        globalLatitude = location.latitude
                        globalLongitude = location.longitude
                    }
                }
            } else {
                // Request permission to access the location
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
            startRunnable() // Schedule the code to run again after 5 seconds
        }, 1000)
    }

    interface ApiServiceData {
        @POST("gpsData")
        suspend fun sendData(@Body data: RequestBody): Response<Unit>
    }

    interface ApiServiceToggle {
        @POST("toggleBoard")
        suspend fun sendData(@Body data: RequestBody): Response<Unit>
    }

    private suspend fun sendDataToServer(data: JSONObject, sendingToggle: Boolean) {
        Log.d("MainActivity", "Sending data: $data")
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // <-- Increase timeout value
                    .build()
            )
            .build()

        val requestBody = data.toString().toRequestBody("application/json".toMediaTypeOrNull())

        var response: Response<Unit>? = null
        var retries = 0
        while (response == null && retries < 3) {
            try {
                if (sendingToggle) {
                    response = retrofit.create(ApiServiceToggle::class.java).sendData(requestBody)
                } else{
                    response = retrofit.create(ApiServiceData::class.java).sendData(requestBody)
                }

            } catch (e: SocketTimeoutException) {
                retries++
                Log.e(TAG, "Socket timeout exception, retrying...")
            }
        }
        Log.d("MainActivity", "Response: $response")
        if (response != null && response.isSuccessful) {
            Log.d(TAG, "Data sent successfully")
            Log.d(TAG, "Response: ${response.body()}")
            wasDataSent = true
        } else {
            Log.e(TAG, "Failed to send data or no response from server")
        }
    }

    private suspend fun sendToBoardServer() {
        val userId = intent.getStringExtra("USER_ID")
        while (true) {
            if (sendToggleSwitch){
                sendToggleSwitch = false
                val dataToSend = JSONObject()
                dataToSend.put("isRunning", isRunning)

                sendDataToServer(dataToSend, true)
            }
            if (isRunning){
                // Create a new JSONObject with the sensor data
                val dataToSend = JSONObject()
                dataToSend.put("longitude", globalLongitude)
                dataToSend.put("latitude", globalLatitude)
                dataToSend.put("ownerId", userId) // Set the ownerId to null for now

                Log.d("MainActivity", "Data to send: $dataToSend")

                sendDataToServer(dataToSend, false)
            }
            delay(1000) // Wait 2 seconds before sending the next data point
        }
    }
}
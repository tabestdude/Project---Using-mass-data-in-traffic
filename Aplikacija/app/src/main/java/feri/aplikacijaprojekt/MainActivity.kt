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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
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

/*import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException*/


var globalAccX = DoubleArray(6)
var globalAccY = DoubleArray(6)
var globalAccZ = DoubleArray(6)

var globalLongitude = 0.0;
var globalLatitude = 0.0;

private var counter = 0

private var wasDataSent = false

private const val dataArraySize = 6

private var isRunning = false

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val handler = Handler()

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor

    private lateinit var accXTextView: TextView
    private lateinit var accYTextView: TextView
    private lateinit var accZTextView: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView

    private lateinit var runningDisplayTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // referenca na sensorManager na napravi
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // findView za elemente
        accXTextView = findViewById<TextView>(R.id.accX2)
        accYTextView = findViewById<TextView>(R.id.accY2)
        accZTextView = findViewById<TextView>(R.id.accZ2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        latitudeTextView = findViewById<TextView>(R.id.latitude)
        longitudeTextView = findViewById<TextView>(R.id.longitude)

        runningDisplayTextView = findViewById<TextView>(R.id.runningDisplay)

        // inicializacija senzorjev
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val infoButtonClick = findViewById<Button>(R.id.infoButton)
        infoButtonClick.setOnClickListener {
            val intent = Intent(this@MainActivity, InfoActivity::class.java)
            startActivity(intent)
        }

        val toggleButtonClick = findViewById<Button>(R.id.toggleButton)
        toggleButtonClick.setOnClickListener {
            isRunning = !isRunning
            runningDisplayTextView.text = if (isRunning) "Enabled" else "Disabled"
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
        CoroutineScope(Dispatchers.Main).launch {
            sendToDatabase()
        }
        // registracija senzorjev
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
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

                        //Log.d("MainActivity", "Latitude: $latitude, Longitude: $longitude")
                    }
                }
            } else {
                // Request permission to access the location
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
            startRunnable() // Schedule the code to run again after 5 seconds
        }, 1000)
    }

    override fun onPause() {
        super.onPause()

        // odregistracija senzorjev
        mSensorManager.unregisterListener(this)
    }

    private var lastUpdateTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 1000) {
                // x y z TextView se posodobi
                accXTextView.text = "X: ${event.values[0]}"
                accYTextView.text = "Y: ${event.values[1]}"
                accZTextView.text = "Z: ${event.values[2]}"
            }

            if (counter < dataArraySize) {
                globalAccX[counter] = event.values[0].toDouble()
                globalAccY[counter] = event.values[1].toDouble()
                globalAccZ[counter] = event.values[2].toDouble()
            }

            counter++

            if (counter >= dataArraySize && wasDataSent) {
                // Reset the counter and arrays
                globalAccX = DoubleArray(dataArraySize)
                globalAccY = DoubleArray(dataArraySize)
                globalAccZ = DoubleArray(dataArraySize)
                counter = 0
                wasDataSent = false
            }

        }
    }

    interface ApiService {
        @POST("roadState")
        suspend fun sendData(@Body data: RequestBody): Response<Unit>
    }

    private suspend fun sendDataToServer(data: JSONObject) {
        Log.d("MainActivity", "Sending data: $data")
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3001/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // <-- Increase timeout value
                    .build()
            )
            .build()

        val service = retrofit.create(ApiService::class.java)

        val requestBody = data.toString().toRequestBody("application/json".toMediaTypeOrNull())

        var response: Response<Unit>? = null
        var retries = 0
        while (response == null && retries < 3) {
            try {
                response = service.sendData(requestBody)
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

    private suspend fun sendToDatabase() {
        val userId = intent.getStringExtra("USER_ID")
        while (true) {
            if (isRunning){
                // Create a new JSONObject with the sensor data
                val dataToSend = JSONObject()
                dataToSend.put("accX", JSONArray(globalAccX))
                dataToSend.put("accY", JSONArray(globalAccY))
                dataToSend.put("accZ", JSONArray(globalAccZ))
                dataToSend.put("longitude", globalLongitude)
                dataToSend.put("latitude", globalLatitude)
                dataToSend.put("ownerId", userId) // Set the ownerId to null for now

                Log.d("MainActivity", "Data to send: $dataToSend")

                sendDataToServer(dataToSend)
            }
            delay(1000) // Wait 5 seconds before sending the next data point
        }
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
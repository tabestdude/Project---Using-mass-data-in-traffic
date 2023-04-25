package feri.aplikacijaprojekt

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import feri.aplikacijaprojekt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    // za giroskop
    private lateinit var sensorManager: SensorManager
    private lateinit var giroXTextView: TextView
    private lateinit var giroYTextView: TextView
    private lateinit var giroZTextView: TextView

    // za accelorometer
    //private lateinit var locationManager: LocationManager
    //private lateinit var speedometerValueTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // referenca na sensorManager na napravi
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // referenca na locationManager na napravi
        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // findView za elemente
        giroXTextView = findViewById<TextView>(R.id.giroX)
        giroYTextView = findViewById<TextView>(R.id.giroY)
        giroZTextView = findViewById<TextView>(R.id.giroZ)

        val infoButtonClick = findViewById<Button>(R.id.infoButton)
        infoButtonClick.setOnClickListener{
            val intent = Intent(this@MainActivity, InfoActivity::class.java)
            startActivity(intent)
        }

        val mapButtonClick = findViewById<Button>(R.id.mapButton)
        mapButtonClick.setOnClickListener{
            val intent2 = Intent(this@MainActivity, MapActivity::class.java)
            startActivity(intent2)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onResume() {
        super.onResume()

        // registracija girsokop listenerja
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()

        // odregistracija giroskop listenerja
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // preverjanje če je event od giroskopa
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            // x y z TextView se posodobi
            giroXTextView.text = "X: ${event.values[0]}"
            giroYTextView.text = "Y: ${event.values[1]}"
            giroZTextView.text = "Z: ${event.values[2]}"
        }
    }

}

/*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ali je GPS vklopljen
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // zahtevanje posodobitev lokacije
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        } else {
            // če GPS ni vklopljen
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
        }

        // referenca na speedometerValue textview
        speedometerValueTextView = findViewById<TextView>(R.id.speedometerValue)
    }

    override fun onLocationChanged(location: Location) {
        // hitrost v km
        val speed = location.speed * 3.6
        // posodabljanje textview-a v aplikaciji
        speedometerValueTextView.text = speed.toString()
    }

 */
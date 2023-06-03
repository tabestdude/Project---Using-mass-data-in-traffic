package feri.aplikacijaprojekt

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

var globalUsername = ""
var globalPassword = ""

class LoginActivity : AppCompatActivity() {

    private lateinit var etUserName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUserName = findViewById(R.id.et_user_name)
        etPassword = findViewById(R.id.et_password)
        btnSubmit = findViewById(R.id.btn_submit)

        btnSubmit.setOnClickListener {

            // Set the globalUsername and globalPassword from the EditTexts
            globalUsername = etUserName.text.toString()
            globalPassword = etPassword.text.toString()

            val dataToSend = JSONObject()
            dataToSend.put("username", globalUsername)
            dataToSend.put("password", globalPassword)

            // Add the sensor data to the JSONObject
            CoroutineScope(Dispatchers.Main).launch {
                sendDataToServer(dataToSend)
            }
        }
    }

    private interface ApiService {
        @POST("users/login/phone")
        suspend fun sendData(@Body data: RequestBody): Response<ResponseBody>
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

        var response: Response<ResponseBody>? = null
        var retries = 0
        while (response == null && retries < 3) {
            try {
                response = service.sendData(requestBody)
            } catch (e: SocketTimeoutException) {
                retries++
                Log.e(ContentValues.TAG, "Socket timeout exception, retrying...")
            }
        }
        Log.d("MainActivity", "Response: $response")
        if (response != null && response.isSuccessful) {
            Log.d(ContentValues.TAG, "Data sent successfully")
            Log.d(ContentValues.TAG, "Response: ${response.body()}")

            // Check if authentication was successful
            val responseBody = response.body()?.string()
            if (responseBody != null) {
                val responseJson = JSONObject(responseBody)
                val userId = responseJson.optString("_id");
                if (!userId.equals("")) {
                    // Launch the next activity
                    val photoIntent = Intent(this@LoginActivity, PhotoActivity::class.java)
                    photoIntent.putExtra("USER_ID", userId)
                    startActivity(photoIntent)
                } else {
                    // Show a popup with an error message
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Napačno uporabniško ime ali geslo!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e(ContentValues.TAG, "Failed to get response body")
            }
        } else {
            Log.e(ContentValues.TAG, "Failed to send data or no response from server")
        }
    }
}

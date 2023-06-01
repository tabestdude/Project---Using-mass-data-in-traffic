package feri.aplikacijaprojekt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color.convert
import android.location.Location.convert
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.internal.utils.ImageUtil
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import feri.aplikacijaprojekt.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PhotoActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnContinue: Button

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        Toast.makeText(this, "Prosim zajemi sliko za nadaljevanje.", Toast.LENGTH_SHORT).show()

        viewFinder = findViewById(R.id.previewView)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnContinue = findViewById(R.id.btnContinue)

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set click listener for take photo button
        btnTakePhoto.setOnClickListener { takePhoto() }

        // Set click listener for continue button
        btnContinue.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            val userId = intent.getStringExtra("USER_ID");
            mainIntent.putExtra("USER_ID", userId)
            startActivity(mainIntent)
        }

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted
            startCamera()
        } else {
            // Permission has not yet been granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Unable to start camera", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        cameraExecutor.shutdown()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

                    // Read the image data from the file
                    val inputStream = FileInputStream(photoFile)
                    val buffer = ByteArray(photoFile.length().toInt())
                    inputStream.read(buffer)
                    inputStream.close()

                    // Encode the image data to base64
                    val base64String = Base64.encodeToString(buffer, Base64.DEFAULT)

                    // Send the image data to the server
                    GlobalScope.launch {
                        sendDataToServer(base64String)
                    }

                    viewFinder.visibility = View.GONE
                    btnTakePhoto.visibility = View.GONE
                    btnContinue.visibility = View.VISIBLE
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "PhotoActivity"
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    private interface ApiService {
        @POST("api/data/image/")
        @Headers("Content-Type: application/json") // Add this line to set the Content-Type header
        suspend fun sendData(@Body requestBody: Map<String, String>): Response<ResponseBody>
    }

    private suspend fun sendDataToServer(base64String: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1:3001/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .build()

        val service = retrofit.create(ApiService::class.java)

        val requestBody = mapOf("image" to base64String) // Create a JSON object with the 'image' field

        var response: Response<ResponseBody>? = null
        var retries = 0
        while (response == null && retries < 3) {
            try {
                response = service.sendData(requestBody) // Pass the requestBody instead of base64String
            } catch (e: SocketTimeoutException) {
                retries++
                Log.e(TAG, "Socket timeout exception, retrying...")
            }
        }

        if (response != null && response.isSuccessful) {
            Log.d(TAG, "Image sent successfully")
        } else {
            Log.e(TAG, "Failed to send image or no response from server")
        }
    }



    object ImageUtil {

        @Throws(IllegalArgumentException::class)
        fun convert(base64Str: String): Bitmap {
            val decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",") + 1),
                Base64.DEFAULT
            )
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }

        fun convert(bitmap: Bitmap): String {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }

    }

}

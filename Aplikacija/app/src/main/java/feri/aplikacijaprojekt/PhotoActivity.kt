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
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.internal.utils.ImageUtil
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import feri.aplikacijaprojekt.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PhotoActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnTakePhoto: Button
    private lateinit var retryTextView: TextView

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        Toast.makeText(this, "Prosim zajemi sliko za nadaljevanje.", Toast.LENGTH_SHORT).show()

        viewFinder = findViewById(R.id.previewView)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set click listener for take photo button
        btnTakePhoto.setOnClickListener { takePhoto() }

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted
            takePhoto()
        } else {
            // Permission has not yet been granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun goToMainActivity(){
        val mainIntent = Intent(this, MainActivity::class.java)
        val userId = intent.getStringExtra("USER_ID");
        mainIntent.putExtra("USER_ID", userId)
        startActivity(mainIntent)
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

                    val bytes = photoFile.readBytes()
                    val base64String = Base64.encodeToString(bytes, Base64.DEFAULT).replace("\n", "")

                    // Send the image data to the server
                    GlobalScope.launch {
                        try {
                            Log.d("PhotoActivity", "result from sending data: before getting result")
                            val result = sendDataToPythonServer(base64String)
                            Log.d("PhotoActivity", "result from sending data: $result")
                            withContext(Dispatchers.Main) {
                                handleServerResult(result)
                            }

                            if (photoFile.exists()) {
                                val deleted = photoFile.delete()
                                if (deleted)
                                    Log.d("PhotoActivity", "File was deleted successfully")
                                else
                                    Log.d("PhotoActivity", "Failed to delete the file")
                            }
                        } catch (e: Exception){
                            Log.e("PhotoActivity", e.stackTraceToString())
                        }

                    }
                    btnTakePhoto.visibility = View.VISIBLE
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    if (photoFile.exists()) {
                        val deleted = photoFile.delete()
                        if (deleted)
                            Log.d("PhotoActivity", "File was deleted successfully")
                        else
                            Log.d("PhotoActivity", "Failed to delete the file")
                    }
                }
            })
    }

    private suspend fun sendDataToPythonServer(base64String: String): String {

        val json = JSONObject()
        json.put("image", base64String)
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())


        val request: Request = Request.Builder()
            .url("http://192.168.137.1:5000/predict")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        return response.body?.string() ?: ""
    }

    private fun printToastForNewImage(){
        val msg = "Please try again"
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun handleServerResult(result: String) {
        val username = intent.getStringExtra("USERNAME")
        Log.d("PhotoActivity", "Before if statements")
        if (result.contains("Luka")) {
            if (username.equals("luka"))
                goToMainActivity()
            else
                printToastForNewImage()
        } else if (result.contains("Ales")) {
            if (username.equals("ales"))
                goToMainActivity()
            else
                printToastForNewImage()
        } else {
            printToastForNewImage()
        }
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

}

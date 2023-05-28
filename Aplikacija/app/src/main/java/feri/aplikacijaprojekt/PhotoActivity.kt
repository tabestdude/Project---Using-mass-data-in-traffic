package feri.aplikacijaprojekt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import feri.aplikacijaprojekt.MainActivity
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnContinue: Button

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        viewFinder = findViewById(R.id.previewView)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnContinue = findViewById(R.id.btnContinue)

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set click listener for take photo button
        btnTakePhoto.setOnClickListener { takePhoto() }

        // Set click listener for continue button
        btnContinue.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted
            //startCamera()
        } else {
            // Permission has not yet been granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onResume() {
        super.onResume()
        //startCamera()
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    private fun startCamera() {
        // Create a preview use case
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()

        // Create an image capture use case
        imageCapture = ImageCapture.Builder()
            .setTargetResolution(Size(480, 640))
            .build()

        // Select the front-facing camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        // Bind the camera use cases to the viewfinder
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            try {
                // Unbind any previous use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the new use cases to the camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

                // Connect the preview to the viewfinder
                preview.setSurfaceProvider(viewFinder.surfaceProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera use cases", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        cameraExecutor.shutdown()
    }

    private fun takePhoto() {
        // Create a file to store the photo
        val photoFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

        // Create an image capture listener
        val imageCaptureListener = object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Save the captured image to the file
                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

                // Hide the camera preview and show the continue button
                viewFinder.visibility = View.GONE
                btnTakePhoto.visibility = View.GONE
                btnContinue.visibility = View.VISIBLE
            }

            override fun onError(exception: ImageCaptureException) {
                // Handle capture failure
                Log.e(TAG, "Capture failed: ${exception.message}", exception)
            }
        }

        // Configure the image capture use case to save the photo to the file
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture?.takePicture(outputOptions, cameraExecutor, imageCaptureListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted, start the camera
                startCamera()
            } else {
                // Permission has been denied, show an error message
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

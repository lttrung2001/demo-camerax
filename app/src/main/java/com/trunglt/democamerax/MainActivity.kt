package com.trunglt.democamerax

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.trunglt.democamerax.databinding.ActivityMainBinding
import kotlin.math.atan2
import kotlin.math.ceil


private const val CAMERA_PERMISSION_REQUEST_CODE = 1

@ExperimentalGetImage
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cameraManager by lazy {
        CameraManager(this, binding.previewView) {
            processImageProxy(it)
        }
    }
    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC
        ).build()
        BarcodeScanning.getClient(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (hasCameraPermission()) cameraManager.startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        else requestPermission()
    }

    // checking to see whether user has already granted permission
    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        // opening up dialog to ask for camera permission
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraManager.startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        } else {
            Toast.makeText(
                this, "Camera permission required", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(
                image, imageProxy.imageInfo.rotationDegrees
            )
            barcodeScanner.process(inputImage).addOnSuccessListener { barcodeList ->
                barcodeList.getOrNull(0)?.let { barcode ->
//                    cameraManager.stop()
                    val sx = binding.drawingView.width.toFloat() / inputImage.height
                    val sy = binding.drawingView.height.toFloat() / inputImage.width
                    val scale = sx.coerceAtLeast(sy)
                    val offsetX =
                        (binding.drawingView.width.toFloat() - ceil(image.cropRect.height() * scale)) / 2.0f
                    val offsetY =
                        (binding.drawingView.height.toFloat() - ceil(image.cropRect.width() * scale)) / 2.0f
                    barcode.boundingBox?.let {
                        it.left = (it.left * scale + offsetX).toInt()
                        it.top = (it.top * scale + offsetY).toInt()
                        it.right = (it.right * scale + offsetX).toInt()
                        it.bottom = (it.bottom * scale + offsetY).toInt()
                        binding.drawingView.barcode = barcode
                        binding.drawingView.animateToDetectedRectF(it)
                    }
                }
            }.addOnFailureListener {

            }.addOnCompleteListener {
                imageProxy.image?.close()
                imageProxy.close()
            }
        }
    }

    private fun isInScanArea(bounds: Rect?): Boolean {
        // Lấy kích thước của preview view
        val previewViewWidth = binding.previewView.width
        val previewViewHeight = binding.previewView.height

        // Lấy kích thước của bounding box
        val bboxLeft = bounds?.left ?: 0
        val bboxTop = bounds?.top ?: 0
        val bboxRight = bounds?.right ?: 0
        val bboxBottom = bounds?.bottom ?: 0

        // Kiểm tra xem bounding box có nằm hoàn toàn bên trong preview view không
        // Nếu cả các cạnh của bounding box đều nằm trong preview view, chúng ta xem nó là nằm trong preview view
        return bboxLeft >= 0 && bboxTop >= 0 && bboxRight <= previewViewWidth && bboxBottom <= previewViewHeight
    }
}
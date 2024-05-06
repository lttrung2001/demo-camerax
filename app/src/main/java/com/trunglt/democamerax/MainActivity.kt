package com.trunglt.democamerax

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
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
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.trunglt.democamerax.databinding.ActivityMainBinding
import kotlin.math.ceil


private const val CAMERA_PERMISSION_REQUEST_CODE = 1

@ExperimentalGetImage
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cameraManager by lazy {
        CameraManager(this, binding.previewView) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastDetectedTime > 1000) {
                lastDetectedTime = currentTime
                processImageProxy(it)
            } else {
                it.close()
            }
        }
    }
    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC
        ).build()
        BarcodeScanning.getClient(options)
    }
    private val realTimeOpts =
        FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE).build()
    private var lastDetectedTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnTakePicture.setOnClickListener {
            cameraManager.swap()
        }

        if (hasCameraPermission()) cameraManager.startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        else requestPermission()
        binding.transparentView.onAnimationFocusDone = {
            binding.previewView.bitmap?.let { srcBitmap ->
                val dstBitmap = Bitmap.createBitmap(
                    srcBitmap,
                    it.left.toInt(),
                    it.top.toInt(),
                    it.width().toInt(),
                    it.height().toInt()
                )
                binding.transparentView.startZoomAnimation(dstBitmap)
            }
        }
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
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraManager.startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        } else {
            Toast.makeText(
                this, "Camera permission required", Toast.LENGTH_LONG
            ).show()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(
                image, imageProxy.imageInfo.rotationDegrees
            )
            barcodeScanner.process(inputImage).addOnSuccessListener { barcodeList ->
                if (barcodeList.isNullOrEmpty()) {
//                    binding.transparentView.setCorners(binding.transparentView.getDefaultBoxCorners())
                    return@addOnSuccessListener
                }
                barcodeList[0]?.let {
                    cameraManager.stopCamera()
                    if (isInScanArea(it.boundingBox)) {
                        Toast.makeText(this, it.rawValue, Toast.LENGTH_LONG).show()
                    }
                    val sx = binding.transparentView.width.toFloat() / inputImage.height
                    val sy = binding.transparentView.height.toFloat() / inputImage.width
                    val scale = sx.coerceAtLeast(sy)
                    val offsetX =
                        (binding.transparentView.width.toFloat() - ceil(image.cropRect.height() * scale)) / 2.0f
                    val offsetY =
                        (binding.transparentView.height.toFloat() - ceil(image.cropRect.width() * scale)) / 2.0f
                    it.cornerPoints?.toList()?.let { crns ->
                        binding.transparentView.setCorners(crns.map { p ->
                            DrawingView.AnimatablePoint(
                                (p.x * scale + offsetX).toInt(),
                                (p.y * scale + offsetY).toInt()
                            )
                        })
                    }
                }
            }.addOnFailureListener {
                // This failure will happen if the barcode scanning model
                // fails to download from Google Play Services
                Log.wtf("TRUNGLE", it.message.orEmpty())
            }.addOnCompleteListener {
                // When the image is from CameraX analysis use case, must
                // call image.close() on received images when finished
                // using them. Otherwise, new images may not be received
                // or the camera may stall.

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

    companion object {
        val TAG: String = "MainActivity"
    }
}
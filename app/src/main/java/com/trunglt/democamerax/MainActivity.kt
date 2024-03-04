package com.trunglt.democamerax

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toRect
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.trunglt.democamerax.databinding.ActivityMainBinding
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
    private val realTimeOpts =
        FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE).build()
    private val faceDetector = FaceDetection.getClient(realTimeOpts)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnTakePicture.setOnClickListener {
            cameraManager.swap()
        }

        if (hasCameraPermission()) cameraManager.startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
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
//            val inputImage =
//                InputImage.fromMediaImage(
//                    image,
//                    imageProxy.imageInfo.rotationDegrees
//                )
//            barcodeScanner.process(inputImage)
//                .addOnSuccessListener { barcodeList ->
//                    val barcode = barcodeList.getOrNull(0)
//                    Log.wtf("TRUNGLE", Gson().toJson(barcode))
//                    // `rawValue` is the decoded value of the barcode
//                    barcode?.rawValue?.let { value ->
//                        if (isInScanArea(barcode.boundingBox)) {
//                            Toast.makeText(this, value, Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//                .addOnFailureListener {
//                    // This failure will happen if the barcode scanning model
//                    // fails to download from Google Play Services
//                    Log.wtf("TRUNGLE", it.message.orEmpty())
//                }.addOnCompleteListener {
//                    // When the image is from CameraX analysis use case, must
//                    // call image.close() on received images when finished
//                    // using them. Otherwise, new images may not be received
//                    // or the camera may stall.
//
//                    imageProxy.image?.close()
//                    imageProxy.close()
//                }
            val inputImage = InputImage.fromMediaImage(
                image, imageProxy.imageInfo.rotationDegrees
            )
            faceDetector.process(inputImage).addOnSuccessListener { faces ->
                binding.transparentView.setFaceRectList(faces.map { face ->
                    val boundingBoxT = face.boundingBox
                    val sx = binding.transparentView.width.toFloat() / inputImage.height
                    val sy = binding.transparentView.height.toFloat() / inputImage.width
                    val scale = sx.coerceAtLeast(sy)
                    val offsetX =
                        (binding.transparentView.width.toFloat() - ceil(image.cropRect.height() * scale)) / 2.0f
                    val offsetY =
                        (binding.transparentView.height.toFloat() - ceil(image.cropRect.width() * scale)) / 2.0f
                    RectF().apply {
                        left = boundingBoxT.right * scale + offsetX
                        top = boundingBoxT.top * scale + offsetY
                        right = boundingBoxT.left * scale + offsetX
                        bottom = boundingBoxT.bottom * scale + offsetY
                        if (cameraManager.isFrontCameraInUse()) { // Front camera
                            val centerX = binding.transparentView.width.toFloat() / 2
                            left = centerX + (centerX - left)
                            right = centerX - (right - centerX)
                        }
                    }.toRect()
                })
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

    companion object {
        val TAG: String = "MainActivity"
    }
}
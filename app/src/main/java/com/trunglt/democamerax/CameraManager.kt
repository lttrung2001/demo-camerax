package com.trunglt.democamerax

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraManager(
    private var activity: AppCompatActivity?,
    private var previewView: PreviewView?,
    private var processImageProxy: (imageProxy: ImageProxy) -> Unit
): DefaultLifecycleObserver {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private val previewUseCase by lazy {
        Preview.Builder().build().apply {
            setSurfaceProvider(previewView?.surfaceProvider)
        }
    }
    private val analysisUseCase by lazy {
        ImageAnalysis.Builder().build().apply {
            setAnalyzer(
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->
                processImageProxy.invoke(imageProxy)
            }
        }
    }

    init {
        activity?.lifecycle?.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        camera = null
        cameraProvider?.unbindAll()
        previewView = null
        activity = null
    }

    fun startCamera(cameraSelector: CameraSelector) {
        this.cameraSelector = cameraSelector
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity!!)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            try {
                camera = cameraProvider?.bindToLifecycle(
                    activity!!, cameraSelector, previewUseCase, analysisUseCase
                )
            } catch (illegalStateException: IllegalStateException) {
                // If the use case has already been bound to another lifecycle or method is not called on main thread.
            } catch (illegalArgumentException: IllegalArgumentException) {
                // If the provided camera selector is unable to resolve a camera to be used for the given use cases.
            }
        }, ContextCompat.getMainExecutor(activity!!))
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    fun swap() {
        camera = null
        cameraProvider?.unbindAll()
        cameraSelector = getOppositeCamera()
        startCamera(cameraSelector)
    }

    fun isFrontCameraInUse() = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

    private fun getOppositeCamera(): CameraSelector {
        return if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
            CameraSelector.DEFAULT_BACK_CAMERA
        else
            CameraSelector.DEFAULT_FRONT_CAMERA
    }
}
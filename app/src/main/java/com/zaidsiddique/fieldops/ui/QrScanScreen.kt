@file:androidx.annotation.OptIn(markerClass = [androidx.camera.core.ExperimentalGetImage::class])

package com.zaidsiddique.fieldops.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QrScanScreen(
    onScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        Text("Camera permission needed")
        return
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->

            val previewView = PreviewView(ctx)

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(
                            previewView.surfaceProvider
                        )
                    }

                val scanner = BarcodeScanning.getClient()

                val analysis = ImageAnalysis.Builder()
                    .build()
                    .also { analysisUseCase ->

                        analysisUseCase.setAnalyzer(
                            Executors.newSingleThreadExecutor()
                        ) { imageProxy ->

                            val mediaImage = imageProxy.image

                            if (mediaImage != null) {

                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->

                                        barcodes
                                            .firstOrNull()
                                            ?.rawValue
                                            ?.let { value ->
                                                onScanned(value)
                                            }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }

                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
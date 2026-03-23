package com.segnities007.setting.presentation.vault

import android.util.Size
import androidx.annotation.OptIn as AndroidXOptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal fun VaultQrScanner(
    onQrDecoded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val delivered = remember { AtomicBoolean(false) }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = future.get()
            bindScannerUseCases(
                cameraProvider = cameraProvider,
                previewView = previewView,
                lifecycleOwner = lifecycleOwner,
                executor = executor,
                scanner = scanner,
                delivered = delivered,
                onQrDecoded = onQrDecoded
            )
        }
        future.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching {
                if (future.isDone) future.get().unbindAll()
            }
            executor.shutdown()
            scanner.close()
            delivered.set(false)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

private fun bindScannerUseCases(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    executor: java.util.concurrent.Executor,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    delivered: AtomicBoolean,
    onQrDecoded: (String) -> Unit
) {
    val preview = Preview.Builder().build().also {
        it.surfaceProvider = previewView.surfaceProvider
    }
    val analysisResolution = ResolutionSelector.Builder()
        .setResolutionStrategy(
            ResolutionStrategy(
                Size(1280, 720),
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
            )
        )
        .build()
    val analysis = ImageAnalysis.Builder()
        .setResolutionSelector(analysisResolution)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
    analysis.setAnalyzer(executor) { imageProxy ->
        analyzeImageForQr(
            imageProxy = imageProxy,
            delivered = delivered,
            scanner = scanner,
            previewView = previewView,
            onQrDecoded = onQrDecoded
        )
    }
    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
    } catch (_: Exception) {
        cameraProvider.unbindAll()
    }
}

@AndroidXOptIn(markerClass = [ExperimentalGetImage::class])
private fun analyzeImageForQr(
    imageProxy: ImageProxy,
    delivered: AtomicBoolean,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    previewView: PreviewView,
    onQrDecoded: (String) -> Unit
) {
    if (delivered.get()) {
        imageProxy.close()
        return
    }
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val raw = barcodes.firstOrNull { it.rawValue != null }?.rawValue ?: return@addOnSuccessListener
            if (delivered.compareAndSet(false, true)) {
                previewView.post { onQrDecoded(raw) }
            }
        }
        .addOnCompleteListener { imageProxy.close() }
}

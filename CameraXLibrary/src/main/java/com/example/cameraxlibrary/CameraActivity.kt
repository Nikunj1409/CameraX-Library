package com.example.cameraxlibrary


import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraxlibrary.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class CameraActivity : AppCompatActivity() {

    private val mainBinding: ActivityCameraBinding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }


    private val multiplePermissionId = 14
    private var second = 0
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    } else {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null


    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    private var orientationEventListener: OrientationEventListener? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var aspectRatio = AspectRatio.RATIO_16_9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        if (checkMultiplePermission()) {
            startCamera()
        }

        mainBinding.flipCameraIB.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUserCases()
        }
        mainBinding.aspectRatioTxt.setOnClickListener {
            if (aspectRatio == AspectRatio.RATIO_16_9) {
                aspectRatio = AspectRatio.RATIO_4_3
                setAspectRatio("H,4:3")
                mainBinding.aspectRatioTxt.text = "4:3"
            } else {
                aspectRatio = AspectRatio.RATIO_16_9
                setAspectRatio("H,0:0")
                mainBinding.aspectRatioTxt.text = "16:9"
            }
            bindCameraUserCases()
        }

        val getValue = intent.getStringExtra("value")
        second = intent.getIntExtra("second", 0)

        when (getValue) {
            "camera" -> {
//                mainBinding.changeCamera.visible()
                mainBinding.captureImage.visible()
//                mainBinding.changeVideo.gone()
                mainBinding.captureVideo.gone()
            }

            "video" -> {
//                mainBinding.changeCamera.gone()
                mainBinding.captureImage.gone()
//                mainBinding.changeVideo.visible()
                mainBinding.captureVideo.visible()
            }

            else -> {
//                mainBinding.changeCamera.visible()
                mainBinding.captureImage.visible()
//                mainBinding.changeVideo.gone()
                mainBinding.captureVideo.gone()
            }
        }
        mainBinding.captureImage.setOnClickListener {
            takePhoto()
        }
        mainBinding.captureVideo.setOnClickListener {
            captureVideo()
        }
        mainBinding.captureIB2.setOnClickListener {
            stopRecording()
        }
        mainBinding.flashToggleIB.setOnClickListener {
            setFlashIcon(camera)
        }
    }


    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    startCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(this))
    }


    private fun bindCameraUserCases() {
        val rotation = mainBinding.previewView.display.rotation

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(mainBinding.previewView.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HIGHEST,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
            )
            .setAspectRatio(aspectRatio)
            .build()

        videoCapture = VideoCapture.withOutput(recorder).apply {
            targetRotation = rotation
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val myRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture.targetRotation = myRotation
                videoCapture?.targetRotation = myRotation
            }
        }
        orientationEventListener?.enable()

        try {
            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, videoCapture
            )
            if (second ?: 0 > 0) {
                captureVideo()
            }
            setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpZoomTapToFocus() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this, listener)

        mainBinding.previewView.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = mainBinding.previewView.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()

                val x = event.x
                val y = event.y

                val focusCircle = RectF(x - 50, y - 50, x + 50, y + 50)

                mainBinding.focusCircleView.focusCircle = focusCircle
                mainBinding.focusCircleView.invalidate()

                camera.cameraControl.startFocusAndMetering(action)

                view.performClick()
            }
            true
        }
    }

    private fun setFlashIcon(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                mainBinding.flashToggleIB.setImageResource(R.drawable.flash_off)
            } else {
                camera.cameraControl.enableTorch(false)
                mainBinding.flashToggleIB.setImageResource(R.drawable.flash_on)
            }
        } else {
            Toast.makeText(
                this,
                "Flash is Not Available",
                Toast.LENGTH_LONG
            ).show()
            mainBinding.flashToggleIB.isEnabled = false
        }
    }

    private fun takePhoto() {

        val imageFolder = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), "Images"
        )
        if (!imageFolder.exists()) {

            imageFolder.mkdir()
        }

        val fileName = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Images")
            }
        }

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = (lensFacing == CameraSelector.LENS_FACING_FRONT)
        }
        val outputOption =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                OutputFileOptions.Builder(
                    contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).setMetadata(metadata).build()
            } else {
                val imageFile = File(imageFolder, fileName)
                OutputFileOptions.Builder(imageFile)
                    .setMetadata(metadata).build()
            }

        imageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val arraylist = arrayListOf<String>()
                    arraylist.add(outputFileResults.savedUri.toString() ?: "")
                    val i = Intent("value")
                    i.putStringArrayListExtra("list", arraylist)
                    sendBroadcast(i)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        exception.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        )
    }

    private fun setAspectRatio(ratio: String) {
        mainBinding.previewView.layoutParams = mainBinding.previewView.layoutParams.apply {
            if (this is ConstraintLayout.LayoutParams) {
                dimensionRatio = ratio
            }
        }
    }

    private fun captureVideo() {
        mainBinding.flashToggleIB.gone()
        mainBinding.flipCameraIB.gone()
        mainBinding.aspectRatioTxt.gone()
//        mainBinding.changeCamera.gone()

        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            stopRecording()
            recording = null
            return
        }
        val fileName = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            } else {
                val storageDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val filePath = File(storageDir, fileName).absolutePath
                put(MediaStore.Video.Media.DATA, filePath)
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture?.output
            ?.prepareRecording(this, mediaStoreOutputOptions)
            ?.apply {
                if (ActivityCompat.checkSelfPermission(
                        this@CameraActivity,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }?.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        startRecording()
                        mainBinding.captureVideo.gone()
                        mainBinding.captureIB2.visible()
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val arraylist = arrayListOf<String>()
                            arraylist.add(recordEvent.outputResults.outputUri.toString() ?: "")
                            val i = Intent("value")
                            i.putStringArrayListExtra("list", arraylist)
                            sendBroadcast(i)
                            finish()
                        } else {
                            recording?.close()
                            recording = null
                            Log.d("error", recordEvent.error.toString())
                        }
                    }
                }
            }

    }


    override fun onResume() {
        super.onResume()
        orientationEventListener?.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
        if (recording != null) {
            recording?.stop()
        }
    }

    override fun onBackPressed() {
        orientationEventListener?.disable()
        if (recording != null) {
            recording?.stop()
        }
        super.onBackPressed()
    }

    private fun timeConversion(totalSeconds: Int): String {
        val MINUTES_IN_AN_HOUR = 60
        val SECONDS_IN_A_MINUTE = 60
        val seconds = totalSeconds % SECONDS_IN_A_MINUTE
        val totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE
        val minutes = totalMinutes % MINUTES_IN_AN_HOUR
        val hours = totalMinutes / MINUTES_IN_AN_HOUR
        if (minutes < 10) {
            return "0$minutes:$seconds"
        } else {
            return "$minutes:$seconds"
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimer = object : Runnable {
        override fun run() {
            val currentTime = SystemClock.elapsedRealtime() - mainBinding.recodingTimerC.base
            val timeString = currentTime.toFormattedTime()
            mainBinding.recodingTimerC.text = timeString
            if (second > 0 && timeString == timeConversion(second)) {
                stopRecording()
                return
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun Long.toFormattedTime(): String {
        val seconds = ((this / 1000) % 60).toInt()
        val minutes = ((this / (1000 * 60)) % 60).toInt()
        val hours = ((this / (1000 * 60 * 60)) % 24).toInt()

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun startRecording() {
        mainBinding.recodingTimerC.visible()
        mainBinding.recodingTimerC.base = SystemClock.elapsedRealtime()
        mainBinding.recodingTimerC.start()
        handler.post(updateTimer)
    }

    private fun stopRecording() {
        mainBinding.captureVideo.visible()
        mainBinding.captureIB2.gone()
        mainBinding.flashToggleIB.visible()
        mainBinding.flipCameraIB.visible()
        mainBinding.aspectRatioTxt.visible()
//        mainBinding.changeVideo.visible()
        recording?.stop()
        recording = null
        mainBinding.recodingTimerC.gone()
        mainBinding.recodingTimerC.stop()
        handler.removeCallbacks(updateTimer)
    }

}
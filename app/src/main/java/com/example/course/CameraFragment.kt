package com.example.course

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.course.databinding.FragmentCameraBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var isRecording = false
    private var isPhotoMode = true
    private var recordingStartTime: Long = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateRecordingTimer()
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupUI()
        startCamera()
    }

    private fun setupUI() {
        binding.apply {
            captureButton.setOnClickListener { takePhoto() }
            switchCamera.setOnClickListener { switchCamera() }
            galleryButton.setOnClickListener {
                findNavController().navigate(R.id.action_camera_to_gallery)
            }
            recordButton.setOnClickListener { toggleRecording() }
            cameraModeToggle.setOnClickListener { toggleCameraMode() }
        }
    }

    private fun toggleCameraMode() {
        isPhotoMode = !isPhotoMode
        binding.apply {
            cameraModeToggle.text = if (isPhotoMode) "Режим: Фото" else "Режим: Видео"
            captureButton.visibility = if (isPhotoMode) View.VISIBLE else View.GONE
            recordButton.visibility = if (isPhotoMode) View.GONE else View.VISIBLE
        }
    }

    private fun updateRecordingTimer() {
        if (isRecording) {
            val duration = System.currentTimeMillis() - recordingStartTime
            val seconds = (duration / 1000) % 60
            val minutes = (duration / (1000 * 60)) % 60
            val hours = (duration / (1000 * 60 * 60)) % 24

            binding.recordingTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Запускаем анимацию вспышки
        binding.flashAnimation.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(100)
                .withEndAction {
                    animate()
                        .alpha(0f)
                        .setDuration(100)
                        .withEndAction {
                            visibility = View.GONE
                        }
                }
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(requireContext(), "Photo captured!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Добавляем фото в галерею
                    MediaScannerConnection.scanFile(
                        requireContext(),
                        arrayOf(output.savedUri.toString()),
                        null
                    ) { _, uri ->
                        // Можно здесь обработать, если нужно
                        Log.d(TAG, "Scanned file: $uri")
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exc.message}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, msg, exc)
                }
            }
        )
    }

    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        binding.recordingTimer.visibility = View.VISIBLE
        binding.recordButton.setImageResource(R.drawable.ic_stop)
        timerHandler.post(timerRunnable)

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraApp")
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        binding.recordButton.isEnabled = true
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Видео сохранено: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            val msg = "Ошибка записи видео: ${recordEvent.error}"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, msg)
                        }
                        binding.recordButton.setImageResource(R.drawable.ic_record)
                        binding.recordButton.isEnabled = true
                        binding.recordingTimer.visibility = View.GONE
                        isRecording = false
                        timerHandler.removeCallbacks(timerRunnable)
                    }
                }
            }
    }

    private fun stopRecording() {
        val recording = recording
        if (recording != null) {
            timerHandler.removeCallbacks(timerRunnable)
            binding.recordingTimer.visibility = View.GONE
            recording.stop()
            this.recording = null
        }
    }

    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun switchCamera() {
        val currentCamera = camera?.cameraInfo?.lensFacing
        val newCameraSelector = if (currentCamera == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        try {
            if (cameraProvider != null) {
                // Создаем новый preview каждый раз при переключении
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    newCameraSelector,
                    preview,  // Добавляем preview
                    imageCapture,
                    videoCapture
                )
            }
        } catch (exc: Exception) {
            Log.e(TAG, "Camera switch failed", exc)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
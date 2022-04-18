package com.example.deafultproject

import android.content.Intent
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.deafultproject.filter.DuotoneFilterNew
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filter.MultiFilter
import com.otaliastudios.cameraview.filters.DuotoneFilter
import com.otaliastudios.cameraview.filters.SaturationFilter
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener, OptionView.Callback {

    companion object {
        private val LOG = CameraLogger.create("DemoApp")
        private const val USE_FRAME_PROCESSOR: Boolean = true
        private const val DECODE_BITMAP: Boolean = false
    }


    private val camera: CameraView by lazy { findViewById(R.id.camera) }
    private val controlPanel: ViewGroup by lazy { findViewById(R.id.controls) }
    private var captureTime: Long = 0

    private var currentFilter = 0
    private val allFilters = Filters.values()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        camera.setLifecycleOwner(this)
        camera.addCameraListener(Listener())
        //camera.setGrid(Grid.DRAW_PHI);

        if (USE_FRAME_PROCESSOR) {
            camera.addFrameProcessor(object : FrameProcessor {
                private var lastTime = System.currentTimeMillis()
                override fun process(frame: Frame) {
                    val newTime = frame.time
                    val delay = newTime - lastTime
                    lastTime = newTime
                    LOG.v("Frame delayMillis:", delay, "FPS:", 1000 / delay)
                    if (DECODE_BITMAP) {
                        if (frame.format == ImageFormat.NV21
                            && frame.dataClass == ByteArray::class.java
                        ) {
                            val data = frame.getData<ByteArray>()
                            val yuvImage = YuvImage(
                                data,
                                frame.format,
                                frame.size.width,
                                frame.size.height,
                                null
                            )
                            val jpegStream = ByteArrayOutputStream()
                            yuvImage.compressToJpeg(
                                Rect(
                                    0, 0,
                                    frame.size.width,
                                    frame.size.height
                                ), 100, jpegStream
                            )
                            val jpegByteArray = jpegStream.toByteArray()
                            val bitmap = BitmapFactory.decodeByteArray(
                                jpegByteArray,
                                0, jpegByteArray.size
                            )
                            bitmap.toString()
                        }
                    }
                }
            })
        }
        findViewById<View>(R.id.edit).setOnClickListener(this)
        findViewById<View>(R.id.capturePicture).setOnClickListener(this)
        findViewById<View>(R.id.capturePictureSnapshot).setOnClickListener(this)
        findViewById<View>(R.id.captureVideo).setOnClickListener(this)
        findViewById<View>(R.id.captureVideoSnapshot).setOnClickListener(this)
        findViewById<View>(R.id.toggleCamera).setOnClickListener(this)
        findViewById/*val group = controlPanel.getChildAt(0) as ViewGroup
        val watermark = findViewById<View>(R.id.watermark)
        val options: List<Option<*>> = listOf(
            // Layout
            Option.Width(), Option.Height(),
            // Engine and preview
            Option.Mode(), Option.Engine(), Option.Preview(),
            // Some controls
            Option.Flash(), Option.WhiteBalance(), Option.Hdr(),
            Option.PictureMetering(), Option.PictureSnapshotMetering(),
            Option.PictureFormat(),
            // Video recording
            Option.PreviewFrameRate(), Option.VideoCodec(), Option.Audio(), Option.AudioCodec(),
            // Gestures
            Option.Pinch(), Option.HorizontalScroll(), Option.VerticalScroll(),
            Option.Tap(), Option.LongTap(),
            // Watermarks
            Option.OverlayInPreview(watermark),
            Option.OverlayInPictureSnapshot(watermark),
            Option.OverlayInVideoSnapshot(watermark),
            // Frame Processing
            Option.FrameProcessingFormat(),
            // Other
            Option.Grid(), Option.GridColor(), Option.UseDeviceOrientation()
        )
        val dividers = listOf(
            // Layout
            false, true,
            // Engine and preview
            false, false, true,
            // Some controls
            false, false, false, false, false, true,
            // Video recording
            false, false, false, true,
            // Gestures
            false, false, false, false, true,
            // Watermarks
            false, false, true,
            // Frame Processing
            true,
            // Other
            false, false, true
        )
        for (i in options.indices) {
            val view = OptionView<Any>(this)
            view.setOption(options[i] as Option<Any>, this)
            view.setHasDivider(dividers[i])
            group.addView(
                view,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }*/<View>(R.id.changeFilter).setOnClickListener(this)


        controlPanel.viewTreeObserver.addOnGlobalLayoutListener {
            BottomSheetBehavior.from(controlPanel).state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
            val group = controlPanel.getChildAt(0) as ViewGroup
            for (i in 0 until group.childCount) {
                val view = group.getChildAt(i) as OptionView<*>
                view.onCameraOpened(camera, options)
            }
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            message("Got CameraException #" + exception.reason, true)
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            if (camera.isTakingVideo) {
                message("Captured while taking video. Size=" + result.size, false)
                return
            }

            // This can happen if picture was taken with a gesture.
            val callbackTime = System.currentTimeMillis()
            if (captureTime == 0L) captureTime = callbackTime - 300
            LOG.w("onPictureTaken called! Launching activity. Delay:", callbackTime - captureTime)
            PicturePreviewActivity.pictureResult = result
            val intent = Intent(this@MainActivity, PicturePreviewActivity::class.java)
            intent.putExtra("delay", callbackTime - captureTime)
            startActivity(intent)
            captureTime = 0
            LOG.w("onPictureTaken called! Launched activity.")
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            LOG.w("onVideoTaken called! Launching activity.")
            VideoPreviewActivity.videoResult = result
            val intent = Intent(this@MainActivity, VideoPreviewActivity::class.java)
            startActivity(intent)
            LOG.w("onVideoTaken called! Launched activity.")
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            LOG.w("onVideoRecordingStart!")
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            message("Video taken. Processing...", false)
            LOG.w("onVideoRecordingEnd!")
        }

        override fun onExposureCorrectionChanged(
            newValue: Float,
            bounds: FloatArray,
            fingers: Array<PointF>?
        ) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers)
            message("Exposure correction:$newValue", false)
        }

        override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
            super.onZoomChanged(newValue, bounds, fingers)
            message("Zoom:$newValue", false)
        }
    }

    private fun message(content: String, important: Boolean) {
        if (important) {
            LOG.w(content)
            Toast.makeText(this, content, Toast.LENGTH_LONG).show()
        } else {
            LOG.i(content)
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(p0: View?) {

        if (p0 != null) {
            when (p0.id) {
                R.id.edit -> edit()
                R.id.capturePicture -> capturePicture()
                R.id.capturePictureSnapshot -> capturePictureSnapshot()
                R.id.captureVideo -> captureVideo()
                R.id.captureVideoSnapshot -> captureVideoSnapshot()
                R.id.toggleCamera -> toggleCamera()
                R.id.changeFilter -> changeCurrentFilter()
            }
        }
    }

    override fun onBackPressed() {
        val b = BottomSheetBehavior.from(controlPanel)
        if (b.state != BottomSheetBehavior.STATE_HIDDEN) {
            b.state = BottomSheetBehavior.STATE_HIDDEN
            return
        }
        super.onBackPressed()
    }

    private fun edit() {
        BottomSheetBehavior.from(controlPanel).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun capturePicture() {
        if (camera.mode == Mode.VIDEO) return run {
            message("Can't take HQ pictures while in VIDEO mode.", false)
        }
        if (camera.isTakingPicture) return
        captureTime = System.currentTimeMillis()
        message("Capturing picture...", false)
        camera.takePicture()
    }

    private fun capturePictureSnapshot() {
        if (camera.isTakingPicture) return
        if (camera.preview != Preview.GL_SURFACE) return run {
            message("Picture snapshots are only allowed with the GL_SURFACE preview.", true)
        }
        captureTime = System.currentTimeMillis()
        message("Capturing picture snapshot...", false)
        camera.takePictureSnapshot()
    }

    private fun captureVideo() {
        if (camera.mode == Mode.PICTURE) return run {
            message("Can't record HQ videos while in PICTURE mode.", false)
        }
        if (camera.isTakingPicture || camera.isTakingVideo) return
        message("Recording for 5 seconds...", true)
        camera.takeVideo(File(filesDir, "video.mp4"), 5000)
    }

    private fun captureVideoSnapshot() {
        if (camera.isTakingVideo) return run {
            message("Already taking video.", false)
        }
        if (camera.preview != Preview.GL_SURFACE) return run {
            message("Video snapshots are only allowed with the GL_SURFACE preview.", true)
        }
        message("Recording snapshot for 5 seconds...", true)
        camera.takeVideoSnapshot(File(filesDir, "video.mp4"), 5000)
    }

    private fun toggleCamera() {
        if (camera.isTakingPicture || camera.isTakingVideo) return
        when (camera.toggleFacing()) {
            Facing.BACK -> message("Switched to back camera!", false)
            Facing.FRONT -> message("Switched to front camera!", false)
        }
    }

    private fun changeCurrentFilter() {
        if (camera.preview != Preview.GL_SURFACE) return run {
            message("Filters are supported only when preview is Preview.GL_SURFACE.", true)
        }


        /*if (currentFilter < allFilters.size - 1) {
            currentFilter++
        } else {
            currentFilter = 0
        }
        val filter = allFilters[currentFilter]
        message(filter.toString(), false)

        // Normal behavior:
        camera.filter = filter.newInstance()*/

        //val duotone = SaturationFilter()
        /* duotone.firstColor = Color.GREEN
         duotone.secondColor = Color.RED*/
        /*try {
            val duotone = LomoishFilter()

            camera.filter = MultiFilter(duotone)
        } catch (ex: AndroidRuntimeException) {
            ex.printStackTrace()
        }*/
        val duotone = DuotoneFilterNew()
        camera.filter = MultiFilter(duotone)

        //camera.setFilter(Filters.);

        // To test MultiFilter:
        // DuotoneFilter duotone = new DuotoneFilter();
        // duotone.setFirstColor(Color.RED);
        // duotone.setSecondColor(Color.GREEN);
        // camera.setFilter(new MultiFilter(duotone, filter.newInstance()));
    }

    override fun <T : Any> onValueChanged(option: Option<T>, value: T, name: String): Boolean {
        if (option is Option.Width || option is Option.Height) {
            val preview = camera.preview
            val wrapContent = value as Int == ViewGroup.LayoutParams.WRAP_CONTENT
            if (preview == Preview.SURFACE && !wrapContent) {
                message(
                    "The SurfaceView preview does not support width or height changes. " +
                            "The view will act as WRAP_CONTENT by default.", true
                )
                return false
            }
        }
        option.set(camera, value)
        BottomSheetBehavior.from(controlPanel).state = BottomSheetBehavior.STATE_HIDDEN
        message("Changed " + option.name + " to " + name, false)
        return true
    }

}
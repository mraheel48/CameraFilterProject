package com.example.deafultproject

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.deafultproject.databinding.ActivityGpuPlusTestBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GpuPlusTest : AppCompatActivity() {

    lateinit var binding: ActivityGpuPlusTestBinding

    val workerThread: ExecutorService = Executors.newCachedThreadPool()
    val workerHandler = Handler(Looper.getMainLooper())

    var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpuPlusTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePhoto.setOnClickListener {
            takePhoto()
        }
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER", "UNUSED_ANONYMOUS_PARAMETER")
    private fun takePhoto() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            try {

                binding.myGLSurfaceView.takePicture({ bmp: Bitmap? ->

                    var bitmap = bmp

                    if (bitmap != null) {

                        workerThread.execute {

                            val s = saveMediaToStorage(bitmap!!)

                            filePath = s

                            Log.d("myTag", "${s}")

                        }

                    }
                }, null, null, 0.70f, true)

            } catch (ex: java.lang.RuntimeException) {
                ex.printStackTrace()

            }

        } else {

            binding.myGLSurfaceView.takePicture({ bmp: Bitmap? ->

                var bitmap = bmp

                if (bitmap != null) {

                    val s = SaveCameraImage.saveBitmap(bitmap)

                    filePath = s

                    /*val file = File(Uri.parse("file://$s").path)
                            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            mediaScanIntent.data = Uri.fromFile(file)
                            Common.savedFilePath = Uri.fromFile(file)
                            MediaScannerConnection.scanFile(
                                this@CameraDemoActivity, arrayOf(file.toString()), null
                            ) { path, uri ->
                                Log.d(
                                    "appname",
                                    "image is saved in gallery and gallery is refreshed."
                                )
                            }*/


                } else {


                }
            }, null, null, 0.6f, true)


        }


    }

    @Suppress("DEPRECATION")
    fun saveMediaToStorage(bitmap: Bitmap): String? {

        var filePath: String? = null

        //Generating a file name
        val filename = "JPEG_${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            applicationContext?.contentResolver?.also { resolver ->

                val dirDest = File(Environment.DIRECTORY_DCIM, "PGU")

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirDest")
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }

            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val dirDest = File(imagesDir, "PGU")
            if (!dirDest.exists()) {
                dirDest.mkdirs()
            }
            val image = File(dirDest, filename)

            Log.e("myFilePath", "${image}")

            filePath = image.toString()

            fos = FileOutputStream(image)

        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            it.flush()
            it.close()


            Log.e("myFileFos", "Saved to Photos")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                filePath = saveInternalDirectory(bitmap)
            } else {
                bitmap.recycle()
            }
        }

        return filePath
    }

    fun saveInternalDirectory(bitmap: Bitmap): String? {

        var filePath: String? = null

        //Generating a file name
        val filename = "JPEG_${System.currentTimeMillis()}.jpg"

        //Output stream
        val fos: OutputStream?

        val imagesDir = Util.getRootPath(this)
        val dirDest = File(imagesDir, "saveData")
        if (!dirDest.exists()) {
            dirDest.mkdirs()
        }
        val image = File(dirDest, filename)

        fos = try {
            FileOutputStream(image)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }


        if (fos != null) {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            Log.e("myFOS", "File is save : ${image}")

            bitmap.recycle()

            filePath = image.toString()
        } else {
            Log.e("myFOS", "FOS is null")
        }

        return filePath
    }
}
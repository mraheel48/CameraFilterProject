package com.example.deafultproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.VideoView
import com.otaliastudios.cameraview.VideoResult

class VideoPreviewActivity : AppCompatActivity() {
    companion object {
        var videoResult: VideoResult? = null
    }

    private val videoView: VideoView by lazy { findViewById<VideoView>(R.id.video) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
    }
}
package com.facebook.video_demo

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val videoView: VideoView = findViewById(R.id.video_view)

        // Set the path of the video file (assuming the video file is in res/raw directory)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.anthem

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        // Set video URI and start playback
        videoView.setVideoURI(Uri.parse(videoPath))
        videoView.setMediaController(mediaController)
        videoView.requestFocus()
        videoView.start()
    }
}
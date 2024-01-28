package com.facebook.video_demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView


class VideoUrlActivity : AppCompatActivity() {
    private lateinit var exoPlayerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_url)
        exoPlayerView = findViewById(R.id.exoplayer)

        // Create an instance of the ExoPlayer..
        exoPlayer = ExoPlayer.Builder ( this).build()

        // Set the player to the PlayerView
        exoPlayerView.player = exoPlayer

        val mediaItem=MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the player when the activity is destroyed
        exoPlayer.release()
    }
}

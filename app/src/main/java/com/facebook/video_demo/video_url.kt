package com.facebook.video_demo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView


class VideoUrlActivity : AppCompatActivity() {
    private lateinit var exoPlayerView: StyledPlayerView
    private lateinit var exoPlayer: ExoPlayer
    private  lateinit var imageButton: ImageButton
    private var isLocked = false // Variable to track the lock state


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_url)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        imageButton=findViewById(R.id.lockButton)
        exoPlayerView = findViewById(R.id.exoplayer)

        // Create an instance of the ExoPlayer..
        exoPlayer = ExoPlayer.Builder ( this).build()

        // Set the player to the PlayerView
        exoPlayerView.player = exoPlayer

        val mediaItem=MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        exoPlayer.setMediaItem(mediaItem)
        // Add Player.EventListener to handle player state changes
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (playWhenReady) {
                            showToast("Video is playing")
                        } else {
                            showToast("Video is paused")
                        }
                    }
                }
            }
        })
        // Set initial state of lock button
        updateLockButton()

        // Set click listener for the lock button
        imageButton.setOnClickListener {
            toggleLockState()
        }

        // Prepare and play the video
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // This Function  calls the playerState function and  lock button function...
    private fun toggleLockState() {
        isLocked = !isLocked
        updatePlayerState()
        updateLockButton()
        showToast(if (isLocked) "Player Locked" else "Player Unlocked")
    }
    //This function updates the player state..
    private fun updatePlayerState() {
        exoPlayerView.useController = !isLocked
        //exoPlayerView.showController()
    }

    private fun updateLockButton() {
        val imageResource = if (isLocked) {
            R.drawable.baseline_lock_24 // Change this to the locked button image resource
        } else {
            R.drawable.baseline_lock_open_24 // Change this to the unlocked button image resource
        }
        imageButton.setImageResource(imageResource)
        imageButton.setBackgroundResource(android.R.color.transparent) // Set background to transparent

    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the player when the activity is destroyed
        exoPlayer.release()
    }
}

package com.facebook.video_demo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.ui.TimeBar


class VideoUrlActivity : AppCompatActivity() {
    private lateinit var exoPlayerView: StyledPlayerView
    private lateinit var exoPlayer: ExoPlayer
    private  lateinit var imageButton: ImageButton
    private var isLocked = false // Variable to track the lock state
    private  lateinit var progressBar:DefaultTimeBar
    private lateinit var timeTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_url)
        val playPauseButton = findViewById<ImageButton>(R.id.playPauseButton)
        val rewindButton = findViewById<ImageButton>(R.id.rewindButton)
        val fastForwardButton = findViewById<ImageButton>(R.id.fastForwardButton)
        timeTextView = findViewById(R.id.timeTextView)
        val prevButton = findViewById<ImageButton>(R.id.prevButton)
        val nextButton = findViewById<ImageButton>(R.id.nextButton)
        progressBar = findViewById<DefaultTimeBar>(R.id.progressBar)

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

        // Set click listeners for the control buttons
        playPauseButton.setOnClickListener {
            // Toggle play/pause state
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                // Change the image to play icon when paused
                playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                exoPlayer.play()
                // Change the image to pause icon when playing
                playPauseButton.setImageResource(R.drawable.baseline_pause_24)
            }
        }
        rewindButton.setOnClickListener {
            // Rewind by 5 seconds
            val currentPosition = exoPlayer.currentPosition
            val newPosition = currentPosition - 5000 // 5000 milliseconds (5 seconds)
            exoPlayer.seekTo(newPosition.coerceAtLeast(0))
        }

        fastForwardButton.setOnClickListener {
            // Fast forward by 5 seconds
            val currentPosition = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            val newPosition = currentPosition + 5000 // 5000 milliseconds (5 seconds)
            exoPlayer.seekTo(newPosition.coerceAtMost(duration))
        }

        // Add scrubber dragged listener to the progress bar
        progressBar.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                // Called when the user stops dragging the scrubber
                if (!canceled) {
                    // Seek to the position when scrubbing is stopped
                    exoPlayer.seekTo(position)
                }
            }
        })

        val mediaItem=MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        exoPlayer.setMediaItem(mediaItem)
        // Add Player.EventListener to handle player state changes
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (playWhenReady) {
                            // Start updating the progress bar and time when video starts playing
                            startUpdatingProgressBarAndTime()
                        } else {
                            showToast("Video is paused")
                            // Stop updating the progress bar and time when video is paused
                            stopUpdatingProgressBarAndTime()
                        }
                    }
                    Player.STATE_ENDED -> {
                        // Stop updating the progress bar and time when video ends
                        stopUpdatingProgressBarAndTime()
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

    // Function to start updating the progress bar
    // Function to start updating the progress bar and time
    private fun startUpdatingProgressBarAndTime() {
        val handler = android.os.Handler()
        handler.post(object : Runnable {
            override fun run() {
                // Update progress bar position and time
                updateProgressBarAndTime()
                // Call this runnable again after 1000 milliseconds (1 second)
                handler.postDelayed(this, 100)
            }
        })
    }

    // Function to stop updating the progress bar and time
    private fun stopUpdatingProgressBarAndTime() {
        // Remove any existing callbacks to stop updating the progress bar and time
        android.os.Handler().removeCallbacksAndMessages(null)
    }

    // Function to update the progress bar and time
    private fun updateProgressBarAndTime() {
        val duration = exoPlayer.duration
        val currentPosition = exoPlayer.currentPosition

        // Update progress bar position
        progressBar.setPosition(currentPosition)
        progressBar.setDuration(duration)

        // Update time TextView
        val progressTimeString = formatTime(currentPosition)
        val durationTimeString = formatTime(duration)
        val timeText = "$progressTimeString/$durationTimeString"
        timeTextView.text = timeText
    }

    // Function to format time in mm:ss format
    private fun formatTime(timeInMillis: Long): String {
        val totalSeconds = timeInMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
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
package com.facebook.video_demo

import SettingsHandler
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class VideoUrlActivity : AppCompatActivity() {
    private lateinit var exoPlayerView: StyledPlayerView
    private lateinit var exoPlayer: ExoPlayer
    private  lateinit var imageButton: ImageButton
    private var isLocked = false // Variable to track the lock state
    private  lateinit var progressBar:DefaultTimeBar
    private lateinit var timeTextView: TextView
    private lateinit var speakerButton: ImageButton
    private var isMuted = false // Variable to track the mute state
    private lateinit var settingsButton: ImageButton

    //These are for the ads playing ..
    private var mInterstitialAd: InterstitialAd? = null
    private final val TAG = "MainActivity"
    private var adDisplayed = false


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
        speakerButton = findViewById(R.id.speaker)
        settingsButton = findViewById(R.id.settings)



        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        imageButton=findViewById(R.id.lockButton)
        exoPlayerView = findViewById(R.id.exoplayer)


       //Initializing Google sdk ads here..and also building build request..
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()

        // Set click listener for the speaker button
        speakerButton.setOnClickListener {
            toggleMuteState()
        }

        //set the click listner for settings button..
        settingsButton.setOnClickListener {
            // Initialize the Settings class
           SettingsHandler(this,settingsButton)
        }

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

        // Handling loading of ad here..
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError?.toString()?.let { Log.d(TAG, it) }
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
                setupAdCallbacks()
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

    // Function to start updating the progress bar and time
    private fun startUpdatingProgressBarAndTime() {
        val handler = android.os.Handler()
        handler.post(object : Runnable {
            override fun run() {
                adload()
                // Update progress bar position and time
                updateProgressBarAndTime()
                // Call this runnable again after 1000 milliseconds (1 second)
                handler.postDelayed(this, 100)
            }
        })
    }

    private fun adload() {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.")
            }
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

    // Function to toggle the mute state
    private fun toggleMuteState() {
        isMuted = !isMuted

        // Toggle the audio on and off
        exoPlayer.volume = if (isMuted) 0f else 1f

        // Change the image resource of the speaker button
        updateSpeakerButton()
    }
    // Function to update the speaker button image
    private fun updateSpeakerButton() {
        val imageResource = if (isMuted) {
            R.drawable.baseline_volume_off_24 // Change this to the volume off image resource
        } else {
            R.drawable.baseline_volume_up_24 // Change this to the volume up image resource
        }
        speakerButton.setImageResource(imageResource)
    }


    // New function to set up callbacks for interstitial ad events..
    //Handling Video Here..
    private fun setupAdCallbacks() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Interstitial ad dismissed callback
                adDisplayed = false
                resumeVideo()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                adDisplayed = false
            }

            override fun onAdShowedFullScreenContent() {
                // Interstitial ad displayed callback
                adDisplayed = true
                pauseVideo()
            }
        }
    }



    private fun pauseVideo() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    private fun resumeVideo() {
        if (!exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Release the player when the activity is destroyed
        exoPlayer.release()
    }
}
package com.example.cpu02351_local.videoviewtest

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.example.cpu02351_local.videoviewtest.DownloadService.Companion.VIDEO_LOADED
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {

    private val videoPath = "https://firebasestorage.googleapis.com/v0/b/fir-chat-47b52.appspot.com/o/messages%2FVID_20170707_170659.mp4?alt=media&token=6d43ede4-092f-42ef-bd8b-01734c466592"
    private val localPath = "VID20170707_170659"

    private lateinit var buttonStart: Button
    private lateinit var buttonChoose: Button
    private lateinit var playerView: SimpleExoPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.myVideoView)
        buttonStart = findViewById(R.id.button4)
        buttonChoose = findViewById(R.id.button5)
        buttonStart.setOnClickListener { startVideoPlay() }
        buttonChoose.setOnClickListener { startVideoPick() }
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Downloading")
        progressDialog.setMessage("Please wait while we download video")
        progressDialog.isIndeterminate = true
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val resultCode = intent?.getIntExtra("resultCode", -1) ?: return
            progressDialog.dismiss()
            if (resultCode == VIDEO_LOADED) {
                playVideo()
            } else {
                // retry
                downloadVideo()
            }
        }
    }

    private fun setUpBroadcastReceiver() {
        val filter = IntentFilter("demo.video.download.result")
        registerReceiver(receiver, filter)
    }

    private fun startVideoPlay() {
        if (isAvailableOffline()) {
            playVideo()
        } else {
            downloadVideo()
        }
    }

    private fun isAvailableOffline(): Boolean {
        val file = applicationContext.getFileStreamPath(localPath)
        return file.exists()
    }

    private lateinit var player: SimpleExoPlayer
    private fun playVideo() {
        Toast.makeText(this, "Video exists", Toast.LENGTH_SHORT).show()
        val mainHandler = Handler()
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        playerView.player = player
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "VideoViewTest"), bandwidthMeter)
        val file = getFileStreamPath(localPath)
        val uri = Uri.fromFile(file)
        val videoSource = ExtractorMediaSource(uri, dataSourceFactory, DefaultExtractorsFactory(), mainHandler, null)
        val loopingSource = LoopingMediaSource(videoSource)
        player.prepare(loopingSource)
        player.playWhenReady = true
    }

    private lateinit var progressDialog: ProgressDialog
    private fun downloadVideo() {
        progressDialog.show()
        val downloadIntent = Intent(this, DownloadService::class.java)
        downloadIntent.putExtra("video_url", videoPath)
        downloadIntent.putExtra("download_location", localPath)
        startService(downloadIntent)
    }


    private fun startVideoPick() {

    }

    override fun onStart() {
        super.onStart()
        setUpBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
        player.release()
    }
}

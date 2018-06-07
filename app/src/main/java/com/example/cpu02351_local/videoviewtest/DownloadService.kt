package com.example.cpu02351_local.videoviewtest

import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService("Download service") {

    companion object {
        const val VIDEO_LOADED = 100
        const val VIDEO_ERROR = 150
        const val RESPONSE_OK = 200
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d("DEBUG_DOWNLOAD", "Download started")
        if (intent == null) return
        val url = intent.getStringExtra("video_url")
        val fileName = intent.getStringExtra("download_location")
        val downloadFile = getFileStreamPath(fileName)
        if (downloadFile.exists()) {
            notifyDownloadSuccess()
            return
        }
        try {
            downloadFile.createNewFile()
            val downloadUrl = URL(url)
            val connection = downloadUrl.openConnection() as HttpURLConnection

            if (connection.responseCode == RESPONSE_OK) {
                val inputStream = connection.inputStream
                val outStream = FileOutputStream(downloadFile)
                val buffer = ByteArray(1024)
                var byteCount = inputStream.read(buffer)
                while (byteCount  != -1) {
                    outStream.write(buffer, 0, byteCount)
                    byteCount = inputStream.read(buffer)
                }
                inputStream.close()
                outStream.close()
                notifyDownloadSuccess()
            }
        } catch (e: Exception) {
            notifyDownloadError()
        }
    }

    private fun notifyDownloadSuccess() {
        Log.d("DEBUG_DOWNLOAD", "Download done")
        val i = Intent("demo.video.download.result")
        i.putExtra("resultCode", VIDEO_LOADED)
        sendBroadcast(i)
    }

    private fun notifyDownloadError() {
        Log.d("DEBUG_DOWNLOAD", "Download error")
        val i = Intent("demo.video.download.result")
        i.putExtra("resultCode", VIDEO_ERROR)
        sendBroadcast(i)
    }
}
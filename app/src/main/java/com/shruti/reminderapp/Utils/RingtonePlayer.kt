package com.shruti.reminderapp.Utils

import android.content.Context
import android.media.MediaPlayer

class RingtonePlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun startRingtone(rawResourceId: Int) {
        stopRingtone()

        try {
            mediaPlayer = MediaPlayer.create(context, rawResourceId).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error starting MediaPlayer: ${e.message}")
        }
    }

    fun stopRingtone() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
            }
            mediaPlayer!!.release()
            mediaPlayer = null
            println("Ringtone stopped and resources released.")
        }
    }
}
package com.shruti.reminderapp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shruti.reminderapp.R
import com.shruti.reminderapp.activity.WebViewActivity
import com.shruti.reminderapp.activity.MainActivity

class AlarmReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("alarm_title") ?: "Reminder"
        val date = intent.getStringExtra("alarm_date") ?: ""
        val time = intent.getStringExtra("alarm_time") ?: ""
        val lang = intent.getStringExtra("alarm_lang") ?: ""
        Log.d("Alarm Receiver", "Alarm triggered: $title at $time ($lang)")

        launchReminderDialog(context, title, date, time, lang)
    }

    private fun launchReminderDialog(context: Context, title: String, date: String, time: String, lang: String) {
        try {
            val dialogIntent = Intent(context, WebViewActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("alarm_title", title)
                putExtra("alarm_date", date)
                putExtra("alarm_time", time)
                putExtra("alarm_lang", lang)
            }
            context.startActivity(dialogIntent)
            Log.d("AlarmReceiver", "Dialog activity launched")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to launch dialog: ${e.message}")
            e.printStackTrace()
        }
    }



}
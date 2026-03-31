package com.shruti.reminderapp.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shawnlin.numberpicker.NumberPicker
import com.shruti.reminderapp.R
import com.shruti.reminderapp.RoomDatabase.AppDatabase
import com.shruti.reminderapp.RoomDatabase.Reminder
import com.shruti.reminderapp.databinding.ActivitySetReminderBinding
import com.shruti.reminderapp.dataclass.RemainderDataClass
import com.shruti.reminderapp.generalfunctions.GeneralFunctions
import com.shruti.reminderapp.receiver.AlarmReceiver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SetReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetReminderBinding
    private lateinit var database: DatabaseReference // Switched to Realtime Database
    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase

    private val languageMap = mapOf(
        "Hindi" to "hi", "Punjabi" to "pa", "Arabic" to "ar",
        "Chinese" to "han", "Hiragana" to "ja", "Japanese" to "ja",
        "Korean" to "ko", "Russian" to "ru", "Thai" to "th",
        "Tamil" to "ta", "Bengali" to "bn", "Telugu" to "te",
        "Gujarati" to "gu", "Kannada" to "kn", "Malayalam" to "ml",
        "Oriya" to "or"
    )

    private var selectedLang = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        // View Compatibility for Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase & Room
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference // Realtime Database root
        db = AppDatabase.getDatabase(this)

        val isUpdate = intent.getBooleanExtra("update", false)
        val reminderId = intent.getStringExtra("reminder_id")

        if (isUpdate && reminderId != null) {
            fetchReminderForUpdate(reminderId)
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        fabClick(isUpdate, reminderId)
        binding.cancelButton.setOnClickListener { finish() }
    }

    private fun requestExactAlarmPermission(onGranted: () -> Unit) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) { e.printStackTrace() }
                return
            }
        }
        onGranted()
    }

    private fun setupNumberPicker(picker: NumberPicker, values: Array<String>, default: Int) {
        picker.minValue = 0
        picker.maxValue = values.size - 1
        picker.displayedValues = values
        picker.value = default

        val request = FontRequest(
            "com.google.android.gms.fonts", "com.google.android.gms", "Arvo",
            R.array.com_google_android_gms_fonts_certs
        )

        FontsContractCompat.requestFont(this, request, object : FontsContractCompat.FontRequestCallback() {
            override fun onTypefaceRetrieved(typeface: Typeface) { picker.typeface = typeface }
        }, Handler(Looper.getMainLooper()))
    }

    @SuppressLint("SetTextI18n")
    private fun fabClick(isUpdate: Boolean, reminderId: String?) {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val amPm = arrayOf("AM", "PM")

        val calendar = Calendar.getInstance()
        setupNumberPicker(binding.dayPicker, (1..31).map { it.toString() }.toTypedArray(), calendar.get(Calendar.DAY_OF_MONTH) - 1)
        setupNumberPicker(binding.monthPicker, months, calendar.get(Calendar.MONTH))
        setupNumberPicker(binding.hourPicker, (1..12).map { it.toString() }.toTypedArray(), (calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }) - 1)
        setupNumberPicker(binding.minutePicker, (0..59).map { String.format("%02d", it) }.toTypedArray(), calendar.get(Calendar.MINUTE))
        setupNumberPicker(binding.amPmPicker, amPm, if (calendar.get(Calendar.AM_PM) == Calendar.AM) 0 else 1)

        val langNames = languageMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, langNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.LangSpinner.adapter = adapter

        binding.LangSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedLang = languageMap[langNames[position]] ?: "en"
            }
            override fun onNothingSelected(parent: AdapterView<*>) { selectedLang = "en" }
        }

        binding.btnDone.setOnClickListener {
            val title = binding.etTitle.text.toString()
            if (title.isEmpty()) {
                GeneralFunctions.showToast(this, "Enter title")
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            binding.pgBar.visibility = View.VISIBLE

            val date = "%02d/%02d/%d".format(binding.dayPicker.value + 1, binding.monthPicker.value + 1, Calendar.getInstance().get(Calendar.YEAR))
            val time = "%02d:%02d %s".format(binding.hourPicker.value + 1, binding.minutePicker.value, amPm[binding.amPmPicker.value])

            if (isUpdate && reminderId != null) {
                updateRemainder(userId, reminderId, title, date, time)
            } else {
                addRemainder(userId, title, date, time)
            }

            lifecycleScope.launch {
                db.dao().insertReminder(Reminder(title = title, date = date, time = time))
            }

            requestExactAlarmPermission { scheduleAlarm(title, date, time, selectedLang) }
            finish()
        }
    }

    private fun addRemainder(userId: String, title: String, date: String, time: String) {
        val ref = database.child("users").child(userId).child("reminders").push()
        val key = ref.key ?: ""
        val data = RemainderDataClass(key, title, date, time)

        ref.setValue(data).addOnSuccessListener {
            GeneralFunctions.showToast(this, "Reminder saved.")
        }.addOnFailureListener {
            Log.e("DatabaseError", it.message ?: "Failed to save")
        }
    }

    private fun fetchReminderForUpdate(reminderId: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("reminders").child(reminderId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    binding.etTitle.setText(snapshot.child("title").value.toString())
                }
            }
    }

    private fun updateRemainder(userId: String, reminderId: String, title: String, date: String, time: String) {
        val updatedData = RemainderDataClass(reminderId, title, date, time)
        database.child("users").child(userId).child("reminders").child(reminderId)
            .setValue(updatedData)
            .addOnSuccessListener {
                GeneralFunctions.showToast(this, "Reminder updated in Cloud")
            }
    }

    private fun scheduleAlarm(title: String, date: String, time: String, lang: String) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val dateTime = sdf.parse("$date $time") ?: return
            val calendar = Calendar.getInstance().apply { this.time = dateTime }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("alarm_title", title)
                putExtra("alarm_date", date)
                putExtra("alarm_time", time)
                putExtra("alarm_lang", lang)
            }

            val pending = PendingIntent.getBroadcast(
                this,
                (title + date + time).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, pending), pending)
            binding.pgBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
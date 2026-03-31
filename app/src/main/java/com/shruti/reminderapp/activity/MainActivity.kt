package com.shruti.reminderapp.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shruti.reminderapp.adapter.RemainderAdapter
import com.shruti.reminderapp.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shruti.reminderapp.R
import com.shruti.reminderapp.RoomDatabase.Reminder
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), RemainderAdapter.OnClick {
    lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    lateinit var auth: FirebaseAuth
    lateinit var adapter: RemainderAdapter
    private lateinit var tts: TextToSpeech

    private var dbQuery: Query? = null
    private var dbListener: ValueEventListener? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var TAG = "Main Activity"
    private val request_code = 101

    private var itemList = ArrayList<Reminder>()
    private var FilterSelectedDate: String = ""

    override fun onResume() {
        super.onResume()
        getFilterReminder(FilterSelectedDate)
        setupGraph() // Refresh graph when returning to activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.fabAi.setOnClickListener {
            startActivity(Intent(this, AIPlannerActivity::class.java))
        }

        requestExactAlarmPermission()

        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }

        // Initialize RecyclerView
        adapter = RemainderAdapter(itemList, this, this)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        setupPermissions()

        binding.fab.setOnClickListener {
            startActivity(Intent(this, SetReminderActivity::class.java))
        }

        binding.profileIcon.setOnClickListener {
            showUserMenu()
        }

        // Calendar View Setup
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        FilterSelectedDate = dateFormat.format(calendar.time)
        binding.textMonth.text = " $FilterSelectedDate"

        getFilterReminder(FilterSelectedDate)
        setupGraph()

        binding.topLayout.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setInputMode(INPUT_MODE_CALENDAR)

            val datePicker = builder.build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                utcCalendar.timeInMillis = selection
                val formattedDate = dateFormat.format(utcCalendar.time)

                FilterSelectedDate = formattedDate
                binding.textMonth.text = formattedDate
                getFilterReminder(FilterSelectedDate)
            }
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupGraph() {
        val userId = auth.currentUser?.uid ?: return
        val lastSevenDays = ArrayList<String>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Generate last 7 dates
        for (i in 0..6) {
            lastSevenDays.add(dateFormat.format(cal.time))
            cal.add(Calendar.DATE, -1)
        }
        lastSevenDays.reverse()

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        var processedCount = 0
        for ((index, dateStr) in lastSevenDays.withIndex()) {
            database.child("users").child(userId).child("reminders")
                .orderByChild("date")
                .equalTo(dateStr)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val count = snapshot.childrenCount.toFloat()
                        entries.add(Entry(index.toFloat(), count))
                        labels.add(dateStr.substring(0, 5)) // "dd/MM"

                        processedCount++
                        if (processedCount == 7) {
                            updateChartUI(entries, labels)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun updateChartUI(entries: List<Entry>, labels: List<String>) {
        val sortedEntries = entries.sortedBy { it.x }
        val skyBlue = Color.parseColor("#0ea5e9") // vivid_sky_blue

        val dataSet = LineDataSet(sortedEntries, "Reminders").apply {
            color = skyBlue
            setCircleColor(skyBlue)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = skyBlue
            fillAlpha = 40
            setDrawHorizontalHighlightIndicator(false)
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisLeft.setDrawGridLines(false)
            axisLeft.granularity = 1f
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    private fun getFilterReminder(selectedDate: String) {
        val userId = auth.currentUser?.uid ?: return
        dbListener?.let { dbQuery?.removeEventListener(it) }

        dbQuery = database.child("users").child(userId).child("reminders")
            .orderByChild("date")
            .equalTo(selectedDate)

        dbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (doc in snapshot.children) {
                    val reminder = Reminder(
                        title = doc.child("title").value.toString(),
                        date = doc.child("date").value.toString(),
                        time = doc.child("time").value.toString()
                    )
                    itemList.add(reminder)
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) { Log.e(TAG, error.message) }
        }
        dbQuery?.addValueEventListener(dbListener!!)
    }

    override fun delete(reminder: Reminder) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("reminders")
            .orderByChild("title")
            .equalTo(reminder.title)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        if (child.child("date").value == reminder.date && child.child("time").value == reminder.time) {
                            child.ref.removeValue()
                        }
                    }
                    Toast.makeText(this@MainActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    setupGraph() // Update graph after deletion
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showUserMenu() {
        val email = auth.currentUser?.email ?: "No email found"
        MaterialAlertDialogBuilder(this)
            .setTitle("Account Details")
            .setMessage("Logged in as:\n$email")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupPermissions() {
        if (isMiuiDevice()) {
            if (!getBoolean(this, KEY_MIUI_POPUP_PERMISSION)) showBackgroundPopupWarning(this)
            if (!getBoolean(this, KEY_MIUI_BACKGROUND_AUTOSTART)) showAutoStartPermission(this)
        } else if (!Settings.canDrawOverlays(this)) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission Required")
                .setMessage("Please allow 'Display over other apps' for alarms to function.")
                .setPositiveButton("OK") { _, _ ->
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), request_code)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // --- MIUI Methods ---
    fun showAutoStartPermission(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Auto Start")
            .setMessage("Enable Auto Start for reliable reminders.")
            .setPositiveButton("OK") { _, _ ->
                openMiuiAutoStart(this)
                saveBoolean(this, KEY_MIUI_BACKGROUND_AUTOSTART, true)
            }.show()
    }

    fun openMiuiAutoStart(context: Context) {
        try {
            val intent = Intent().apply { component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity") }
            context.startActivity(intent)
        } catch (e: Exception) {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.parse("package:${context.packageName}") })
        }
    }

    fun showBackgroundPopupWarning(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Background Pop-ups")
            .setMessage("Enable 'Display pop-ups while running in background' in permissions.")
            .setPositiveButton("OK") { _, _ ->
                openMIUIPopupPermission(this)
                saveBoolean(this, KEY_MIUI_POPUP_PERMISSION, true)
            }.show()
    }

    fun openMIUIPopupPermission(context: Context) {
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                putExtra("extra_pkgname", context.packageName)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null) })
        }
    }

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivityForResult(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { data = Uri.parse("package:$packageName") }, 1001)
            }
        }
    }

    fun isMiuiDevice(): Boolean {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        return listOf("xiaomi", "redmi", "poco", "mi").any { it == brand || it == manufacturer }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        dbListener?.let { dbQuery?.removeEventListener(it) }
        coroutineScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_MIUI_POPUP_PERMISSION = "miui_popup_checked"
        private const val KEY_MIUI_BACKGROUND_AUTOSTART = "miui_background_autostart_checked"
        private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        fun saveBoolean(context: Context, key: String, value: Boolean) { getPrefs(context).edit().putBoolean(key, value).apply() }
        fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean = getPrefs(context).getBoolean(key, defaultValue)
    }
}
package com.shruti.reminderapp.activity

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import com.shruti.reminderapp.R
import com.shruti.reminderapp.Utils.Chatbot
import com.shruti.reminderapp.Utils.RingtonePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class WebViewActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var webView: WebView
    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var ringer: RingtonePlayer
    private lateinit var tts: TextToSpeech
    var response = ""

    private lateinit var motivationTextView: TextView
    private var title: String? = "Lunch"
    private var date: String? = null
    private var time: String? = null
    private var selectedLang: String? = "English"


    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.light_vivid_sky_blue)

        setContentView(R.layout.activity_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ringer = RingtonePlayer(this)
        ringer.startRingtone(R.raw.iphone_alarm)


        tts = TextToSpeech(this, this)


        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)

                runOnUiThread {
                    highlightText(response, start, end)
                }
            }

            override fun onStart(utteranceId: String?) {

                runOnUiThread {
                    ringer.stopRingtone()
                    try {
                        mediaPlayer?.stop()
                    } catch (_: Exception) {
                    }
                    try {
                        mediaPlayer?.release()
                    } catch (_: Exception) {
                    }

                    webView.evaluateJavascript("window.setTalkingStatus(true)", null)

                }
            }

            override fun onDone(utteranceId: String?) {

                runOnUiThread {
                    webView.evaluateJavascript("window.setTalkingStatus(false)", null)

                    finish()
                }
            }

            override fun onError(utteranceId: String?) {
                // Error occurred
            }
        })

        makeActivityShowOnLockScreen()
        acquireWakeLock()

        readIntentData()
// Inside onCreate:
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(this))
            .build()

        webView = findViewById(R.id.three_webview)
        webView.settings.apply {
            javaScriptEnabled = true
            // Crucial for modules to work
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Example: Send a message 2 seconds after loading
                lifecycleScope.launch {
                    Chatbot.getGeminiTextResponse(
                        """

                        You are a motivational assistant that converts reminders into energetic messages.

                                    TASK: Convert this reminder into a motivational message (15-20 words)

                                    REMINDER: "$title"

                                    REQUIREMENTS:
                                    - Output language: $selectedLang (must be written in this language only)
                                    - Length: 15-20 words
                                    - Tone: Positive, energetic, motivational
                                    - No emojis allowed
                                    - Must sound natural when spoken aloud
                                    - Keep it concise and uplifting

                                    LANGUAGE CODES REFERENCE:
                                    Hindi: "hi", Punjabi: "pa", Arabic: "ar", Chinese: "han",
                                    Japanese: "ja", Korean: "ko", Russian: "ru", Thai: "th",
                                    Tamil: "ta", Bengali: "bn", Telugu: "te", Gujarati: "gu",
                                    Kannada: "kn", Malayalam: "ml", Oriya: "or"

                                    EXAMPLE:
                                    Input: "study for math"
                                    English Output: "Time to conquer math! Stay focused and make this study session count. You've got this!"

                                    Now create a motivational message in $selectedLang for: "$title"

                    """.trimIndent()
                    ) { result ->


                        if (result == "Unable to fetch response!") {

                            response = title.toString()
                        } else {


                            response = result
                            webView.postDelayed({
                                sendTextToAvatar(response)
                                speak(response)
                            }, 100)
                        }

                    }
                }
            }

            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                // Intercepts the virtual URL and directs it to your local assets folder
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }

// Load the virtual URL instead of file:///
        webView.loadUrl("https://appassets.androidplatform.net/assets/avatar.html")
    }

    fun sendTextToAvatar(text: String) {
        val safeText = JSONObject.quote(text)
        webView.evaluateJavascript(
            "window.showToast($safeText)",
            null
        )
    }

    private fun highlightText(fullText: String, start: Int, end: Int) {
        val spannable = SpannableString(fullText)

        // Set highlight color (e.g., Yellow or a light blue from your resources)
        val highlightColor = ContextCompat.getColor(this, R.color.light_vivid_sky_blue)

        spannable.setSpan(
            BackgroundColorSpan(highlightColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        motivationTextView.text = spannable
    }

    private fun readIntentData() {
        title = intent.getStringExtra("alarm_title")
        date = intent.getStringExtra("alarm_date")
        time = intent.getStringExtra("alarm_time")
        selectedLang = intent.getStringExtra("alarm_lang")

//        if (title.isNullOrEmpty()) {
//            finish()
//            return
//        }
    }

    private fun makeActivityShowOnLockScreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "ReminderApp::AlarmWakeLock"
        )
        wakeLock?.acquire(3 * 60 * 1000L)
    }

    private fun finishWithCleanup() {

        ringer.stopRingtone()
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

        wakeLock?.let {
            if (it.isHeld) it.release()
        }

        coroutineScope.cancel()
        finish()
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                // Language not supported
            } else {
//                speak("Text to speech initialized successfully")
            }
        }
    }

    private fun speak(text: String) {
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "TTS_ID"
        )
    }

    override fun onDestroy() {
        finishWithCleanup()

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class VoiceMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class VoiceAgentManager(
    private val context: Context,
    private val onCommandReceived: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _currentLanguage = MutableStateFlow("hi-IN")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _messages = MutableStateFlow<List<VoiceMessage>>(emptyList())
    val messages: StateFlow<List<VoiceMessage>> = _messages.asStateFlow()

    private val _liveSpokenText = MutableStateFlow("")
    val liveSpokenText: StateFlow<String> = _liveSpokenText.asStateFlow()

    var isContinuousMode: Boolean = true

    init {
        initializeTts()
        initializeRecognizer()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            setLanguage(_currentLanguage.value)
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                    if (isContinuousMode && utteranceId == "agent_reply_with_listen") {
                        // Automatically restart listening for hands-free two-way conversation
                        startListening()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                }
            })

            // Welcome greeting in Hindi
            speak(
                "नमस्ते! मैं आपका ऑल-इन-वन साइबर कैफे, वकील और सहारा रिफंड एआई एजेंट हूँ। आप बोलकर सहारा क्लेम, कानूनी नोटिस, फोटो शीट या बिलिंग का काम करवा सकते हैं।",
                listenAfterSpeaking = false
            )
        } else {
            Log.e("VoiceAgent", "TTS Initialization failed with status: $status")
        }
    }

    private fun initializeRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceAgentManager)
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceAgent", "SpeechRecognizer error: ${e.message}")
        }
    }

    fun setLanguage(langCode: String) {
        _currentLanguage.value = langCode
        val locale = when (langCode) {
            "bho-IN" -> Locale.forLanguageTag("hi-IN")
            else -> Locale.forLanguageTag(langCode)
        }
        textToSpeech?.let {
            val result = it.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                it.setLanguage(Locale.ENGLISH)
            }
        }
    }

    fun startListening() {
        if (_voiceState.value == VoiceState.SPEAKING) {
            textToSpeech?.stop()
        }

        try {
            if (speechRecognizer == null) {
                initializeRecognizer()
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, _currentLanguage.value)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, _currentLanguage.value)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "बोलिए, मैं सुन रहा हूँ...")
            }
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
            _liveSpokenText.value = ""
        } catch (e: Exception) {
            Log.e("VoiceAgent", "startListening error: ${e.message}")
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _voiceState.value = VoiceState.IDLE
        } catch (e: Exception) {
            Log.e("VoiceAgent", "stopListening error: ${e.message}")
        }
    }

    fun speak(text: String, listenAfterSpeaking: Boolean = false) {
        if (!isTtsInitialized) return

        addMessage(text, isUser = false)
        _voiceState.value = VoiceState.SPEAKING
        val utteranceId = if (listenAfterSpeaking) "agent_reply_with_listen" else "agent_reply"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun addMessage(text: String, isUser: Boolean) {
        val current = _messages.value.toMutableList()
        current.add(VoiceMessage(text = text, isUser = isUser))
        _messages.value = current
    }

    fun simulateSpokenCommand(command: String) {
        addMessage(command, isUser = true)
        _voiceState.value = VoiceState.PROCESSING
        onCommandReceived(command)
    }

    // SpeechRecognizer Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.value = VoiceState.LISTENING
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _voiceState.value = VoiceState.PROCESSING
    }

    override fun onError(error: Int) {
        _voiceState.value = VoiceState.IDLE
        _liveSpokenText.value = ""
    }

    override fun onResults(results: Bundle?) {
        _voiceState.value = VoiceState.IDLE
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            _liveSpokenText.value = spokenText
            addMessage(spokenText, isUser = true)
            onCommandReceived(spokenText)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _liveSpokenText.value = matches[0]
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e("VoiceAgent", "destroy error: ${e.message}")
        }
    }
}

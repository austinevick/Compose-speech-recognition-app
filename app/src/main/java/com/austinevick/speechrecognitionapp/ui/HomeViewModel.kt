package com.austinevick.speechrecognitionapp.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val generativeModel: GenerativeModel
) : ViewModel(), RecognitionListener {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var intent: Intent? = null


    private var _speechUiState = MutableStateFlow(SpeechUiState())
    val speechUiState = _speechUiState.asStateFlow()


    init {
        initSpeechRecognizer()
        initTextToSpeech()
    }

    fun setSpeaking(isSpeaking: Boolean) {
      _speechUiState.update { state ->
          state.copy(
              isSpeaking = isSpeaking
          )
      }
    }


    fun startListening() {
        speechRecognizer?.startListening(intent)
    }

    fun textToSpeechHandler(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stopTextToSpeech() {
        textToSpeech?.stop()
    }

    fun setPrompt(prompt: String) {
        _speechUiState.update { state ->
            state.copy(
                prompt = prompt
            )
        }
    }

    fun generateResponse(prompt: String) {
        viewModelScope.launch {
            try {
                _speechUiState.update { state ->
                    state.copy(
                        isLoading = true
                    )
                }
                val response = generativeModel.generateContent(prompt)
                _speechUiState.update { state ->
                    state.copy(
                        response = response.text.toString(),
                        isLoading = false
                    )
                }
                Log.d("response", response.toString())
                Log.d("response", response.text.toString())
            } catch (e: Exception) {
                Log.d("data", e.message.toString())
                _speechUiState.update { state ->
                    state.copy(
                        isLoading = false
                    )
                }
            }
        }
    }


    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
        speechRecognizer?.setRecognitionListener(this)
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent?.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
        intent?.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.setSpeechRate(0.7f)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }

    override fun onReadyForSpeech(result: Bundle?) {

    }

    override fun onBeginningOfSpeech() {

    }

    override fun onRmsChanged(result: Float) {

    }

    override fun onBufferReceived(result: ByteArray?) {

    }

    override fun onEndOfSpeech() {

    }

    override fun onError(result: Int) {
        Log.d("data", result.toString())
    }

    override fun onResults(result: Bundle?) {
        val data = result?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = data?.get(0)
        Log.d("data", text.toString())
        _speechUiState.update { state ->
            state.copy(
                prompt = text.toString()
            )
        }
    }

    override fun onPartialResults(result: Bundle?) {

    }

    override fun onEvent(result: Int, p1: Bundle?) {

    }

}

data class SpeechUiState(
    val isLoading: Boolean = false,
    val isSpeaking: Boolean = false,
    val prompt: String = "",
    val response: String = ""
)
//package com.jlss.smartDairy.screen
//
//import android.content.Intent
//import android.os.Bundle
//import android.speech.RecognizerIntent
//import android.speech.SpeechRecognizer
//import android.speech.RecognitionListener
//import android.widget.Toast
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Mic
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import com.jlss.smartDairy.viewmodel.EntryViewModel
//import androidx.compose.material3.Button
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//
//
//@Composable
//fun VoiceInputButton(vm: EntryViewModel) {
//    val context = LocalContext.current
//    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
//    var isListening by remember { mutableStateOf(false) }
//
//    val intent = remember {
//        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
//            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
//        }
//    }
//
//    DisposableEffect(Unit) {
//        val listener = object : RecognitionListener {
//            override fun onResults(results: Bundle?) {
//                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0) ?: ""
//                vm.processSpeechInput(spokenText)
//                isListening = false
//            }
//
//            override fun onError(error: Int) {
//                Toast.makeText(context, "Speech error $error", Toast.LENGTH_SHORT).show()
//                isListening = false
//            }
//
//            override fun onBeginningOfSpeech() {}
//            override fun onBufferReceived(p0: ByteArray?) {}
//            override fun onEndOfSpeech() {}
//            override fun onEvent(p0: Int, p1: Bundle?) {}
//            override fun onPartialResults(p0: Bundle?) {}
//            override fun onReadyForSpeech(p0: Bundle?) {}
//            override fun onRmsChanged(p0: Float) {}
//        }
//
//        speechRecognizer.setRecognitionListener(listener)
//
//        onDispose {
//            speechRecognizer.destroy()
//        }
//    }
//
//    Button(onClick = {
//        if (!isListening) {
//            speechRecognizer.startListening(intent)
//            isListening = true
//        }
//    }) {
//        IconButton(onClick = { }) {
//            Icon(Icons.Default.Mic, contentDescription = "Voice Input")
//        }
//    }
//}

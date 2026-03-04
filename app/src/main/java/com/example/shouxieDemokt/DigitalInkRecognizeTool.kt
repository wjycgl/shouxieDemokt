package com.example.shouxieDemokt

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions

class DigitalInkRecognizeTool {
    interface DigitalModelDownloadListener {
        fun onDownloadSuccess(digitalInkRecognizer: DigitalInkRecognizer)
        fun onDownloadFailure()
    }

    fun downloadDigitalInkModel(language: String, digitalModelDownloadListener: DigitalModelDownloadListener) {
        Thread().run {
            val remoteModel = RemoteModelManager.getInstance()
            val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(language)!!
            val model  = DigitalInkRecognitionModel.builder(modelIdentifier).build()
            val booleanTask = remoteModel.isModelDownloaded(model)
            booleanTask.addOnSuccessListener { boolean->
                if (!boolean){
                    model.let {
                        remoteModel.download(it, DownloadConditions.Builder().build())
                            .addOnSuccessListener {
                                val recognizer: DigitalInkRecognizer =
                                    DigitalInkRecognition.getClient(
                                        DigitalInkRecognizerOptions.builder(model).setMaxResultCount(8).build())
                                digitalModelDownloadListener.onDownloadSuccess(recognizer)
                            }
                            .addOnFailureListener { e: Exception ->
                                digitalModelDownloadListener.onDownloadFailure()
                            }
                    }
                }else{
                    val recognizer: DigitalInkRecognizer =
                        DigitalInkRecognition.getClient(
                            DigitalInkRecognizerOptions.builder(model).setMaxResultCount(8).build())
                    digitalModelDownloadListener.onDownloadSuccess(recognizer)

                }
            }
        }

    }

    companion object{
        fun identityLanguage(text:String){
            val languageIdentifier = LanguageIdentification.getClient()
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageCode ->
                    Log.e("language", "Language: $languageCode")
                }
                .addOnFailureListener {
                }
        }
    }

}
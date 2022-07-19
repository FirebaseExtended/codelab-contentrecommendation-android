/*
 * Copyright 2020 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 *
 */

package com.google.firebase.codelabs.recommendations

import android.content.Context
import android.util.Log
import com.google.firebase.codelabs.recommendations.data.Movie
import com.google.firebase.codelabs.recommendations.data.MovieRepository
import com.google.firebase.codelabs.recommendations.utils.Config
import com.google.firebase.codelabs.recommendations.utils.FileUtils
import com.google.firebase.codelabs.recommendations.utils.showToast
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlinx.coroutines.*
import java.io.File

/** Interface to load TfLite model and provide recommendations.  */
class RecommendationClient(private val context: Context, private val config: Config) {
    private val candidates: MutableMap<Int, Movie> = HashMap()
    private var tflite: Interpreter? = null

    /** An immutable result returned by a RecommendationClient.  */
    data class Result(
        /** Predicted id.  */
        val id: Int,
        /** Recommended item.  */
        val item: Movie,
        /** A sortable score for how good the result is relative to others. Higher should be better.  */
        val confidence: Float
    ) {
        override fun toString(): String {
            return String.format("[%d] confidence: %.3f, item: %s", id, confidence, item)
        }
    }

    /** Load the TF Lite model and dictionary.  */
    suspend fun load() {
        downloadRemoteModel()
        loadLocalModel()
        loadCandidateList()
    }

    /** Load TF Lite model.  */
    private suspend fun loadLocalModel() {
        return withContext(Dispatchers.IO) {
            try {
                val buffer: ByteBuffer = FileUtils.loadModelFile(
                    context.assets, config.modelPath
                )
                initializeInterpreter(buffer)
                Log.v(TAG, "TFLite model loaded.")
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }
    }

    private suspend fun initializeInterpreter(model: Any) {
        return withContext(Dispatchers.IO) {
            tflite?.apply {
                close()
            }
            when (model) {
                is ByteBuffer -> {
                    tflite = Interpreter(model)
                }
                is CustomModel -> {
                    model.file?.let {
                        tflite = Interpreter(it)
                    }
                }
                else -> {
                    showToast(context, "Unexpected model type downloaded.")
                }
            }
            Log.v(TAG, "TFLite model loaded.")
        }
    }

    /** Load recommendation candidate list.  */
    private suspend fun loadCandidateList() {
        return withContext(Dispatchers.IO) {
            val collection = MovieRepository.getInstance(context).getContent()
            for (item in collection) {
                candidates[item.id] = item
            }
            Log.v(TAG, "Candidate list loaded.")
        }
    }

    /** Free up resources as the client is no longer needed.  */
    fun unload() {
        tflite?.close()
        candidates.clear()
    }

    /** Given a list of selected items, preprocess to get tflite input.  */
    private suspend fun preprocess(selectedMovies: List<Movie>): IntArray {
        return withContext(Dispatchers.Default) {
            val inputContext = IntArray(config.inputLength)
            for (i in 0 until config.inputLength) {
                if (i < selectedMovies.size) {
                    val (id) = selectedMovies[i]
                    inputContext[i] = id
                } else {
                    // Padding input.
                    inputContext[i] = config.pad
                }
            }
            inputContext
        }
    }

    /** Postprocess to gets results from tflite inference.  */
    private suspend fun postprocess(
        outputIds: IntArray, confidences: FloatArray, selectedMovies: List<Movie>
    ): List<Result> {
        return withContext(Dispatchers.Default) {
            val results = ArrayList<Result>()

            // Add recommendation results. Filter null or contained items.
            for (i in outputIds.indices) {
                if (results.size >= config.topK) {
                    Log.v(TAG, String.format("Selected top K: %d. Ignore the rest.", config.topK))
                    break
                }
                val id = outputIds[i]
                val item = candidates[id]
                if (item == null) {
                    Log.v(TAG, String.format("Inference output[%d]. Id: %s is null", i, id))
                    continue
                }
                if (selectedMovies.contains(item)) {
                    Log.v(TAG, String.format("Inference output[%d]. Id: %s is contained", i, id))
                    continue
                }
                val result = Result(
                    id, item,
                    confidences[i]
                )
                results.add(result)
                Log.v(TAG, String.format("Inference output[%d]. Result: %s", i, result))
            }
            results
        }
    }

    /** Given a list of selected items, and returns the recommendation results.  */
    suspend fun recommend(selectedMovies: List<Movie>): List<Result> {
        return withContext(Dispatchers.Default) {
            val inputs = arrayOf<Any>(preprocess(selectedMovies))

            // Run inference.
            val outputIds = IntArray(config.outputLength)
            val confidences = FloatArray(config.outputLength)
            val outputs: MutableMap<Int, Any> = HashMap()
            outputs[config.outputIdsIndex] = outputIds
            outputs[config.outputScoresIndex] = confidences
            tflite?.let {
                it.runForMultipleInputsOutputs(inputs, outputs)
                postprocess(outputIds, confidences, selectedMovies)
            } ?: run {
                Log.e(TAG, "No tflite interpreter loaded")
                emptyList()
            }
        }
    }

    private fun downloadRemoteModel() {
        downloadModel(config.remoteModelName)
    }

    private fun downloadModel(modelName: String) {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel(modelName, DownloadType.LOCAL_MODEL, conditions)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    showToast(context, "Failed to get model file.")
                } else {
                    showToast(context, "Downloaded remote model: $modelName")
                    GlobalScope.launch { initializeInterpreter(it.result) }
                }
            }
            .addOnFailureListener {
                showToast(context, "Model download failed for recommendations, please check your connection.")
            }
    }

    companion object {
        private const val TAG = "RecommendationClient"
    }
}
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
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
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
            if (model is ByteBuffer) {
                tflite = Interpreter(model)
            } else {
                tflite = Interpreter(model as File)
            }
            Log.v(TAG, "TFLite model loaded.")
        }
    }

    /** Load recommendation candidate list.  */
    private suspend fun loadCandidateList() {
        // TODO: Replace this function with code from the codelab to load a list of recommendation
        // candidates.
    }

    /** Free up resources as the client is no longer needed.  */
    fun unload() {
        tflite?.close()
        candidates.clear()
    }

    /** Given a list of selected items, and returns the recommendation results.  */
    @Synchronized
    suspend fun recommend(selectedMovies: List<Movie>): List<Result> {
        // TODO: Replace this function with code from the codelab to generate recommendation using
        //  the downloaded model.

        return withContext(Dispatchers.Default) {
            listOf()
        }
    }

    private fun downloadRemoteModel() {
        downloadModel(config.remoteModelName)
    }

    private fun downloadModel(modelName: String) {
        // TODO: Insert code from the codelab here to download the remote model from
        //  Firebase Machine Learning.
    }

    companion object {
        private const val TAG = "RecommendationClient"
    }
}
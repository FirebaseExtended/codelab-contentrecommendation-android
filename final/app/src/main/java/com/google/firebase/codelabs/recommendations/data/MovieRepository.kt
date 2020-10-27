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

package com.google.firebase.codelabs.recommendations.data

import android.content.Context
import com.google.firebase.codelabs.recommendations.utils.Config
import com.google.firebase.codelabs.recommendations.utils.getJsonDataFromAsset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Provides the centralized list of recognized movies.
 */
class MovieRepository private constructor() {
    private val items: MutableList<Movie> = mutableListOf()

    suspend fun getContent(): List<Movie> {
        if (items.isEmpty()) {
            val jsonFileString = getJsonDataFromAsset(context!!, Config().movieListPath)

            val gson = Gson()
            val listPersonType = object : TypeToken<List<Movie>>() {}.type

            items.addAll(gson.fromJson(jsonFileString, listPersonType))
        }
        return items
    }

    companion object {
        @Volatile private var instance: MovieRepository? = null
        @Volatile private var context: Context? = null

        fun getInstance(inContext: Context) =
            instance ?: synchronized(this) {
                instance ?: MovieRepository().also {
                    instance = it
                    context = inContext
                }
            }
    }
}
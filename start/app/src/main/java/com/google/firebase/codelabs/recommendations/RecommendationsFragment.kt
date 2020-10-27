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

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.codelabs.recommendations.adapters.RecommendationsAdapter
import com.google.firebase.codelabs.recommendations.data.Movie
import com.google.firebase.codelabs.recommendations.databinding.FragmentRecommendationsBinding
import com.google.firebase.codelabs.recommendations.utils.Config
import com.google.firebase.codelabs.recommendations.viewmodels.LikedMoviesViewModel
import kotlinx.coroutines.launch

/**
 * A fragment containing the list of generated movie recommendations for the items in the Liked
 * page.
 */
class RecommendationsFragment : Fragment() {

    private var config = Config()
    private var client: RecommendationClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client = RecommendationClient(requireContext(), config)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentRecommendationsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val adapter = RecommendationsAdapter()
        binding.list.adapter = adapter
        val viewModel: LikedMoviesViewModel = ViewModelProvider(requireActivity()).get(LikedMoviesViewModel::class.java)
        viewModel.movies.observe(viewLifecycleOwner) {
            recommend(it.filter { movie -> movie.liked }.toList(), adapter)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            client?.load()
        }
    }

    override fun onStop() {
        lifecycleScope.launch {
            client?.unload()
        }
        super.onStop()
    }

    /** Sends selected movie list and get recommendations.  */
    private fun recommend(movies: List<Movie>, adapter: RecommendationsAdapter) {
        lifecycleScope.launch {

            // Run inference with TF Lite.
            Log.d(TAG, "Run inference with TFLite model.")
            client?.recommend(movies)?.run {
                Log.d(TAG, toString())
                adapter.submitList(this)
            }
        }
    }

    companion object {
        private val TAG = "RecommendationsFragment";
    }
}
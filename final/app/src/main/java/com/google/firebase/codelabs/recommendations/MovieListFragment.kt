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
import com.google.firebase.codelabs.recommendations.adapters.FilterType
import com.google.firebase.codelabs.recommendations.adapters.ItemClickListener
import com.google.firebase.codelabs.recommendations.adapters.MoviesAdapter
import com.google.firebase.codelabs.recommendations.data.Movie
import com.google.firebase.codelabs.recommendations.databinding.FragmentMovieListBinding
import com.google.firebase.codelabs.recommendations.viewmodels.LikedMoviesViewModel


/**
 * A fragment containing the list of available movies recognized in our model.
 */
class MovieListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMovieListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val viewModel: LikedMoviesViewModel = ViewModelProvider(requireActivity()).get(LikedMoviesViewModel::class.java)
        val movieClickListener = object : ItemClickListener() {
                         override fun onLike(movie: Movie) {
                            viewModel.onMovieLiked(movie)
                        }

                        override fun onRemoveLike(movie: Movie) {
                            viewModel.onMovieLikeRemoved(movie)
                        }
        }
        val adapter = MoviesAdapter(movieClickListener, FilterType.NONE)
        binding.list.adapter = adapter
        viewModel.movies.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
            adapter.notifyDataSetChanged()
        }
        setHasOptionsMenu(true)
        return binding.root
    }
}
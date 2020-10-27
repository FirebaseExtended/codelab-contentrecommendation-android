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

package com.google.firebase.codelabs.recommendations.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.card.MaterialCardView
import com.google.firebase.codelabs.recommendations.R
import com.google.firebase.codelabs.recommendations.data.Movie

/**
 * Adapter used to populate the recyclerviews for the Movies and Liked tabs.
 */
class MoviesAdapter(private val clickListener: ItemClickListener, private val filterType: FilterType) :
    ListAdapter<Movie, MoviesAdapter.ViewHolder>(
    MoviesDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_movie, parent, false)
        return ViewHolder(view)
    }

    override fun submitList(list: List<Movie>?) {
        if (filterType == FilterType.LIKED) {
            list?.apply {
                val likedList = filter { movie -> movie.liked }
                super.submitList(likedList)
            }
        } else {
            super.submitList(list)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = getItem(position)
        holder.contentView.text = movie.title
        holder.starButton.isChecked = movie.liked
        holder.starButton.isClickable = false
        holder.starButton.isFocusable = false
        holder.cardView.setOnClickListener {
            if (holder.starButton.isChecked) clickListener.onRemoveLike(movie) else clickListener.onLike(movie)
            notifyItemChanged(position)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val starButton: ToggleButton = view.findViewById(R.id.star_button)
        val contentView: TextView = view.findViewById(R.id.title)
        val cardView: MaterialCardView = view.findViewById(R.id.card)
        init {
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}

private class MoviesDiffCallback : DiffUtil.ItemCallback<Movie>() {

    override fun areItemsTheSame(
        oldItem: Movie,
        newItem: Movie
    ): Boolean {
    return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Movie,
        newItem: Movie
    ): Boolean {
        return oldItem == newItem && oldItem.liked == newItem.liked
    }
}

abstract class ItemClickListener() {
    abstract fun onLike(movie: Movie)
    abstract fun onRemoveLike(movie: Movie)
}

enum class FilterType {
   NONE, LIKED
}

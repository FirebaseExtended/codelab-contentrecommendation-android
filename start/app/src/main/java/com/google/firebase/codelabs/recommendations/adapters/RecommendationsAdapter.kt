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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.firebase.codelabs.recommendations.R
import com.google.firebase.codelabs.recommendations.RecommendationClient.Result

/**
 * Used to populate the recommendations results.
 */
class RecommendationsAdapter :
    ListAdapter<Result, RecommendationsAdapter.ViewHolder>(
    RecommendationsDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position)
        holder.contentView.text = result.item.title
        holder.confidenceView.text = String.format("%.5f", result.confidence)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.findViewById(R.id.title)
        val confidenceView: TextView = view.findViewById(R.id.confidence)
        init {
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}

private class RecommendationsDiffCallback : DiffUtil.ItemCallback<Result>() {

    override fun areItemsTheSame(
        oldItem: Result,
        newItem: Result
    ): Boolean {
    return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Result,
        newItem: Result
    ): Boolean {
        return oldItem == newItem
    }
}

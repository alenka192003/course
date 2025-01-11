package com.example.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.course.databinding.ItemMediaBinding

class MediaAdapter(
    private val mediaList: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(mediaList[position])
    }

    override fun getItemCount(): Int = mediaList.size

    inner class MediaViewHolder(
        private val binding: ItemMediaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(media: MediaItem) {
            Glide.with(itemView)
                .load(media.uri)
                .centerCrop()
                .into(binding.imageView)

            binding.videoIndicator.visibility = when (media.type) {
                MediaType.VIDEO -> View.VISIBLE
                MediaType.IMAGE -> View.GONE
            }

            itemView.setOnClickListener { onItemClick(media) }
        }
    }
}
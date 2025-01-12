package com.example.course

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.course.databinding.ItemMediaBinding

class MediaAdapter(
    private val mediaList: List<MediaItem>,
    private val onItemClick: (MediaItem, Int) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.bind(mediaItem)

        val params = holder.itemView.layoutParams
        val screenWidth = holder.itemView.context.resources.displayMetrics.widthPixels
        val spanCount = 3 // Количество столбцов
        val spacing = 16.dpToPx(holder.itemView.context) // Отступы между элементами

        params.width = (screenWidth - (spanCount + 1) * spacing) / spanCount
        params.height = params.width
        holder.itemView.layoutParams = params

        holder.itemView.setOnClickListener {
            onItemClick(mediaItem, position)
        }
    }


    override fun getItemCount(): Int = mediaList.size

    inner class MediaViewHolder(
        private val binding: ItemMediaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(media: MediaItem) {
            when (media.type) {
                MediaType.VIDEO -> {
                    binding.videoIndicator.visibility = View.VISIBLE
                    // Загружаем превью видео
                    Glide.with(itemView)
                        .asBitmap()
                        .load(media.uri)
                        .centerCrop()
                        .into(binding.imageView)
                }
                MediaType.IMAGE -> {
                    binding.videoIndicator.visibility = View.GONE
                    Glide.with(itemView)
                        .load(media.uri)
                        .centerCrop()
                        .into(binding.imageView)
                }
            }

            // Передаём два параметра в onItemClick
            itemView.setOnClickListener { onItemClick(media, adapterPosition) }
        }
    }
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

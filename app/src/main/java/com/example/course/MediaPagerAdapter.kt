package com.example.course

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import com.example.course.databinding.ItemMediaBinding

class MediaPagerAdapter(
    private var mediaList: MutableList<MediaItem>,
    private val onDeleteClick: (MediaItem, Int) -> Unit
) : RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.bind(mediaItem, position)
    }

    override fun getItemCount(): Int = mediaList.size

    fun removeItem(position: Int) {
        mediaList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mediaList.size)
    }

    inner class MediaViewHolder(
        private val binding: ItemMediaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mediaItem: MediaItem, position: Int) {
            if (mediaItem.type == MediaType.IMAGE) {
                binding.imageView.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE
                binding.imageView.setImageURI(mediaItem.uri)
                binding.videoView.stopPlayback() // Останавливаем видео если оно воспроизводилось
            } else if (mediaItem.type == MediaType.VIDEO) {
                binding.imageView.visibility = View.GONE
                binding.videoView.visibility = View.VISIBLE

                // Настройка VideoView
                binding.videoView.setVideoURI(mediaItem.uri)
                val mediaController = MediaController(binding.root.context)
                mediaController.setAnchorView(binding.videoView)
                binding.videoView.setMediaController(mediaController)

                // Автовоспроизведение
                binding.videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()
                }

                // Обработка ошибок
                binding.videoView.setOnErrorListener { mp, what, extra ->
                    Log.e("MediaPagerAdapter", "Error playing video: what=$what extra=$extra")
                    false
                }
            }

            // Setup delete button
            binding.deleteButton.setOnClickListener {
                binding.videoView.stopPlayback() // Останавливаем видео перед удалением
                onDeleteClick(mediaItem, position)
            }
        }
    }

}
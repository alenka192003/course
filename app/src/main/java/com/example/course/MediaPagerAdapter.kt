package com.example.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView

class MediaPagerAdapter(
    private val mediaList: List<MediaItem>
) : RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int = mediaList.size

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        //private val videoView: VideoView = itemView.findViewById(R.id.videoView)

        fun bind(mediaItem: MediaItem) {
            if (mediaItem.type == MediaType.IMAGE) {
                imageView.visibility = View.VISIBLE
                //videoView.visibility = View.GONE
                imageView.setImageURI(mediaItem.uri)
            } else if (mediaItem.type == MediaType.VIDEO) {
                imageView.visibility = View.GONE
               // videoView.visibility = View.VISIBLE
                //videoView.setVideoURI(mediaItem.uri)
                //videoView.start()
            }
        }
    }
}
package com.example.course

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.example.course.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaAdapter
    private val mediaList = mutableListOf<MediaItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadMedia()
    }


    private fun loadMedia() {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.MIME_TYPE
        )

        val imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        // Query for images
        requireContext().contentResolver.query(
            imageCollection,
            projection,
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?",
            arrayOf("%Pictures/CameraApp%"),
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val date = cursor.getLong(dateColumn)
                val name = cursor.getString(nameColumn)
                val contentUri = ContentUris.withAppendedId(imageCollection, id)

                mediaList.add(MediaItem(id, contentUri, name, date, MediaType.IMAGE))
            }
        }


        // Query for videos
        requireContext().contentResolver.query(
            videoCollection,
            projection,
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?",
            arrayOf("%Pictures/CameraApp%"),
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val date = cursor.getLong(dateColumn)
                val name = cursor.getString(nameColumn)
                val contentUri = ContentUris.withAppendedId(videoCollection, id)

                mediaList.add(MediaItem(id, contentUri, name, date, MediaType.VIDEO))
            }
        }

        mediaAdapter.notifyDataSetChanged()
    }


    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(mediaList) { media ->
            val position = mediaList.indexOf(media)
            val bundle = Bundle().apply {
                putParcelableArrayList("mediaList", ArrayList(mediaList))
                putInt("initialPosition", position)
            }

            view?.let { view ->
                Navigation.findNavController(view).navigate(
                    R.id.action_gallery_to_media_viewer,
                    bundle
                )
            }
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = mediaAdapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
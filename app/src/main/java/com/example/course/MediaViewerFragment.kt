package com.example.course

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.example.course.databinding.FragmentMediaViewerBinding

class MediaViewerFragment : Fragment() {
    private var _binding: FragmentMediaViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaUri: String
    private lateinit var mediaType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMediaViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaUri = arguments?.getString("uri") ?: ""
        mediaType = arguments?.getString("type") ?: ""

        displayMedia()
    }

    private fun displayMedia() {
        if (mediaType == "IMAGE") {
            binding.imageView.apply {
                visibility = View.VISIBLE
                setImageURI(Uri.parse(mediaUri))
            }
            binding.videoView.visibility = View.GONE
        } else if (mediaType == "VIDEO") {
            binding.videoView.apply {
                visibility = View.VISIBLE
                setVideoURI(Uri.parse(mediaUri))
                start()
            }
            binding.imageView.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

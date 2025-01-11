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

    private lateinit var mediaList: ArrayList<MediaItem>
    private var initialPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMediaViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaList = arguments?.getParcelableArrayList("mediaList") ?: arrayListOf()
        initialPosition = arguments?.getInt("initialPosition") ?: 0

        val adapter = MediaPagerAdapter(mediaList)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(initialPosition, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
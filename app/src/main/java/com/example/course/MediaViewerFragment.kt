package com.example.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.course.databinding.FragmentMediaViewerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentResolver
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult

class MediaViewerFragment : Fragment() {
    private var _binding: FragmentMediaViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaList: MutableList<MediaItem>
    private lateinit var pagerAdapter: MediaPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaList = arguments?.getParcelableArrayList<MediaItem>("mediaList")?.toMutableList()
            ?: mutableListOf()
        val initialPosition = arguments?.getInt("initialPosition") ?: 0

        setupViewPager(initialPosition)
    }

    private fun setupViewPager(initialPosition: Int) {
        pagerAdapter = MediaPagerAdapter(
            mediaList = mediaList,
            onDeleteClick = { mediaItem, position ->
                showDeleteConfirmationDialog(mediaItem, position)
            }
        )

        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.setCurrentItem(initialPosition, false) // Установка позиции
    }

    private fun showDeleteConfirmationDialog(mediaItem: MediaItem, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить медиафайл")
            .setMessage("Вы уверены, что хотите удалить этот файл?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteMedia(mediaItem, position)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteMedia(mediaItem: MediaItem, position: Int) {
        val contentResolver: ContentResolver = requireContext().contentResolver

        try {
            val rowsDeleted = contentResolver.delete(mediaItem.uri, null, null)

            if (rowsDeleted > 0) {
                // Удаляем из списка
                pagerAdapter.removeItem(position)

                // Отправляем результат обратно в GalleryFragment
                setFragmentResult("media_deleted", bundleOf("deleted_id" to mediaItem.id))

                if (mediaList.isEmpty()) {
                    findNavController().navigateUp()
                }
            }
        } catch (e: Exception) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ошибка")
                .setMessage("Не удалось удалить файл: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
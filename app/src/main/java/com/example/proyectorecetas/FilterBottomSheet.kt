package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.example.proyectorecetas.databinding.BottomSheetFilterBinding

class FilterBottomSheet(
    private val onFiltersSelected: (List<String>, List<String>) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    private val difficulties = listOf("Fácil", "Media", "Difícil")
    private val selectedDifficulties = mutableListOf<String>()

    private val categories = listOf(
        "Ensaladas", "Carnes", "Bebidas", "Postres"
    )

    private val selectedFilters = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryChips()
        setupDifficultyChips()
        setupApplyButton()
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.removeAllViews()

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = selectedFilters.contains(category)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedFilters.add(category)
                    } else {
                        selectedFilters.remove(category)
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupDifficultyChips() {
        binding.chipGroupDifficulties.removeAllViews()

        difficulties.forEach { difficulty ->
            val chip = Chip(requireContext()).apply {
                text = difficulty
                isCheckable = true
                isChecked = selectedDifficulties.contains(difficulty)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDifficulties.add(difficulty)
                    } else {
                        selectedDifficulties.remove(difficulty)
                    }
                }
            }
            binding.chipGroupDifficulties.addView(chip)
        }
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            onFiltersSelected(selectedFilters.toList(), selectedDifficulties.toList())
            dismiss()
        }

        binding.btnClear.setOnClickListener {
            selectedFilters.clear()
            selectedDifficulties.clear()
            binding.chipGroupCategories.clearCheck()
            binding.chipGroupDifficulties.clearCheck()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
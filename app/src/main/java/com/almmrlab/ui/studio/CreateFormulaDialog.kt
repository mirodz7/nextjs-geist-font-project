package com.almmrlab.ui.studio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.almmrlab.R
import com.almmrlab.databinding.DialogCreateFormulaBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateFormulaDialog : DialogFragment() {

    private var _binding: DialogCreateFormulaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerfumeStudioViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_AlmmrLab_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateFormulaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupInputValidation()
        setupKeywordChips()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener { dismiss() }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_save -> {
                        createFormula()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupInputValidation() {
        binding.apply {
            // Name validation
            nameInput.editText?.doAfterTextChanged { text ->
                nameInput.error = if (text.isNullOrBlank()) {
                    getString(R.string.error_required)
                } else {
                    null
                }
                updateSaveButtonState()
            }

            // Description validation
            descriptionInput.editText?.doAfterTextChanged { text ->
                descriptionInput.error = if (text.isNullOrBlank()) {
                    getString(R.string.error_required)
                } else {
                    null
                }
                updateSaveButtonState()
            }

            // Perfumer validation
            perfumerInput.editText?.doAfterTextChanged { text ->
                perfumerInput.error = if (text.isNullOrBlank()) {
                    getString(R.string.error_required)
                } else {
                    null
                }
                updateSaveButtonState()
            }
        }
    }

    private fun setupKeywordChips() {
        binding.keywordsInput.editText?.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val keyword = textView.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    addKeywordChip(keyword)
                    textView.text = ""
                }
                true
            } else {
                false
            }
        }
    }

    private fun addKeywordChip(keyword: String) {
        val chip = Chip(requireContext()).apply {
            text = keyword
            isCloseIconVisible = true
            setOnCloseIconClickListener { binding.keywordsChipGroup.removeView(this) }
        }
        binding.keywordsChipGroup.addView(chip)
    }

    private fun updateSaveButtonState() {
        val isValid = binding.run {
            nameInput.error == null && !nameInput.editText?.text.isNullOrBlank() &&
            descriptionInput.error == null && !descriptionInput.editText?.text.isNullOrBlank() &&
            perfumerInput.error == null && !perfumerInput.editText?.text.isNullOrBlank()
        }
        binding.toolbar.menu.findItem(R.id.action_save)?.isEnabled = isValid
    }

    private fun createFormula() {
        val keywords = mutableListOf<String>().apply {
            for (i in 0 until binding.keywordsChipGroup.childCount) {
                val chip = binding.keywordsChipGroup.getChildAt(i) as? Chip
                chip?.text?.toString()?.let { add(it) }
            }
        }

        viewModel.createFormula(
            name = binding.nameInput.editText?.text.toString(),
            description = binding.descriptionInput.editText?.text.toString(),
            perfumer = binding.perfumerInput.editText?.text.toString(),
            keywords = keywords
        )

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.almmrlab.ui.studio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.almmrlab.R
import com.almmrlab.data.entities.FormulaStatus
import com.almmrlab.data.entities.MaterialType
import com.almmrlab.databinding.FragmentPerfumeStudioBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PerfumeStudioFragment : Fragment() {

    private var _binding: FragmentPerfumeStudioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerfumeStudioViewModel by viewModels()
    private lateinit var formulaAdapter: FormulaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfumeStudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFab()
        setupObservers()
        setupFilterChips()
    }

    private fun setupRecyclerView() {
        formulaAdapter = FormulaAdapter(
            onItemClick = { formula ->
                findNavController().navigate(
                    PerfumeStudioFragmentDirections.actionStudioToFormulaDetail(formula.id)
                )
            },
            onDuplicateClick = { formula ->
                viewModel.duplicateFormula(formula.id)
            },
            onArchiveClick = { formula ->
                showArchiveConfirmationDialog(formula.id)
            }
        )

        binding.formulaList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = formulaAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddFormula.setOnClickListener {
            showCreateFormulaDialog()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe formulas
                launch {
                    viewModel.formulas.collect { formulas ->
                        formulaAdapter.submitList(formulas)
                        updateEmptyState(formulas.isEmpty())
                    }
                }

                // Observe events
                launch {
                    viewModel.events.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun setupFilterChips() {
        // Status filter
        binding.chipGroupStatus.setOnCheckedChangeListener { group, checkedId ->
            val status = when (checkedId) {
                R.id.chip_draft -> FormulaStatus.DRAFT
                R.id.chip_development -> FormulaStatus.IN_DEVELOPMENT
                R.id.chip_testing -> FormulaStatus.TESTING
                R.id.chip_approved -> FormulaStatus.APPROVED
                R.id.chip_production -> FormulaStatus.PRODUCTION
                else -> null
            }
            viewModel.setStatusFilter(status)
        }

        // Note type filter
        binding.chipGroupNoteType.setOnCheckedChangeListener { group, checkedId ->
            val type = when (checkedId) {
                R.id.chip_top_notes -> MaterialType.TOP_NOTE
                R.id.chip_middle_notes -> MaterialType.MIDDLE_NOTE
                R.id.chip_base_notes -> MaterialType.BASE_NOTE
                else -> null
            }
            viewModel.setNoteTypeFilter(type)
        }
    }

    private fun showCreateFormulaDialog() {
        val dialog = CreateFormulaDialog()
        dialog.show(childFragmentManager, "create_formula")
    }

    private fun showArchiveConfirmationDialog(formulaId: Long) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.archive_formula_title)
            .setMessage(R.string.archive_formula_message)
            .setPositiveButton(R.string.archive) { _, _ ->
                viewModel.archiveFormula(formulaId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.formulaList.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun handleEvent(event: PerfumeStudioEvent) {
        when (event) {
            is PerfumeStudioEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is PerfumeStudioEvent.FormulaDuplicated -> {
                Snackbar.make(
                    binding.root,
                    R.string.formula_duplicated,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            is PerfumeStudioEvent.FormulaArchived -> {
                Snackbar.make(
                    binding.root,
                    R.string.formula_archived,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

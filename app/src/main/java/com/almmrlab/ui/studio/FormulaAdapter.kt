package com.almmrlab.ui.studio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.almmrlab.R
import com.almmrlab.data.entities.PerfumeFormula
import com.almmrlab.databinding.ItemFormulaBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class FormulaAdapter(
    private val onItemClick: (PerfumeFormula) -> Unit,
    private val onDuplicateClick: (PerfumeFormula) -> Unit,
    private val onArchiveClick: (PerfumeFormula) -> Unit
) : ListAdapter<PerfumeFormula, FormulaAdapter.FormulaViewHolder>(FormulaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormulaViewHolder {
        val binding = ItemFormulaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FormulaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FormulaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FormulaViewHolder(
        private val binding: ItemFormulaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.buttonDuplicate.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDuplicateClick(getItem(position))
                }
            }

            binding.buttonArchive.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onArchiveClick(getItem(position))
                }
            }
        }

        fun bind(formula: PerfumeFormula) {
            binding.apply {
                formulaName.text = formula.name
                formulaDescription.text = formula.description

                // Set status chip
                formulaStatus.text = formula.status.name
                formulaStatus.setChipBackgroundColorResource(
                    when (formula.status) {
                        FormulaStatus.DRAFT -> R.color.status_draft
                        FormulaStatus.IN_DEVELOPMENT -> R.color.status_development
                        FormulaStatus.TESTING -> R.color.status_testing
                        FormulaStatus.APPROVED -> R.color.status_approved
                        FormulaStatus.PRODUCTION -> R.color.status_production
                    }
                )

                // Set note counts
                topNotesChip.text = itemView.context.getString(
                    R.string.filter_top_notes,
                    formula.topNotes.size
                )
                middleNotesChip.text = itemView.context.getString(
                    R.string.filter_middle_notes,
                    formula.middleNotes.size
                )
                baseNotesChip.text = itemView.context.getString(
                    R.string.filter_base_notes,
                    formula.baseNotes.size
                )

                // Set formula details
                val lastModifiedDays = TimeUnit.MILLISECONDS.toDays(
                    System.currentTimeMillis() - formula.lastModified.time
                )
                formulaDetails.text = itemView.context.getString(
                    R.string.formula_details_format,
                    formula.perfumer,
                    formula.version,
                    if (lastModifiedDays == 0L) {
                        itemView.context.getString(R.string.today)
                    } else {
                        itemView.context.resources.getQuantityString(
                            R.plurals.days_ago,
                            lastModifiedDays.toInt(),
                            lastModifiedDays
                        )
                    }
                )
            }
        }
    }

    private class FormulaDiffCallback : DiffUtil.ItemCallback<PerfumeFormula>() {
        override fun areItemsTheSame(oldItem: PerfumeFormula, newItem: PerfumeFormula): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PerfumeFormula, newItem: PerfumeFormula): Boolean {
            return oldItem == newItem
        }
    }
}

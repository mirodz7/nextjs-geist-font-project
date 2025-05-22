package com.almmrlab.ui.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almmrlab.data.entities.FormulaStatus
import com.almmrlab.data.entities.MaterialType
import com.almmrlab.data.entities.PerfumeFormula
import com.almmrlab.data.repository.PerfumeFormulaRepository
import com.almmrlab.util.AIManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PerfumeStudioViewModel @Inject constructor(
    private val perfumeFormulaRepository: PerfumeFormulaRepository,
    private val aiManager: AIManager
) : ViewModel() {

    private val _events = MutableSharedFlow<PerfumeStudioEvent>()
    val events = _events.asSharedFlow()

    private val statusFilter = MutableStateFlow<FormulaStatus?>(null)
    private val noteTypeFilter = MutableStateFlow<MaterialType?>(null)
    private val searchQuery = MutableStateFlow("")

    val formulas: StateFlow<List<PerfumeFormula>> = combine(
        perfumeFormulaRepository.getAllFormulas(),
        statusFilter,
        noteTypeFilter,
        searchQuery
    ) { formulas, status, noteType, query ->
        formulas.filter { formula ->
            var matches = true

            // Apply status filter
            if (status != null) {
                matches = matches && formula.status == status
            }

            // Apply note type filter
            if (noteType != null) {
                matches = matches && when (noteType) {
                    MaterialType.TOP_NOTE -> formula.topNotes.isNotEmpty()
                    MaterialType.MIDDLE_NOTE -> formula.middleNotes.isNotEmpty()
                    MaterialType.BASE_NOTE -> formula.baseNotes.isNotEmpty()
                    else -> true
                }
            }

            // Apply search query
            if (query.isNotEmpty()) {
                matches = matches && (
                    formula.name.contains(query, ignoreCase = true) ||
                    formula.description.contains(query, ignoreCase = true) ||
                    formula.perfumer.contains(query, ignoreCase = true)
                )
            }

            matches
        }.sortedByDescending { it.creationDate }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createFormula(
        name: String,
        description: String,
        perfumer: String,
        keywords: List<String>
    ) {
        viewModelScope.launch {
            try {
                // Get AI suggestions for notes based on keywords
                val recommendation = aiManager.suggestNotesFromKeywords(keywords)

                val formula = PerfumeFormula(
                    name = name,
                    description = description,
                    perfumer = perfumer,
                    creationDate = Date(),
                    version = 1,
                    alcoholPercentage = 0.0, // Will be calculated when notes are added
                    topNotes = emptyList(),
                    middleNotes = emptyList(),
                    baseNotes = emptyList(),
                    status = FormulaStatus.DRAFT,
                    lastModified = Date()
                )

                perfumeFormulaRepository.saveFormula(formula)
            } catch (e: Exception) {
                _events.emit(PerfumeStudioEvent.ShowError(e.message ?: "Error creating formula"))
            }
        }
    }

    fun duplicateFormula(id: Long) {
        viewModelScope.launch {
            try {
                val newId = perfumeFormulaRepository.duplicateFormula(id)
                if (newId > 0) {
                    _events.emit(PerfumeStudioEvent.FormulaDuplicated)
                } else {
                    _events.emit(PerfumeStudioEvent.ShowError("Failed to duplicate formula"))
                }
            } catch (e: Exception) {
                _events.emit(PerfumeStudioEvent.ShowError(e.message ?: "Error duplicating formula"))
            }
        }
    }

    fun archiveFormula(id: Long) {
        viewModelScope.launch {
            try {
                perfumeFormulaRepository.archiveFormula(id)
                _events.emit(PerfumeStudioEvent.FormulaArchived)
            } catch (e: Exception) {
                _events.emit(PerfumeStudioEvent.ShowError(e.message ?: "Error archiving formula"))
            }
        }
    }

    fun setStatusFilter(status: FormulaStatus?) {
        statusFilter.value = status
    }

    fun setNoteTypeFilter(type: MaterialType?) {
        noteTypeFilter.value = type
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun getAISuggestions(keywords: List<String>) {
        viewModelScope.launch {
            try {
                val suggestions = aiManager.suggestNotesFromKeywords(keywords)
                // Handle AI suggestions (could emit through events or separate flow)
            } catch (e: Exception) {
                _events.emit(PerfumeStudioEvent.ShowError(e.message ?: "Error getting AI suggestions"))
            }
        }
    }
}

sealed class PerfumeStudioEvent {
    data class ShowError(val message: String) : PerfumeStudioEvent()
    object FormulaDuplicated : PerfumeStudioEvent()
    object FormulaArchived : PerfumeStudioEvent()
}
